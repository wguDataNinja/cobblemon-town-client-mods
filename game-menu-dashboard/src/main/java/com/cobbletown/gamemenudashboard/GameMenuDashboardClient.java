package com.cobbletown.gamemenudashboard;

import com.cobbletown.gamemenudashboard.homes.HomeNotesStore;
import com.cobbletown.gamemenudashboard.homes.HomeSnapshot;
import com.cobbletown.gamemenudashboard.homes.HomesController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;

/** Client-only entry point. The Game Menu remains the operational dashboard surface. */
public final class GameMenuDashboardClient implements ClientModInitializer {
    public static final HomesController HOMES = new HomesController();
    public static final HomeNotesStore NOTES = new HomeNotesStore();
    private HomeSnapshot lastPersistedSnapshot;

    @Override
    public void onInitializeClient() {
        NOTES.load();
        HOMES.restore(NOTES.cachedHomes());
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            long before = HOMES.version();
            HOMES.onGameMessage(message, overlay);
            if (HOMES.version() != before && HOMES.truth() == HomesController.Truth.FRESH) {
                NOTES.setCachedHomes(HOMES.snapshot());
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            HOMES.tick();
            if (HOMES.snapshot() != null && HOMES.snapshot() != lastPersistedSnapshot) {
                NOTES.setCachedHomes(HOMES.snapshot());
                lastPersistedSnapshot = HOMES.snapshot();
            }
            Screen current = client.currentScreen;
            if (current != null) GameMenuDashboardPanel.tick(current);
        });
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (screen instanceof GameMenuScreen) GameMenuDashboardPanel.attach(screen, width, height);
        });
    }
}
