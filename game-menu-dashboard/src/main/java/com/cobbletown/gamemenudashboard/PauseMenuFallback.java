package com.cobbletown.gamemenudashboard;

import java.util.List;
import java.util.Optional;

/** Small, bounded routing policy for the Essential pause-menu fallback. */
final class PauseMenuFallback {
    private static final int LAUNCHER_WIDTH = 120;
    private static final int LAUNCHER_HEIGHT = 20;

    enum Route { INLINE_DASHBOARD, SWAMP_MENU_LAUNCHER }

    record Bounds(int x, int y, int width, int height) {
        boolean intersects(Bounds other) {
            return x < other.x + other.width && x + width > other.x
                && y < other.y + other.height && y + height > other.y;
        }
    }

    private PauseMenuFallback() { }

    static Route route(boolean essentialPauseControlsPresent) {
        return essentialPauseControlsPresent ? Route.SWAMP_MENU_LAUNCHER : Route.INLINE_DASHBOARD;
    }

    static Optional<Bounds> launcherPlacement(int screenWidth, int screenHeight, List<Bounds> occupied) {
        int x = (screenWidth - LAUNCHER_WIDTH) / 2;
        List<Bounds> candidates = List.of(
            new Bounds(x, Math.max(8, screenHeight / 4 - 16), LAUNCHER_WIDTH, LAUNCHER_HEIGHT),
            new Bounds(x, Math.max(8, screenHeight - LAUNCHER_HEIGHT - 6), LAUNCHER_WIDTH, LAUNCHER_HEIGHT)
        );
        return candidates.stream().filter(candidate -> occupied.stream().noneMatch(candidate::intersects)).findFirst();
    }
}
