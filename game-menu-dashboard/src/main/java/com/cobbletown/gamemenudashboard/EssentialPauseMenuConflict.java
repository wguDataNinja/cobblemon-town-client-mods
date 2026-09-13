package com.cobbletown.gamemenudashboard;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;

/**
 * Observes the actual standard widgets Essential mounted on this pause screen. No
 * Essential configuration, API, reflection, or class linkage is used.
 */
final class EssentialPauseMenuConflict {
    private static final String ESSENTIAL_PROXY_PREFIX = "gg.essential.gui.proxies.";

    private EssentialPauseMenuConflict() { }

    static boolean isPresent(Screen screen) {
        return Screens.getButtons(screen).stream()
            .anyMatch(widget -> widget.getClass().getName().startsWith(ESSENTIAL_PROXY_PREFIX));
    }
}
