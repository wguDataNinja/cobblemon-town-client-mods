package com.cobbletown.collectionexporter;

import com.cobbletown.collectionexporter.launcher.SwampMenuIntegration;
import net.fabricmc.api.ClientModInitializer;

/** Client entry point: initialize the core and register the single Swamp Mods launcher entry. */
public final class CollectionExporterClient implements ClientModInitializer {
    private static CollectionExporterRuntime runtime;

    @Override
    public void onInitializeClient() {
        try {
            runtime = CollectionExporterRuntime.createDefault();
            SwampMenuIntegration.register(runtime);
            CollectionExporterMod.LOGGER.info(
                    "Collection Exporter initialized (catalog schema {}); Swamp Mods launcher registered",
                    runtime.catalog().schemaVersion());
        } catch (RuntimeException failure) {
            runtime = null;
            CollectionExporterMod.LOGGER.error(
                    "Collection Exporter core disabled: catalog or service initialization failed",
                    failure);
        }
    }

    public static CollectionExporterRuntime runtime() {
        return runtime;
    }
}
