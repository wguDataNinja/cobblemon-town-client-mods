package com.cobbletown.gamemenudashboard.claims;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Local, opt-in display preferences for the future Claims cache and renderers.
 *
 * <p>These flags do not request claim data, send a command, or enable the disposable
 * synthetic renderer. A future renderer may draw only geometry that the player has
 * deliberately loaded into the local owned-claims cache.</p>
 */
public final class ClaimsDisplaySettings {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private boolean boundariesVisible;
    private boolean minimapVisible;

    private Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("game-menu-dashboard/settings.json");
    }

    public void load() {
        try {
            if (!Files.exists(path())) return;
            Stored stored = GSON.fromJson(Files.readString(path()), Stored.class);
            if (stored != null && stored.schemaVersion == 1) {
                boundariesVisible = stored.showClaimBoundaries;
                minimapVisible = stored.showClaimsOnMinimap;
            }
        } catch (Exception ignored) {
            // Preferences are optional; a damaged local file must not block the pause menu.
        }
    }

    public boolean boundariesVisible() { return boundariesVisible; }
    public boolean minimapVisible() { return minimapVisible; }

    public void toggleBoundaries() { boundariesVisible = !boundariesVisible; save(); }
    public void toggleMinimap() { minimapVisible = !minimapVisible; save(); }

    private void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(new Stored(boundariesVisible, minimapVisible)));
        } catch (Exception ignored) {
            // Local preference failure never changes game or server behavior.
        }
    }

    private static final class Stored {
        int schemaVersion = 1;
        boolean showClaimBoundaries;
        boolean showClaimsOnMinimap;

        Stored(boolean showClaimBoundaries, boolean showClaimsOnMinimap) {
            this.showClaimBoundaries = showClaimBoundaries;
            this.showClaimsOnMinimap = showClaimsOnMinimap;
        }

        @SuppressWarnings("unused")
        Stored() {}
    }
}
