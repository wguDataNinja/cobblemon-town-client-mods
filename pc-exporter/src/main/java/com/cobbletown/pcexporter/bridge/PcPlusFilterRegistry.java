package com.cobbletown.pcexporter.bridge;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.pc_plus.client.filter.FilterController;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Weak map of the active PC Plus {@link FilterController} per PC screen, populated by
 * {@code FilterControllerMixin} and read by {@link com.cobbletown.pcexporter.ui.PcPlusAnchor}.
 * Referenced only when PC Plus is present.
 */
public final class PcPlusFilterRegistry {
    private static final Map<PCGUI, FilterController> BY_GUI = new WeakHashMap<>();
    private static final Map<FilterController, PCGUI> BY_CONTROLLER = new WeakHashMap<>();

    private PcPlusFilterRegistry() {
    }

    public static synchronized void register(PCGUI pcGui, FilterController controller) {
        if (pcGui != null && controller != null) {
            BY_GUI.put(pcGui, controller);
            BY_CONTROLLER.put(controller, pcGui);
        }
    }

    public static synchronized FilterController get(PCGUI pcGui) {
        return pcGui == null ? null : BY_GUI.get(pcGui);
    }

    public static synchronized PCGUI get(FilterController controller) {
        return controller == null ? null : BY_CONTROLLER.get(controller);
    }
}
