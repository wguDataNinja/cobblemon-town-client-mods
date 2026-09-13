package com.cobbletown.gamemenudashboard;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DashboardScreenLayoutTest {
    @Test void centeredModules_useOneUniformScale_atCompatibilityGuiDimensions() {
        DashboardScreenLayout.Placement placement = DashboardScreenLayout.forScreen(534, 281);
        assertTrue(placement.scale() > 0.80F && placement.scale() < 0.81F);
        assertEquals(30, placement.topY());
        int totalWidth = Math.round((DashboardScreenLayout.MODULE_WIDTH * 2 + 16) * placement.scale());
        assertEquals((534 - totalWidth) / 2, placement.leftX());
        assertEquals(placement.leftX() + Math.round((DashboardScreenLayout.MODULE_WIDTH + 16) * placement.scale()), placement.rightX());
    }

    @Test void broadScreen_keepsTheOriginalModuleScale() {
        DashboardScreenLayout.Placement placement = DashboardScreenLayout.forScreen(1600, 842);
        assertEquals(1.0F, placement.scale());
        assertEquals((1600 - 396) / 2, placement.leftX());
        assertEquals(placement.leftX() + 206, placement.rightX());
    }
}
