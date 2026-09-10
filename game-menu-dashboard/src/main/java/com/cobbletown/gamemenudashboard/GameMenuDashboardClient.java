package com.cobbletown.gamemenudashboard;

import com.cobbletown.gamemenudashboard.claims.ClaimsStyleComparisonHarness;
import com.cobbletown.gamemenudashboard.claims.ClaimsDisplaySettings;
import com.cobbletown.gamemenudashboard.claims.ClaimsController;
import com.cobbletown.gamemenudashboard.claims.ClaimsWorldRenderer;
import com.cobbletown.gamemenudashboard.claims.ClaimsMinimapRenderer;
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
    /** Private, disposable visual-comparison control; it has no server data path. */
    public static final ClaimsStyleComparisonHarness CLAIMS_STYLE_LAB = new ClaimsStyleComparisonHarness();
    /** Local-only, opt-in Claims display preferences. Both begin disabled. */
    public static final ClaimsDisplaySettings CLAIMS_DISPLAY = new ClaimsDisplaySettings();
    public static final ClaimsController CLAIMS = new ClaimsController();
    private static final ClaimsWorldRenderer CLAIMS_WORLD_RENDERER = new ClaimsWorldRenderer();
    private static final ClaimsMinimapRenderer CLAIMS_MINIMAP_RENDERER = new ClaimsMinimapRenderer();
    private HomeSnapshot lastPersistedSnapshot;

    @Override
    public void onInitializeClient() {
        CLAIMS_STYLE_LAB.register();
        CLAIMS_WORLD_RENDERER.register();
        CLAIMS_DISPLAY.load();
        CLAIMS.restore();
        NOTES.load();
        HOMES.restore(NOTES.cachedHomes());
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            long before = HOMES.version();
            HOMES.onGameMessage(message, overlay);
            CLAIMS.onGameMessage(message, overlay);
            if (HOMES.version() != before && HOMES.truth() == HomesController.Truth.FRESH) {
                NOTES.setCachedHomes(HOMES.snapshot());
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            CLAIMS_STYLE_LAB.tick(client);
            CLAIMS.tick();
            CLAIMS_MINIMAP_RENDERER.tick();
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
