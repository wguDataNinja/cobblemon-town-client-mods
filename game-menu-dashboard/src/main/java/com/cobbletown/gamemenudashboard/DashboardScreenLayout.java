package com.cobbletown.gamemenudashboard;

/** Pure geometry for the v1 Swamp-owned screen's two centered dashboard modules. */
final class DashboardScreenLayout {
    static final int MODULE_WIDTH = 190;
    static final int MODULE_HEIGHT = 302;
    private static final int MODULE_GAP = 16;
    private static final int OUTER_MARGIN = 8;
    private static final int TOP_CLEARANCE = 30;

    record Placement(int leftX, int topY, int rightX, float scale) { }

    private DashboardScreenLayout() { }

    static Placement forScreen(int width, int height) {
        float horizontal = Math.max(0.0F, width - OUTER_MARGIN * 2) / (MODULE_WIDTH * 2.0F + MODULE_GAP);
        float vertical = Math.max(0.0F, height - TOP_CLEARANCE - OUTER_MARGIN) / (float) MODULE_HEIGHT;
        float scale = Math.min(1.0F, Math.min(horizontal, vertical));
        int totalWidth = Math.round((MODULE_WIDTH * 2 + MODULE_GAP) * scale);
        int leftX = Math.max(OUTER_MARGIN, (width - totalWidth) / 2);
        int panelHeight = Math.round(MODULE_HEIGHT * scale);
        int topY = Math.max(TOP_CLEARANCE, (height - panelHeight) / 2);
        int rightX = leftX + Math.round((MODULE_WIDTH + MODULE_GAP) * scale);
        return new Placement(leftX, topY, rightX, scale);
    }
}
