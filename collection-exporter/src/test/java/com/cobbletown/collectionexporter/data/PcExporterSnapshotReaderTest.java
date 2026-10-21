package com.cobbletown.collectionexporter.data;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PcExporterSnapshotReaderTest {
    @Test
    void mapsSnapshotFieldsWithoutMinecraft() {
        String json = """
                {
                  "schemaVersion": 1,
                  "producer": "pc-exporter",
                  "producerVersion": "0.2.0",
                  "source": "cobblemon-client-pc",
                  "generatedAt": "2026-01-01T00:00:00Z",
                  "completeness": "complete-synchronized-store",
                  "boxCount": 150,
                  "store": {"uuid": "store", "observedBoxCount": 1},
                  "entries": [{
                    "uuid": "00000000-0000-0000-0000-000000000001",
                    "box": 3, "slot": 4,
                    "species": "cobblemon:snorlax", "speciesName": "Snorlax",
                    "dexNumber": 143, "form": "Normal", "formId": "normal",
                    "aspects": ["male", "easter2026"], "shiny": false,
                    "level": 50, "gender": "male", "nickname": "Sleepy"
                  }]
                }
                """;
        PcInventorySnapshot snapshot = new PcExporterSnapshotReader().read(new StringReader(json));
        assertEquals("2026-01-01T00:00:00Z", snapshot.capturedAt());
        assertEquals(InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE, snapshot.completeness());
        assertEquals(150, snapshot.boxCount());
        assertEquals(1, snapshot.entries().size());
        PokemonRecord record = snapshot.entries().get(0);
        assertEquals(3, record.box());
        assertEquals(4, record.slot());
        assertEquals("cobblemon:snorlax", record.speciesId());
        assertEquals("normal", record.formId());
        assertEquals(2, record.aspects().size());
        assertFalse(record.shiny());
        assertEquals("Sleepy", record.nickname());
        assertTrue(snapshot.diagnostics().isEmpty());
    }

    @Test
    void preservesEntriesWithMissingFieldsAndReportsDiagnostics() {
        String json = """
                {
                  "schemaVersion": 1,
                  "producer": "pc-exporter",
                  "source": "cobblemon-client-pc",
                  "entries": [{"uuid": "not-a-uuid"}]
                }
                """;
        PcInventorySnapshot snapshot = new PcExporterSnapshotReader().read(new StringReader(json));
        assertEquals(1, snapshot.entries().size());
        assertNull(snapshot.entries().get(0).uuid());
        assertEquals("unknown", snapshot.entries().get(0).speciesId());
        assertTrue(snapshot.diagnostics().size() >= 5);
        assertEquals(InventoryCompleteness.PARTIAL_INCOMPLETE_STORE, snapshot.completeness());
    }

    @Test
    void reportsDuplicateUuidsWithoutDroppingEntries() {
        String json = """
                {
                  "schemaVersion": 1, "producer": "pc-exporter", "source": "cobblemon-client-pc",
                  "entries": [
                    {"uuid": "00000000-0000-0000-0000-000000000001", "species": "cobblemon:a"},
                    {"uuid": "00000000-0000-0000-0000-000000000001", "species": "cobblemon:b"}
                  ]
                }
                """;
        PcInventorySnapshot snapshot = new PcExporterSnapshotReader().read(new StringReader(json));
        assertEquals(2, snapshot.entries().size());
        assertTrue(snapshot.diagnostics().stream().anyMatch(line -> line.contains("duplicate uuid")));
    }

    @Test
    void rejectsUnsupportedSchemaOrProducer() {
        assertThrows(PcExporterSnapshotReader.SnapshotException.class, () ->
                new PcExporterSnapshotReader().read(new StringReader("{\"schemaVersion\":2}")));
        assertThrows(PcExporterSnapshotReader.SnapshotException.class, () ->
                new PcExporterSnapshotReader().read(new StringReader(
                        "{\"schemaVersion\":1,\"producer\":\"other\",\"source\":\"cobblemon-client-pc\",\"entries\":[]}")));
    }
}
