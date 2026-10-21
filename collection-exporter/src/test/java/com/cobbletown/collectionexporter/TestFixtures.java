package com.cobbletown.collectionexporter;

import com.cobbletown.collectionexporter.data.InventoryCompleteness;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import com.cobbletown.collectionexporter.data.PokemonRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Deterministic synthetic inventories used by focused tests. */
public final class TestFixtures {
    private static int sequence;

    private TestFixtures() {
    }

    public static PcInventorySnapshot exampleInventory() {
        sequence = 0;
        List<PokemonRecord> entries = new ArrayList<>();
        entries.add(pokemon("cobblemon:tinkaton", "Tinkaton", 959, "female", List.of("female", "october2026spooky"), false, 50));
        entries.add(pokemon("cobblemon:barraskewda", "Barraskewda", 847, "male", List.of("shiny", "male", "september2026crystall"), true, 50));
        entries.add(pokemon("cobblemon:eelektross", "Eelektross", 604, "male", List.of("shiny", "male", "september2026crystall"), true, 50));
        entries.add(pokemon("cobblemon:golem", "Golem", 76, "female", List.of("shiny", "female", "september2026crystall"), true, 50));
        entries.add(pokemon("cobblemon:golem", "Golem", 76, "female", List.of("shiny", "female", "september2026crystall"), true, 50));
        entries.add(pokemon("cobblemon:tangrowth", "Tangrowth", 465, "female", List.of("shiny", "female", "september2026crystall"), true, 50));
        entries.add(pokemon("cobblemon:golem", "Golem", 76, "male", List.of("male", "september2026crystall"), false, 50));
        entries.add(pokemon("cobblemon:houndoom", "Houndoom", 229, "male", List.of("male", "shield-forme", "september2026crystall"), false, 50));
        entries.add(pokemon("cobblemon:magmortar", "Magmortar", 467, "male", List.of("male", "september2026crystall"), false, 50));
        entries.add(pokemon("cobblemon:starmie", "Starmie", 121, "genderless", List.of("genderless", "september2026crystall"), false, 50));
        entries.add(pokemon("cobblemon:tangrowth", "Tangrowth", 465, "male", List.of("male", "september2026crystall"), false, 50));

        entries.add(pokemon("cobblemon:dragapult", "Dragapult", 887, "male", List.of("shiny", "male", "tron2026"), true, 50));
        entries.add(pokemon("cobblemon:tyrantrum", "Tyrantrum", 697, "male", List.of("shiny", "male", "tron2026"), true, 50));
        entries.add(pokemon("cobblemon:entei", "Entei", 244, "genderless", List.of("genderless", "tron2026"), false, 50));
        entries.add(pokemon("cobblemon:mewtwo", "Mewtwo", 150, "genderless", List.of("genderless", "tron2026"), false, 50));
        entries.add(pokemon("cobblemon:pelipper", "Pelipper", 279, "male", List.of("male", "shield-forme", "tron2026"), false, 50));
        entries.add(pokemon("cobblemon:tropius", "Tropius", 357, "female", List.of("female", "shield-forme", "tron2026"), false, 50));

        entries.add(pokemon("cobblemon:jynx", "Jynx", 124, "female", List.of("shiny", "female", "august2026western"), true, 50));
        entries.add(pokemon("cobblemon:jynx", "Jynx", 124, "female", List.of("female", "august2026western"), false, 50));
        entries.add(pokemon("cobblemon:machamp", "Machamp", 68, "female", List.of("female", "shield-forme", "august2026western"), false, 50));
        entries.add(pokemon("cobblemon:venusaur", "Venusaur", 3, "male", List.of("male", "august2026western"), false, 50));
        entries.add(pokemon("cobblemon:venusaur", "Venusaur", 3, "male", List.of("male", "august2026western"), false, 50));

        entries.add(pokemon("cobblemon:armaldo", "Armaldo", 348, "male", List.of("shiny", "male", "shield-forme", "petal2026"), true, 50));
        entries.add(pokemon("cobblemon:machamp", "Machamp", 68, "male", List.of("shiny", "male", "shield-forme", "petal2026"), true, 50));
        entries.add(pokemon("cobblemon:machamp", "Machamp", 68, "male", List.of("male", "petal2026", "shield-forme"), false, 50));

        entries.add(pokemon("cobblemon:aegislash", "Aegislash", 681, "male", List.of("shiny", "male", "shield-forme", "july2026kimono", "totally_unknown_secondary"), true, 50));
        entries.add(pokemon("cobblemon:alakazam", "Alakazam", 65, "male", List.of("shiny", "male", "july2026kimono"), true, 50));
        entries.add(pokemon("cobblemon:charizard", "Charizard", 6, "male", List.of("male", "july2026kimono"), false, 50));

        entries.add(pokemon("cobblemon:chesnaught", "Chesnaught", 652, "female", List.of("female", "relic2026"), false, 50));
        entries.add(pokemon("cobblemon:snorlax", "Snorlax", 143, "male", List.of("male", "easter2026"), false, 50));

        entries.add(pokemon("cobblemon:breloom", "Breloom", 286, "male", List.of("male", "radiation"), false, 50));
        entries.add(pokemon("cobblemon:snivy", "Snivy", 495, "female", List.of("female", "archelian"), false, 50));
        entries.add(pokemon("cobblemon:eevee", "Eevee", 133, "female", List.of("female", "totally_unknown_token"), false, 50));
        entries.add(pokemon("cobblemon:pikachu", "Pikachu", 25, "male", List.of("shiny", "male"), true, 50));
        entries.add(pokemon("cobblemon:mew", "Mew", 151, "genderless", List.of("shiny", "genderless"), true, 50));
        entries.add(pokemon("cobblemon:greattusk", "Great Tusk", 984, "genderless", List.of("genderless"), false, 50));
        entries.add(pokemon("cobblemon:buzzwole", "Buzzwole", 794, "male", List.of("male"), false, 50));

        return new PcInventorySnapshot(
                "2026-10-20T00:00:00Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                150,
                12,
                entries,
                List.of("fixture inventory"));
    }

    public static PcInventorySnapshot conflictInventory() {
        sequence = 100;
        PokemonRecord conflict = pokemon(
                "cobblemon:snorlax", "Snorlax", 143, "male",
                List.of("male", "easter2026", "petal2026"), false, 50);
        return new PcInventorySnapshot(
                "2026-10-20T00:00:01Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                1,
                1,
                List.of(conflict),
                List.of());
    }

    public static PcInventorySnapshot emptyInventory() {
        return new PcInventorySnapshot(
                "2026-10-20T00:00:02Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                150,
                0,
                List.of(),
                List.of());
    }

    public static PcInventorySnapshot skinnedShinyLegendaryInventory() {
        sequence = 300;
        return new PcInventorySnapshot(
                "2026-10-20T00:00:04Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                1,
                1,
                List.of(pokemon("cobblemon:mew", "Mew", 151, "genderless",
                        List.of("shiny", "genderless", "july2026kimono"), true, 50)),
                List.of());
    }

    public static PcInventorySnapshot unresolvedShinyInventory() {
        sequence = 400;
        return new PcInventorySnapshot(
                "2026-10-21T05:20:02.170870Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                1,
                1,
                List.of(pokemon("cobblemon:gyarados", "Gyarados", 130, "male",
                        List.of("shiny", "male", "magikarp-jump-standard", "gyarados-eye-color-red"),
                        true, 50)),
                List.of());
    }

    public static PcInventorySnapshot legendaryAndMythicalInventory() {
        sequence = 500;
        return new PcInventorySnapshot(
                "2026-10-20T00:00:05Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                1,
                1,
                List.of(
                        pokemon("cobblemon:articuno", "Articuno", 144, "genderless",
                                List.of("genderless"), false, 50),
                        pokemon("cobblemon:azelf", "Azelf", 482, "genderless",
                                List.of("genderless"), false, 50),
                        pokemon("cobblemon:manaphy", "Manaphy", 490, "genderless",
                                List.of("genderless"), false, 50),
                        pokemon("cobblemon:mew", "Mew", 151, "genderless",
                                List.of("genderless"), false, 50)),
                List.of());
    }

    public static PcInventorySnapshot customSpeciesInventory() {
        sequence = 600;
        return new PcInventorySnapshot(
                "2026-10-20T00:00:06Z",
                "cobblemon-client-pc",
                "00000000-0000-0000-0000-000000000000",
                InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE,
                1,
                1,
                List.of(pokemon("cobblemon:custommon", "Custommon", 0, "male",
                        List.of("male"), false, 50)),
                List.of());
    }

    public static PcInventorySnapshot partialInventory() {
        sequence = 200;
        return new PcInventorySnapshot(
                "2026-10-20T00:00:03Z",
                "cobblemon-client-pc",
                null,
                InventoryCompleteness.PARTIAL_SINGLE_STORE_FALLBACK,
                150,
                1,
                List.of(pokemon("cobblemon:tinkaton", "Tinkaton", 959, "female",
                        List.of("female", "october2026spooky"), false, 50)),
                List.of("single store fallback"));
    }

    private static PokemonRecord pokemon(String speciesId, String name, int dex, String gender,
                                         List<String> aspects, boolean shiny, int level) {
        sequence++;
        return new PokemonRecord(
                UUID.fromString(String.format("00000000-0000-0000-0000-%012d", sequence)),
                sequence / 30,
                sequence % 30,
                speciesId,
                name,
                dex,
                "normal",
                "Normal",
                aspects,
                shiny,
                level,
                gender,
                null);
    }
}
