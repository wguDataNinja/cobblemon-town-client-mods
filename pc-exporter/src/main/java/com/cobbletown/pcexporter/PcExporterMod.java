package com.cobbletown.pcexporter;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC Exporter client entrypoint. Read-only: no networking, no mutation, no render work.
 */
public final class PcExporterMod implements ClientModInitializer {
    public static final String MOD_ID = "pc-exporter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("PC Exporter initialized (read-only PC snapshot tool).");
    }
}
