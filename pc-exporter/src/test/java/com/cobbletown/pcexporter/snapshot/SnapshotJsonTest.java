package com.cobbletown.pcexporter.snapshot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotJsonTest {

    private static PcSnapshot sampleSnapshot() {
        PcSnapshot snapshot = new PcSnapshot();
        snapshot.generatedAt = "2026-10-03T00:00:00Z";
        snapshot.source = "cobblemon-client-pc";
        snapshot.producer = "pc-exporter";
        snapshot.producerVersion = "0.2.0";
        PcSnapshot.RuntimeInfo runtime = new PcSnapshot.RuntimeInfo();
        runtime.minecraft = "1.21.1";
        runtime.cobblemon = "1.8.1";
        runtime.fabricLoader = "0.18.4";
        runtime.fabricApi = "0.116.7+1.21.1";
        runtime.java = "21";
        snapshot.runtime = runtime;
        snapshot.boxCount = 2;
        PcSnapshot.StoreInfo store = new PcSnapshot.StoreInfo();
        store.uuid = "11111111-2222-3333-4444-555555555555";
        store.boxCount = 2;
        store.observedBoxCount = 1;
        snapshot.store = store;
        snapshot.completeness = "complete-synchronized-store";

        PcEntry entry = new PcEntry();
        entry.box = 1;
        entry.slot = 7;
        entry.uuid = "00000000-0000-0000-0000-000000000001";
        entry.species = "cobblemon:pikachu";
        entry.speciesName = "Pikachu";
        entry.dexNumber = 25;
        entry.form = "Normal";
        entry.formId = "normal";
        entry.aspects = List.of("male");
        entry.nickname = "Sparky";
        entry.gender = "male";
        entry.level = 42;
        entry.experience = 12345;
        entry.shiny = true;
        entry.nature = "cobblemon:jolly";
        entry.mintedNature = "cobblemon:adamant";
        entry.ability = "static";
        entry.abilityHidden = false;
        entry.abilitySlot = 0;
        entry.eggGroups = List.of("field", "fairy");
        Map<String, Integer> ivs = new LinkedHashMap<>();
        ivs.put("hp", 31);
        ivs.put("atk", 30);
        entry.ivs = ivs;
        Map<String, Integer> evs = new LinkedHashMap<>();
        evs.put("speed", 252);
        entry.evs = evs;
        Map<String, Integer> stats = new LinkedHashMap<>();
        stats.put("speed", 150);
        entry.stats = stats;
        MoveInfo move = new MoveInfo();
        move.name = "thunderbolt";
        move.type = "electric";
        move.pp = 15;
        move.maxPp = 15;
        entry.moves = List.of(move);
        entry.heldItem = "cobblemon:light_ball";
        entry.heldItemCount = 1;
        entry.cosmeticItem = "cobblemon:example_cosmetic";
        entry.cosmeticItemCount = 1;
        entry.activeMark = "cobblemon:mark_personality_flustered";
        entry.marks = List.of("cobblemon:mark_personality_flustered");
        FeatureInfo feature = new FeatureInfo();
        feature.name = "season";
        feature.value = new JsonPrimitive("summer");
        entry.features = List.of(feature);
        BenchedMoveInfo benched = new BenchedMoveInfo();
        benched.name = "tackle";
        benched.ppRaisedStages = 2;
        entry.benchedMoves = List.of(benched);
        entry.tradeable = true;
        entry.friendship = 200;
        entry.caughtBall = "cobblemon:pokeball";
        entry.otType = "PLAYER";
        entry.otName = "Buddy";
        snapshot.entries = List.of(entry);
        return snapshot;
    }

    @Test
    void serializesSnapshotShape() {
        JsonObject parsed = new Gson().fromJson(SnapshotJson.toJson(sampleSnapshot()), JsonObject.class);

        assertEquals(1, parsed.get("schemaVersion").getAsInt());
        assertEquals("pc-exporter", parsed.get("producer").getAsString());
        assertEquals("0.2.0", parsed.get("producerVersion").getAsString());
        assertEquals("cobblemon-client-pc", parsed.get("source").getAsString());
        assertEquals("complete-synchronized-store", parsed.get("completeness").getAsString());
        assertEquals("zero-based-store", parsed.get("boxIndexing").getAsString());
        assertEquals("zero-based-box", parsed.get("slotIndexing").getAsString());
        assertEquals("1.8.1", parsed.getAsJsonObject("runtime").get("cobblemon").getAsString());
        assertEquals("11111111-2222-3333-4444-555555555555",
                parsed.getAsJsonObject("store").get("uuid").getAsString());
        assertEquals(2, parsed.getAsJsonObject("store").get("boxCount").getAsInt());
        assertEquals(1, parsed.getAsJsonObject("store").get("observedBoxCount").getAsInt());
        assertEquals(2, parsed.get("boxCount").getAsInt());
        assertEquals(1, parsed.getAsJsonArray("entries").size());

        JsonObject first = parsed.getAsJsonArray("entries").get(0).getAsJsonObject();
        assertEquals(1, first.get("box").getAsInt());
        assertEquals(7, first.get("slot").getAsInt());
        assertEquals("cobblemon:pikachu", first.get("species").getAsString());
        assertEquals("normal", first.get("formId").getAsString());
        assertEquals(12345, first.get("experience").getAsInt());
        assertEquals("cobblemon:adamant", first.get("mintedNature").getAsString());
        assertEquals("static", first.get("ability").getAsString());
        assertEquals(false, first.get("abilityHidden").getAsBoolean());
        assertEquals(0, first.get("abilitySlot").getAsInt());
        assertEquals(2, first.getAsJsonArray("eggGroups").size());
        assertEquals("field", first.getAsJsonArray("eggGroups").get(0).getAsString());
        assertEquals("cobblemon:example_cosmetic", first.get("cosmeticItem").getAsString());
        assertEquals(1, first.get("cosmeticItemCount").getAsInt());
        assertEquals("cobblemon:mark_personality_flustered", first.get("activeMark").getAsString());
        assertEquals(1, first.getAsJsonArray("marks").size());
        JsonObject feature = first.getAsJsonArray("features").get(0).getAsJsonObject();
        assertEquals("season", feature.get("name").getAsString());
        assertEquals("summer", feature.get("value").getAsString());
        assertEquals(1, first.getAsJsonArray("benchedMoves").size());
        assertEquals("tackle",
                first.getAsJsonArray("benchedMoves").get(0).getAsJsonObject().get("name").getAsString());
        assertEquals(true, first.get("tradeable").getAsBoolean());
        assertEquals(31, first.getAsJsonObject("ivs").get("hp").getAsInt());
        assertEquals(1, first.getAsJsonArray("moves").size());
        assertNotNull(first.get("uuid"));
        assertTrue(first.has("heldItem"));
    }

    @Test
    void distinguishesNullEmptyAndZero() {
        PcSnapshot snapshot = sampleSnapshot();
        PcEntry entry = snapshot.entries.get(0);
        entry.nickname = null;          // absent
        entry.moves = List.of();        // empty list
        entry.dmaxLevel = 0;            // real zero
        entry.abilityHidden = null;     // unknown (e.g. forced ability)
        entry.abilitySlot = null;       // unknown
        entry.eggGroups = List.of();    // known to be none
        entry.mintedNature = null;      // no mint
        entry.cosmeticItem = null;      // no cosmetic item
        entry.activeMark = null;        // no active mark
        entry.marks = List.of();        // no marks
        entry.features = List.of();     // no features
        entry.benchedMoves = List.of(); // no benched moves
        JsonObject parsed = new Gson().fromJson(SnapshotJson.toJson(snapshot), JsonObject.class);
        JsonObject first = parsed.getAsJsonArray("entries").get(0).getAsJsonObject();
        assertTrue(first.get("nickname").isJsonNull());
        assertEquals(0, first.getAsJsonArray("moves").size());
        assertEquals(0, first.get("dmaxLevel").getAsInt());
        assertTrue(first.get("abilityHidden").isJsonNull());
        assertTrue(first.get("abilitySlot").isJsonNull());
        assertEquals(0, first.getAsJsonArray("eggGroups").size());
        assertTrue(first.get("mintedNature").isJsonNull());
        assertTrue(first.get("cosmeticItem").isJsonNull());
        assertTrue(first.get("activeMark").isJsonNull());
        assertEquals(0, first.getAsJsonArray("marks").size());
        assertEquals(0, first.getAsJsonArray("features").size());
        assertEquals(0, first.getAsJsonArray("benchedMoves").size());
    }

    @Test
    void emptySnapshotIsValidJson() {
        PcSnapshot snapshot = new PcSnapshot();
        snapshot.generatedAt = "2026-10-03T00:00:00Z";
        snapshot.source = "cobblemon-client-pc";
        snapshot.boxCount = 0;
        JsonObject parsed = new Gson().fromJson(SnapshotJson.toJson(snapshot), JsonObject.class);
        assertEquals(PcSnapshot.SCHEMA_VERSION, parsed.get("schemaVersion").getAsInt());
        assertEquals(0, parsed.getAsJsonArray("entries").size());
    }
}
