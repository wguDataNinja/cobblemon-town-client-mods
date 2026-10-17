package com.cobbletown.pcexporter.snapshot;

import com.cobblemon.mod.common.client.storage.ClientBox;
import com.cobblemon.mod.common.client.storage.ClientPC;
import com.cobblemon.mod.common.pokemon.Pokemon;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Read-only snapshot service: traverses every occupied box/slot of the client-resident PC and
 * writes one private timestamped JSON file. No mutation, no networking.
 */
public final class PcSnapshotService {
    private static final AtomicBoolean EXPORTING = new AtomicBoolean(false);

    private PcSnapshotService() {
    }

    /** Writes to the resolved directory (configured, else default). */
    public static ExportResult export(ClientPC pc) {
        return export(pc, PcPaths.snapshotsDir());
    }

    /** Writes one snapshot into {@code dir}. */
    public static ExportResult export(ClientPC pc, Path dir) {
        if (pc == null) {
            return ExportResult.unavailable("no synchronized PC state is available");
        }
        if (dir == null) {
            return ExportResult.error("no export folder is configured");
        }
        if (!EXPORTING.compareAndSet(false, true)) {
            return ExportResult.busy();
        }
        try {
            if (Files.exists(dir) && !Files.isDirectory(dir)) {
                return ExportResult.error("the export path is not a folder: " + dir);
            }
            if (Files.isDirectory(dir) && !Files.isWritable(dir)) {
                return ExportResult.error("the export folder is not writable: " + dir);
            }

            List<ClientBox> boxes = pc.getBoxes();
            if (boxes == null || boxes.isEmpty()) {
                return ExportResult.unavailable("the PC has no synchronized boxes yet");
            }

            PcSnapshot snapshot = new PcSnapshot();
            snapshot.generatedAt = Instant.now().toString();
            snapshot.source = "cobblemon-client-pc";
            snapshot.producer = "pc-exporter";
            snapshot.producerVersion = ProducerVersion.VERSION;
            snapshot.runtime = RuntimeInfoFactory.capture();
            snapshot.boxCount = boxes.size();

            PcSnapshot.StoreInfo store = new PcSnapshot.StoreInfo();
            store.uuid = pc.getUuid() != null ? pc.getUuid().toString() : null;
            store.boxCount = boxes.size();
            snapshot.store = store;

            snapshot.entries = new ArrayList<>();

            int observedBoxes = 0;
            for (int boxIndex = 0; boxIndex < boxes.size(); boxIndex++) {
                ClientBox box = boxes.get(boxIndex);
                if (box == null) {
                    continue;
                }
                List<Pokemon> slots = box.getSlots();
                boolean boxObserved = false;
                for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
                    Pokemon pokemon = slots.get(slotIndex);
                    if (pokemon != null) {
                        snapshot.entries.add(PokemonMapper.map(boxIndex, slotIndex, pokemon));
                        boxObserved = true;
                    }
                }
                if (boxObserved) {
                    observedBoxes++;
                }
            }
            store.observedBoxCount = observedBoxes;

            // The client PC store is fully synchronized when present (Cobblemon sends
            // InitializePCPacket + one SetPCBoxPacket per box). We can truthfully state we
            // traversed the complete synchronized store; we assert no stronger guarantee.
            snapshot.completeness = "complete-synchronized-store";

            Path out = dir.resolve(PcPaths.snapshotFileName());
            Files.createDirectories(out.getParent());
            Files.writeString(out, SnapshotJson.toJson(snapshot),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            return ExportResult.success(snapshot.entries.size(), out.toString());
        } catch (AccessDeniedException denied) {
            return ExportResult.error("the export folder is not writable: " + dir);
        } catch (IOException io) {
            return ExportResult.error("could not write snapshot: " + io.getMessage());
        } catch (RuntimeException runtime) {
            return ExportResult.error(runtime.getClass().getSimpleName() + ": " + runtime.getMessage());
        } finally {
            EXPORTING.set(false);
        }
    }
}
