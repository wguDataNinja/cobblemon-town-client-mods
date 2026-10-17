package com.cobbletown.pcexporter.ui;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.pc_plus.client.filter.FilterController;
import com.cobblemon.pc_plus.client.filter.PanelButtonWidget;
import kotlin.Unit;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The Export PC control, implemented as a real PC Plus {@link PanelButtonWidget} so it renders
 * with PC Plus's own border/fill/hover/text-fitting code and matches the native titlebar controls
 * exactly. PC Plus is not modified or bundled; only its public widget is instantiated.
 *
 * <p>Rendering happens inside PC Plus's {@code FilterController.renderOnTop} overlay pass (see
 * {@code FilterControllerMixin}); clicks are routed through PC Plus's {@code beginTerminalDrag}
 * so the click is consumed before a panel drag begins. The widget is not added to the screen's
 * child list, so it is neither hidden behind the panel nor re-routed through vanilla input.
 */
public final class ExportPcOverlay {
    /** Comfortably fits the "Export" label under PC Plus's scaled-to-fit text. */
    public static final int WIDTH = 54;
    public static final int GAP = 4;

    private static final Map<FilterController, PanelButtonWidget> WIDGETS = new WeakHashMap<>();

    private ExportPcOverlay() {
    }

    /** Positions and draws the native button inside PC Plus's raised titlebar layer. */
    public static void render(FilterController controller, PCGUI pcGui,
                              DrawContext context, int mouseX, int mouseY, float delta) {
        int[] box = PcPlusAnchor.leftOfFilterControls(pcGui);
        if (box == null) {
            return;
        }
        PanelButtonWidget widget = widgetFor(controller, pcGui);
        widget.setDimensions(WIDTH, box[3]);
        widget.setX(box[0]);
        widget.setY(box[1]);
        widget.render(context, mouseX, mouseY, delta);
    }

    /**
     * Routes a click to the native widget (which fires its {@code onPress} — the single export
     * action). PC Plus supplies internal coordinates to this path, matching {@link #render}.
     * Returns true when the click was on the control.
     */
    public static boolean click(FilterController controller, PCGUI pcGui,
                                double mouseX, double mouseY, int button) {
        PanelButtonWidget widget = WIDGETS.get(controller);
        return widget != null && widget.mouseClicked(mouseX, mouseY, button);
    }

    private static PanelButtonWidget widgetFor(FilterController controller, PCGUI pcGui) {
        PanelButtonWidget existing = WIDGETS.get(controller);
        if (existing != null) {
            return existing;
        }
        PanelButtonWidget widget = new PanelButtonWidget(
                0, 0, WIDTH, 10,
                () -> Text.literal("Export"),
                () -> {
                    ExportController.onExport(pcGui);
                    return Unit.INSTANCE;
                },
                () -> Boolean.FALSE,
                () -> Boolean.TRUE,
                null,
                null,
                null,
                () -> 0,
                null);
        WIDGETS.put(controller, widget);
        return widget;
    }
}
