package com.cobbletown.gamemenudashboard;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Public-facing dashboard identity. The release version comes from fabric.mod.json via
 * Gradle's single {@code mod_version} source; never duplicate a release number here.
 */
public final class DashboardBranding {
    public static final String MOD_ID = "game-menu-dashboard";
    public static final String FOOTER_PREFIX = "Cobblemon-Town Game Menu Dashboard v";
    public static final String AUTHOR = "SwampDad";

    private DashboardBranding() { }

    public static String footerLineOne() {
        return FOOTER_PREFIX + version();
    }

    public static String footerLineTwo() {
        return "by " + AUTHOR;
    }

    /** Temporary, conspicuous verification marker for private development installs. */
    public static String developmentBuildMarker() {
        return "DEV BUILD " + version() + " · HERE IN LIST";
    }

    private static String version() {
        String version = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
        return version;
    }
}
