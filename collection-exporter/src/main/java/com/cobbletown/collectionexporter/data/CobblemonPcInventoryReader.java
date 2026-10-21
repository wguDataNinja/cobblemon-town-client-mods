package com.cobbletown.collectionexporter.data;

import com.cobblemon.mod.common.client.CobblemonClient;
import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.client.storage.ClientStorageManager;
import com.cobblemon.mod.common.pokemon.Pokemon;
import net.minecraft.client.MinecraftClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reads the player's client-resident Cobblemon PC directly from Cobblemon's public client
 * storage. No PC Plus API, screen, packet interception, or mutation is involved.
 *
 * <p>The store is selected by the local player UUID. Cobblemon keeps a map of PC stores because
 * it can also observe other players' PCs; choosing by ownership avoids exporting the wrong one.
 * The client store is the complete synchronized store only when Cobblemon has sent the
 * initialization plus box updates. The snapshot states exactly what was observed.</p>
 */
public final class CobblemonPcInventoryReader implements PcInventoryReader {
    @Override
    public PcInventorySnapshot read() {
        String capturedAt = Instant.now().toString();
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return PcInventorySnapshot.unavailable(capturedAt, "no local player is in a world");
        }

        ClientStorageManager storage = CobblemonClient.INSTANCE.getStorage();
        if (storage == null) {
            return PcInventorySnapshot.unavailable(capturedAt, "Cobblemon client storage is unavailable");
        }
        Map<UUID, ClientPC> stores = storage.getPcStores();
        if (stores == null) {
            return PcInventorySnapshot.unavailable(capturedAt, "Cobblemon PC store map is unavailable");
        }

        UUID playerId = client.player.getUuid();
        PcStoreSelection.Selection<ClientPC> selection = PcStoreSelection.select(stores, playerId);
        return switch (selection.status()) {
            case OWN_STORE -> readStore(capturedAt, selection, InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                    "selected the PC store matching the local player UUID");
            case SINGLE_STORE_FALLBACK -> readStore(capturedAt, selection,
                    InventoryCompleteness.PARTIAL_SINGLE_STORE_FALLBACK,
                    "only one client PC store is present but its UUID did not match the local player; ownership is not proven");
            case AMBIGUOUS -> new PcInventorySnapshot(
                    capturedAt,
                    "cobblemon-client-pc",
                    null,
                    InventoryCompleteness.AMBIGUOUS_MULTIPLE_STORES,
                    0,
                    0,
                    List.of(),
                    List.of("multiple client PC stores are present and none matches the local player UUID; refusing to choose one"));
            case NONE -> PcInventorySnapshot.unavailable(capturedAt,
                    "no client PC store is synchronized; open the PC once or wait for synchronization");
        };
    }

    private PcInventorySnapshot readStore(String capturedAt,
                                          PcStoreSelection.Selection<ClientPC> selection,
                                          InventoryCompleteness baseCompleteness,
                                          String selectionDiagnostic) {
        ClientPC pc = selection.store();
        List<String> diagnostics = new ArrayList<>();
        diagnostics.add(selectionDiagnostic);

        List<ClientBox> boxes = pc.getBoxes();
        if (boxes == null || boxes.isEmpty()) {
            return new PcInventorySnapshot(
                    capturedAt,
                    "cobblemon-client-pc",
                    storeUuid(selection),
                    InventoryCompleteness.PARTIAL_INCOMPLETE_STORE,
                    0,
                    0,
                    List.of(),
                    List.of("the selected PC store has no synchronized boxes"));
        }

        List<PokemonRecord> entries = new ArrayList<>();
        int observedBoxes = 0;
        boolean incomplete = false;

        for (int boxIndex = 0; boxIndex < boxes.size(); boxIndex++) {
            ClientBox box = boxes.get(boxIndex);
            if (box == null) {
                incomplete = true;
                diagnostics.add("box " + boxIndex + " was not present in the synchronized store");
                continue;
            }
            List<Pokemon> slots = box.getSlots();
            if (slots == null) {
                incomplete = true;
                diagnostics.add("box " + boxIndex + " has no readable slot list");
                continue;
            }
            boolean boxObserved = false;
            for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
                Pokemon pokemon = slots.get(slotIndex);
                if (pokemon == null) {
                    continue;
                }
                boxObserved = true;
                try {
                    entries.add(map(boxIndex, slotIndex, pokemon));
                } catch (RuntimeException failure) {
                    incomplete = true;
                    diagnostics.add("could not read box " + boxIndex + " slot " + slotIndex
                            + ": " + failure.getClass().getSimpleName());
                }
            }
            if (boxObserved) {
                observedBoxes++;
            }
        }

        InventoryCompleteness completeness = incomplete
                ? InventoryCompleteness.PARTIAL_INCOMPLETE_STORE
                : baseCompleteness;
        if (incomplete) {
            diagnostics.add("the snapshot is partial; do not present it as a complete inventory");
        }
        return new PcInventorySnapshot(
                capturedAt,
                "cobblemon-client-pc",
                storeUuid(selection),
                completeness,
                boxes.size(),
                observedBoxes,
                entries,
                diagnostics);
    }

    private static String storeUuid(PcStoreSelection.Selection<ClientPC> selection) {
        return selection.storeId() == null ? null : selection.storeId().toString();
    }

    private static PokemonRecord map(int box, int slot, Pokemon pokemon) {
        String speciesId = pokemon.getSpecies() == null
                ? "unknown"
                : pokemon.getSpecies().getResourceIdentifier().toString();
        String speciesName = pokemon.getSpecies() == null || pokemon.getSpecies().getTranslatedName() == null
                ? speciesId
                : pokemon.getSpecies().getTranslatedName().getString();
        int dex = pokemon.getSpecies() == null ? 0 : pokemon.getSpecies().getNationalPokedexNumber();
        String formId = pokemon.getForm() == null ? "normal" : pokemon.getForm().formOnlyShowdownId();
        String formName = pokemon.getForm() == null ? null : pokemon.getForm().getName();
        List<String> aspects = pokemon.getAspects() == null
                ? List.of()
                : List.copyOf(pokemon.getAspects());
        String gender = pokemon.getGender() == null ? null : pokemon.getGender().getShowdownName();
        String nickname = pokemon.getNickname() == null ? null : pokemon.getNickname().getString();
        return new PokemonRecord(
                pokemon.getUuid(),
                box,
                slot,
                speciesId,
                speciesName,
                dex,
                formId,
                formName,
                aspects,
                pokemon.getShiny(),
                pokemon.getLevel(),
                gender,
                nickname);
    }
}
