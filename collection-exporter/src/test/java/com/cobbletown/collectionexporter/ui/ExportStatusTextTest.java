package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.TestFixtures;
import com.cobbletown.collectionexporter.classify.ClassificationCatalogLoader;
import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import com.cobbletown.collectionexporter.report.ReportCategory;
import com.cobbletown.collectionexporter.report.ReportEngine;
import com.cobbletown.collectionexporter.report.ReportRequest;
import com.cobbletown.collectionexporter.report.ReportResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportStatusTextTest {
    private final ReportEngine engine = new ReportEngine(ClassificationCatalogLoader.loadDefault());

    private ReportResult report(PcInventorySnapshot inventory, ReportCategory category) {
        return engine.build(inventory, new ReportRequest(Set.of(category), true, false));
    }

    @Test
    void completeSnapshotUsesPlayerFacingLoadedState() {
        ExportStatusText.SnapshotStatus state = ExportStatusText.snapshotStatus(
                report(TestFixtures.exampleInventory(), ReportCategory.SKINS), null);
        assertEquals("PC Loaded Successfully", state.headline());
        assertEquals(ExportStatusText.Tone.SUCCESS, state.tone());
        assertEquals(null, state.detail());
    }

    @Test
    void partialSnapshotKeepsAccurateWarning() {
        ExportStatusText.SnapshotStatus state = ExportStatusText.snapshotStatus(
                report(TestFixtures.partialInventory(), ReportCategory.SKINS), null);
        assertEquals("PC Loaded Partially", state.headline());
        assertEquals(ExportStatusText.Tone.WARNING, state.tone());
        assertTrue(state.detail().contains("ownership"));
    }

    @Test
    void unavailableSnapshotIsAnErrorNotAnEmptyCollection() {
        ReportResult report = engine.build(
                PcInventorySnapshot.unavailable("2026-10-20T00:00:00Z", "no store"),
                new ReportRequest(Set.of(ReportCategory.ALL), true, false));
        ExportStatusText.SnapshotStatus state = ExportStatusText.snapshotStatus(report, null);
        assertEquals("PC Not Available", state.headline());
        assertEquals(ExportStatusText.Tone.ERROR, state.tone());
        assertTrue(state.detail().contains("Open your PC"));
    }

    @Test
    void ambiguousStoresAreAnError() {
        PcInventorySnapshot snapshot = new PcInventorySnapshot(
                "2026-10-20T00:00:00Z",
                "cobblemon-client-pc",
                null,
                com.cobbletown.collectionexporter.data.InventoryCompleteness.AMBIGUOUS_MULTIPLE_STORES,
                0,
                0,
                List.of(),
                List.of("multiple stores"));
        ReportResult report = engine.build(snapshot,
                new ReportRequest(Set.of(ReportCategory.ALL), true, false));
        ExportStatusText.SnapshotStatus state = ExportStatusText.snapshotStatus(report, null);
        assertEquals("PC Not Loaded", state.headline());
        assertEquals(ExportStatusText.Tone.ERROR, state.tone());
        assertTrue(state.detail().contains("Multiple PC stores"));
    }

    @Test
    void normalLoadHasNoRedundantReadyMessage() {
        ReportResult report = report(TestFixtures.exampleInventory(), ReportCategory.SKINS);
        assertEquals(null, ExportStatusText.afterLoad(report, null, true));
    }

    @Test
    void noSelectedCategoryIsAWarning() {
        ReportResult report = report(TestFixtures.exampleInventory(), ReportCategory.SKINS);
        ExportStatusText.Message message = ExportStatusText.afterLoad(report, null, false);
        assertEquals(ExportStatusText.Tone.WARNING, message.tone());
        assertTrue(message.text().contains("Select at least one"));
    }

    @Test
    void selectedLineCountsPokemonOnce() {
        ReportResult report = engine.build(TestFixtures.exampleInventory(),
                new ReportRequest(Set.of(ReportCategory.SKINS, ReportCategory.LEGEND_MYTH,
                        ReportCategory.SHINY), true, false));
        assertEquals("32 Pokémon selected", ExportStatusText.selectedLine(report));
    }

    @Test
    void unresolvedLineNamesPokemonAndKeepsCountWhenTruncated() {
        ReportResult report = report(TestFixtures.unresolvedShinyInventory(), ReportCategory.SHINY);
        assertTrue(ExportStatusText.unresolvedLine(report, 200, String::length)
                .contains("Unresolved: 1 (Gyarados)"));

        String truncated = ExportStatusText.unresolvedLine(report, 20, String::length);
        assertTrue(truncated.startsWith("Unresolved: 1"));
        assertTrue(truncated.length() <= 20);
        assertTrue(truncated.contains("..."));
        assertFalse(truncated.contains("Gyarados)"));
    }

    @Test
    void customSpeciesLineIsSeparateFromUnresolved() {
        ReportResult report = report(TestFixtures.customSpeciesInventory(), ReportCategory.ALL);
        assertEquals("Custom/unknown species: 1", ExportStatusText.customSpeciesLine(report));
        assertEquals("", ExportStatusText.unresolvedLine(report, 200, String::length));
    }

    @Test
    void clipboardFailureIsAnError() {
        ReportResult report = report(TestFixtures.exampleInventory(), ReportCategory.SKINS);
        ExportStatusText.Message message = ExportStatusText.afterCopy(
                ClipboardResult.failure("clipboard write failed"), report);
        assertEquals(ExportStatusText.Tone.ERROR, message.tone());
        assertTrue(message.text().contains("clipboard write failed"));
    }

    @Test
    void copyUsesPokemonTerminology() {
        ReportResult report = report(TestFixtures.exampleInventory(), ReportCategory.SKINS);
        ExportStatusText.Message message = ExportStatusText.afterCopy(
                ClipboardResult.success(10), report);
        assertEquals("Copied 30 Pokémon to the clipboard.", message.text());
        assertFalse(message.text().contains("instances"));
    }
}
