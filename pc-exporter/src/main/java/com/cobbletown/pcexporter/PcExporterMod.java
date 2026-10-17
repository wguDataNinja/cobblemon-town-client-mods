package com.cobbletown.pcexporter;

import com.cobbletown.pcexporter.ui.ExportCommands;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC Exporter client entrypoint. Read-only: no networking, no mutation.
 */
public final class PcExporterMod implements ClientModInitializer {
    public static final String MOD_ID = "pc-exporter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ExportCommands.register();
        LOGGER.info("PC Exporter initialized (read-only PC snapshot tool).");
    }
}
