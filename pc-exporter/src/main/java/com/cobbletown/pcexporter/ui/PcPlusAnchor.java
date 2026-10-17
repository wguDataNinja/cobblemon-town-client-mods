package com.cobbletown.pcexporter.ui;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.pc_plus.client.filter.FilterController;
import com.cobblemon.pc_plus.client.filter.PanelButtonWidget;
import com.cobbletown.pcexporter.bridge.PcPlusFilterRegistry;

/**
 * Computes the Export PC control position from PC Plus's own public widget geometry, placing it
 * immediately to the left of the native top-right filter controls (same titlebar row).
 *
 * <p>PC Plus (All Rights Reserved) is not modified or bundled; this reads only its public
 * getters. This class references PC Plus types and is loaded only when PC Plus is present (see
 * {@code PCGUIMixin}), so an absent PC Plus never causes a class-loading failure.
 */
public final class PcPlusAnchor {

    private PcPlusAnchor() {
    }

    /**
     * Returns {@code {x, y, width, height}} for the Export PC control, or {@code null} when PC
     * Plus is not currently driving a filter panel.
     *
     * <p>Anchor = the leftmost <em>visible</em> native titlebar control (reset when the panel has
     * been moved, otherwise the catalog/"filters" button), with the control placed one gap to its
     * left and top-aligned with the native titlebar row.
     */
    public static int[] leftOfFilterControls(PCGUI pcGui) {
        FilterController controller = PcPlusFilterRegistry.get(pcGui);
        if (controller == null) {
            return null;
        }
        PanelButtonWidget catalog = controller.getCatalogButton();
        if (catalog == null) {
            return null;
        }

        int leftmostX = Integer.MAX_VALUE;
        leftmostX = include(leftmostX, catalog);
        leftmostX = include(leftmostX, controller.getResetButton());
        leftmostX = include(leftmostX, controller.getMinimizeButton());
        leftmostX = include(leftmostX, controller.getMaximizeButton());
        if (leftmostX == Integer.MAX_VALUE) {
            return null;
        }

        int x = leftmostX - ExportPcOverlay.GAP - ExportPcOverlay.WIDTH;
        int y = catalog.getY();
        return new int[]{x, y, ExportPcOverlay.WIDTH, catalog.getHeight()};
    }

    private static int include(int leftmostX, PanelButtonWidget widget) {
        if (widget != null && widget.visible) {
            return Math.min(leftmostX, widget.getX());
        }
        return leftmostX;
    }
}
