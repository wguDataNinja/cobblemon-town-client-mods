package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.TestFixtures;
import com.cobbletown.collectionexporter.classify.ClassificationCatalogLoader;
import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.clipboard.ClipboardService;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import com.cobbletown.collectionexporter.report.ReportEngine;
import com.cobbletown.collectionexporter.report.ReportCategory;
import com.cobbletown.collectionexporter.report.ReportTextFormatter;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportScreenModelTest {
    private ExportScreenModel model(PcInventorySnapshot inventory, ClipboardService clipboard) {
        ExportController controller = new ExportController(
                () -> inventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                clipboard);
        ExportScreenModel model = new ExportScreenModel();
        model.refresh(controller);
        return model;
    }

    @Test
    void approvedDefaultsAreAppliedToTheRequest() {
        ExportScreenModel model = new ExportScreenModel();
        assertEquals(Set.of(ReportCategory.SKINS, ReportCategory.LEGEND_MYTH, ReportCategory.SHINY),
                model.selectedCategories());
        assertFalse(model.request().selects(ReportCategory.ALL));
    }

    @Test
    void selectingAllClearsSpecificCategoriesAndViceVersa() {
        ExportScreenModel model = new ExportScreenModel();
        model.selectCategory(ReportCategory.ALL, true);
        assertEquals(Set.of(ReportCategory.ALL), model.selectedCategories());

        model.selectCategory(ReportCategory.SHINY, true);
        assertEquals(Set.of(ReportCategory.SHINY), model.selectedCategories());

        model.selectCategory(ReportCategory.SHINY, false);
        assertTrue(model.selectedCategories().isEmpty());
    }

    @Test
    void resetRestoresApprovedDefaults() {
        ExportScreenModel model = new ExportScreenModel();
        model.selectCategory(ReportCategory.ALL, true);
        model.resetFilters();
        assertEquals(Set.of(ReportCategory.SKINS, ReportCategory.LEGEND_MYTH, ReportCategory.SHINY),
                model.selectedCategories());
    }

    @Test
    void defaultUnionIncludesSkinsLegendAndShinyOnce() {
        ExportScreenModel model = model(TestFixtures.exampleInventory(),
                text -> ClipboardResult.success(text.length()));
        assertEquals(32, model.report().summary().includedInstances());
        assertEquals(30, model.report().summary().skinInstances());
        assertEquals(1, model.report().legendMyth().size());
        assertEquals(1, model.report().shiny().size());
        assertEquals("Pikachu", model.report().shiny().get(0).displayName());
    }

    @Test
    void categoryCountsUseTheWholeInventoryAndDoNotChangeWithFilters() {
        ExportScreenModel model = model(TestFixtures.exampleInventory(),
                text -> ClipboardResult.success(text.length()));
        assertEquals(37, model.categoryCount(ReportCategory.ALL));
        assertEquals(30, model.categoryCount(ReportCategory.SKINS));
        assertEquals(3, model.categoryCount(ReportCategory.LEGEND_MYTH));
        assertEquals(14, model.categoryCount(ReportCategory.SHINY));

        model.selectCategory(ReportCategory.ALL, true);
        assertEquals(37, model.categoryCount(ReportCategory.ALL));
        assertEquals(30, model.categoryCount(ReportCategory.SKINS));
        assertEquals(3, model.categoryCount(ReportCategory.LEGEND_MYTH));
        assertEquals(14, model.categoryCount(ReportCategory.SHINY));
    }

    @Test
    void unresolvedNamesAreAvailableToTheScreen() {
        ExportScreenModel model = model(TestFixtures.unresolvedShinyInventory(),
                text -> ClipboardResult.success(text.length()));
        assertEquals(1, model.unresolved().size());
        assertEquals("Gyarados", model.unresolved().get(0).displayName());
    }

    @Test
    void partialInventoryIsNeverReportedAsComplete() {
        ExportScreenModel model = model(TestFixtures.partialInventory(),
                text -> ClipboardResult.success(text.length()));
        assertEquals(InventoryCompleteness.PARTIAL_SINGLE_STORE_FALLBACK, model.completeness());
        ExportStatusText.SnapshotStatus status = ExportStatusText.snapshotStatus(
                model.report(), null);
        assertEquals(ExportStatusText.Tone.WARNING, status.tone());
        assertTrue(status.headline().contains("Partially"));
    }

    @Test
    void emptyCollectionProducesAnExplicitEmptyState() {
        ExportScreenModel model = model(TestFixtures.emptyInventory(),
                text -> ClipboardResult.success(text.length()));
        assertTrue(model.report().isEmpty());
        ExportStatusText.Message message = ExportStatusText.afterLoad(
                model.report(), null, true);
        assertEquals(ExportStatusText.Tone.MUTED, message.tone());
        assertTrue(message.text().contains("No Pok\u00e9mon matched"));
    }

    @Test
    void copyUsesTheGeneratedReportAndReportsSuccess() {
        AtomicReference<String> copied = new AtomicReference<>();
        ExportController controller = new ExportController(
                TestFixtures::exampleInventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                text -> {
                    copied.set(text);
                    return ClipboardResult.success(text.length());
                });
        ExportScreenModel model = new ExportScreenModel();
        model.refresh(controller);
        ClipboardResult result = model.copy(controller);

        assertTrue(result.success());
        assertTrue(copied.get().contains("COBBLEMON TOWN"));
        assertTrue(copied.get().contains("CUSTOM SKINS"));
        assertTrue(copied.get().contains("SUMMARY"));
        ExportStatusText.Message message = ExportStatusText.afterCopy(result, model.report());
        assertEquals(ExportStatusText.Tone.SUCCESS, message.tone());
    }

    @Test
    void clipboardFailureIsExplicit() {
        ExportScreenModel model = model(TestFixtures.exampleInventory(),
                text -> ClipboardResult.failure("test failure"));
        ExportController controller = new ExportController(
                TestFixtures::exampleInventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                text -> ClipboardResult.failure("test failure"));
        ClipboardResult result = model.copy(controller);
        assertFalse(result.success());
        ExportStatusText.Message message = ExportStatusText.afterCopy(result, model.report());
        assertEquals(ExportStatusText.Tone.ERROR, message.tone());
        assertTrue(message.text().contains("Could not copy"));
    }
}
