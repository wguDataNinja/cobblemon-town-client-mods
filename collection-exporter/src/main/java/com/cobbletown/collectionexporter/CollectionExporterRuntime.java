package com.cobbletown.collectionexporter;

import com.cobbletown.collectionexporter.classify.ClassificationCatalog;
import com.cobbletown.collectionexporter.classify.ClassificationCatalogLoader;
import com.cobbletown.collectionexporter.clipboard.ClipboardService;
import com.cobbletown.collectionexporter.clipboard.MinecraftClipboardService;
import com.cobbletown.collectionexporter.data.CobblemonPcInventoryReader;
import com.cobbletown.collectionexporter.data.PcInventoryReader;
import com.cobbletown.collectionexporter.report.ReportEngine;
import com.cobbletown.collectionexporter.report.ReportTextFormatter;
import com.cobbletown.collectionexporter.ui.ExportController;

/**
 * Composition root for the UI-independent core. A future owner-approved screen can use the
 * controller and catalog without reimplementing data access or classification.
 */
public final class CollectionExporterRuntime {
    private final ClassificationCatalog catalog;
    private final ExportController controller;

    private CollectionExporterRuntime(ClassificationCatalog catalog, ExportController controller) {
        this.catalog = catalog;
        this.controller = controller;
    }

    public static CollectionExporterRuntime createDefault() {
        ClassificationCatalog catalog = ClassificationCatalogLoader.loadDefault();
        PcInventoryReader reader = new CobblemonPcInventoryReader();
        ClipboardService clipboard = new MinecraftClipboardService();
        ExportController controller = new ExportController(
                reader,
                new ReportEngine(catalog),
                new ReportTextFormatter(),
                clipboard);
        return new CollectionExporterRuntime(catalog, controller);
    }

    public ClassificationCatalog catalog() {
        return catalog;
    }

    public ExportController controller() {
        return controller;
    }
}
