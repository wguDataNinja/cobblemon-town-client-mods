package com.cobbletown.gamemenudashboard;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PauseMenuFallbackTest {
    @Test void onlyActualEssentialPauseControls_selectTheLauncherRoute() {
        assertEquals(PauseMenuFallback.Route.INLINE_DASHBOARD, PauseMenuFallback.route(false));
        assertEquals(PauseMenuFallback.Route.SWAMP_MENU_LAUNCHER, PauseMenuFallback.route(true));
    }

    @Test void defaultCompatibilityGeometry_placesLauncherAboveVanillaControls() {
        List<PauseMenuFallback.Bounds> occupied = List.of(
            new PauseMenuFallback.Bounds(165, 78, 204, 20),
            new PauseMenuFallback.Bounds(165, 102, 98, 20),
            new PauseMenuFallback.Bounds(271, 102, 98, 20),
            new PauseMenuFallback.Bounds(421, 78, 80, 20),
            new PauseMenuFallback.Bounds(22, 61, 120, 120)
        );
        PauseMenuFallback.Bounds launcher = PauseMenuFallback.launcherPlacement(534, 281, occupied).orElseThrow();
        assertEquals(207, launcher.x());
        assertEquals(54, launcher.y());
        assertTrue(occupied.stream().noneMatch(launcher::intersects));
    }

    @Test void launcher_usesItsSecondSafeCandidate_whenTheUpperGapIsOccupied() {
        PauseMenuFallback.Bounds upper = new PauseMenuFallback.Bounds(207, 54, 120, 20);
        PauseMenuFallback.Bounds launcher = PauseMenuFallback.launcherPlacement(534, 281, List.of(upper)).orElseThrow();
        assertFalse(launcher.intersects(upper));
        assertEquals(255, launcher.y());
    }
}
