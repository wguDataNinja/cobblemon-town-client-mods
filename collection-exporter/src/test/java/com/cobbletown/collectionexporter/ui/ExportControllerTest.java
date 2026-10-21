package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.TestFixtures;
import com.cobbletown.collectionexporter.classify.ClassificationCatalogLoader;
import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.clipboard.ClipboardService;
import com.cobbletown.collectionexporter.report.ReportCategory;
import com.cobbletown.collectionexporter.report.ReportEngine;
import com.cobbletown.collectionexporter.report.ReportRequest;
import com.cobbletown.collectionexporter.report.ReportTextFormatter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportControllerTest {
    @Test
    void refreshAndCopyUseInjectedDependencies() {
        AtomicReference<String> copied = new AtomicReference<>();
        ClipboardService clipboard = text -> {
            copied.set(text);
            return ClipboardResult.success(text.length());
        };
        ExportController controller = new ExportController(
                TestFixtures::exampleInventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                clipboard);

        controller.refresh(ReportRequest.of(ReportCategory.SKINS));
        assertTrue(controller.lastText().contains("Skins"));
        assertEquals(30, controller.lastReport().summary().skinInstances());

        ClipboardResult result = controller.copyLastReport();
        assertTrue(result.success());
        assertEquals(controller.lastText(), copied.get());
    }

    @Test
    void rebuildUsesCachedObservationForFilterChanges() {
        java.util.concurrent.atomic.AtomicInteger reads = new java.util.concurrent.atomic.AtomicInteger();
        ExportController controller = new ExportController(
                () -> {
                    reads.incrementAndGet();
                    return TestFixtures.exampleInventory();
                },
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                text -> ClipboardResult.success(text.length()));
        controller.refresh(ReportRequest.of(ReportCategory.ALL));
        assertEquals(1, reads.get());
        controller.rebuild(ReportRequest.of(ReportCategory.SKINS));
        assertEquals(1, reads.get());
        assertEquals(30, controller.lastReport().summary().skinInstances());
    }

    @Test
    void copyBeforeRefreshFailsClosed() {
        ExportController controller = new ExportController(
                TestFixtures::exampleInventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                text -> ClipboardResult.success(text.length()));
        assertFalse(controller.copyLastReport().success());
    }

    @Test
    void screenModelKeepsFilterStateAndReport() {
        ExportController controller = new ExportController(
                TestFixtures::exampleInventory,
                new ReportEngine(ClassificationCatalogLoader.loadDefault()),
                new ReportTextFormatter(),
                text -> ClipboardResult.success(text.length()));
        ExportScreenModel model = new ExportScreenModel();
        assertTrue(model.isSelected(ReportCategory.SKINS));
        assertTrue(model.isSelected(ReportCategory.LEGEND_MYTH));
        assertTrue(model.isSelected(ReportCategory.SHINY));
        assertFalse(model.isSelected(ReportCategory.ALL));
        model.refresh(controller);
        assertEquals(30, model.report().summary().skinInstances());
        assertTrue(model.completeness().isComplete());
        assertFalse(model.reportText().isBlank());
    }
}
