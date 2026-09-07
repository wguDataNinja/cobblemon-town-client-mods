package com.cobbletown.gamemenudashboard;

import com.cobbletown.gamemenudashboard.homes.HomeListParser;
import com.cobbletown.gamemenudashboard.homes.HomeSnapshot;
import com.cobbletown.gamemenudashboard.homes.HomesController;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** The single-screen Homes workspace beside untouched vanilla Game Menu controls. */
final class GameMenuDashboardPanel {
    private static final String FOOTER = "Cobblemon-Town Game Menu Dashboard v" + FabricLoader.getInstance()
        .getModContainer("game-menu-dashboard").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("?") + " · by SwampDad";
    private static final String[][] WARPS = {{"Arena", "warp arena"}, {"BattleTower", "warp battletower"}, {"Crates", "warp crates"}, {"End", "warp end"}, {"Nether", "warp nether"}, {"Parkour", "warp parkour"}, {"PokeCenter", "warp pokecenter"}, {"PokeMart", "warp pokemart"}, {"TownArena", "warp townarena"}};
    private static final Map<Screen, GameMenuDashboardPanel> PANELS = new IdentityHashMap<>();
    private final Screen screen;
    private final Layout layout;
    private final List<ClickableWidget> widgets = new ArrayList<>();
    private long renderedVersion = Long.MIN_VALUE;
    private int scrollRow;
    private String deleteCandidate;
    private boolean creating;

    private GameMenuDashboardPanel(Screen screen, Layout layout) { this.screen = screen; this.layout = layout; }

    static void attach(Screen screen, int width, int height) {
        GameMenuDashboardPanel prior = PANELS.remove(screen);
        if (prior != null) prior.removeWidgets();
        GameMenuDashboardPanel panel = new GameMenuDashboardPanel(screen, Layout.forScreen(width, height));
        PANELS.put(screen, panel);
        panel.rebuild();
        ScreenEvents.afterRender(screen).register((current, context, mouseX, mouseY, delta) -> {
            if (PANELS.get(current) == panel) panel.render(context);
        });
        ScreenMouseEvents.afterMouseScroll(screen).register((current, mouseX, mouseY, horizontal, vertical) -> panel.scroll(mouseX, mouseY, vertical));
        ScreenEvents.remove(screen).register(removed -> {
            GameMenuDashboardPanel gone = PANELS.remove(removed);
            if (gone != null) gone.removeWidgets();
        });
    }

    static void tick(Screen current) {
        GameMenuDashboardPanel panel = PANELS.get(current);
        if (panel != null && panel.renderedVersion != GameMenuDashboardClient.HOMES.version()) panel.rebuild();
    }

    private void rebuild() {
        removeWidgets();
        renderedVersion = GameMenuDashboardClient.HOMES.version();
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        int maxScroll = snapshot == null ? 0 : Math.max(0, snapshot.names().size() - layout.rows);
        scrollRow = Math.min(scrollRow, maxScroll);
        String loadLabel = snapshot == null ? "Load Homes" : "⟳";
        ButtonWidget reload = ButtonWidget.builder(Text.literal(loadLabel), button -> { deleteCandidate = null; GameMenuDashboardClient.HOMES.refresh(); })
            .dimensions(snapshot == null ? layout.x : layout.x + layout.width - 22, layout.y + 2, snapshot == null ? 86 : 22, 20).build();
        if (snapshot != null) reload.setTooltip(Tooltip.of(Text.literal("Reload Homes")));
        add(reload);
        if (layout.canShowTravel()) addServerTravel();
        if (snapshot == null) return;
        int y = layout.rowsY;
        int start = scrollRow;
        int end = Math.min(snapshot.names().size(), start + layout.rows);
        for (int index = start; index < end; index++) { addHomeRow(snapshot.names().get(index), y); y += layout.rowHeight; }
        if (end == snapshot.names().size() && snapshot.count() < snapshot.limit()
            && y < layout.rowsY + layout.rows * layout.rowHeight) addCreateRow(y);
    }

    private void addHomeRow(String name, int y) {
        add(ButtonWidget.builder(Text.literal(name), button -> GameMenuDashboardClient.HOMES.go(name)).dimensions(layout.x, y, layout.width - 26, 20).build());
        Text label = name.equals(deleteCandidate)
            ? Text.literal("Confirm").formatted(Formatting.RED)
            : Text.literal("Delete");
        ButtonWidget deleteButton = name.equals(deleteCandidate)
            ? new DangerButton(layout.x + layout.width - 22, y, 22, 20, Text.literal("X"), button -> {
                GameMenuDashboardClient.HOMES.delete(name); deleteCandidate = null; rebuild();
            })
            : ButtonWidget.builder(Text.literal("×").formatted(Formatting.RED), button -> { deleteCandidate = name; rebuild(); }).dimensions(layout.x + layout.width - 22, y, 22, 20).build();
        deleteButton.setTooltip(Tooltip.of(Text.literal(name.equals(deleteCandidate) ? "Confirm delete " + name : "Delete " + name)));
        add(deleteButton);
    }

    private void addCreateRow(int y) {
        if (!creating) {
            ButtonWidget create = ButtonWidget.builder(Text.literal("+ New Home"), button -> { creating = true; rebuild(); })
                .dimensions(layout.x, y, layout.width, 20).build();
            add(create);
            return;
        }
        TextFieldWidget name = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, layout.x, y, 116, 20, Text.literal("Home name"));
        name.setMaxLength(32); name.setPlaceholder(Text.literal("enter Home name")); name.setFocused(true); add(name); screen.setFocused(name);
        add(ButtonWidget.builder(Text.literal("Set Here"), button -> {
            String value = name.getText().strip();
            if (HomeListParser.isSafeName(value)) { creating = false; GameMenuDashboardClient.HOMES.setHome(value); }
        }).dimensions(layout.x + 120, y, 88, 20).build());
    }

    private void add(ClickableWidget widget) { Screens.getButtons(screen).add(widget); widgets.add(widget); }

    private void addServerTravel() {
        int y = layout.travelY;
        add(ButtonWidget.builder(Text.literal("Spawn"), b -> sendTravel("spawn")).dimensions(layout.x, y, 92, 20).build());
        add(ButtonWidget.builder(Text.literal("RTP"), b -> sendTravel("rtp")).dimensions(layout.x + 96, y, 92, 20).build());
        int gridY = y + 43;
        for (int i = 0; i < WARPS.length; i++) {
            int column = i % 3, row = i / 3;
            String label = WARPS[i][0], command = WARPS[i][1];
            add(ButtonWidget.builder(Text.literal(label), b -> sendTravel(command))
                .dimensions(layout.x + column * 64, gridY + row * 22, 60, 18).build());
        }
    }

    private static void sendTravel(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) client.getNetworkHandler().sendChatCommand(command);
    }
    private void removeWidgets() { Screens.getButtons(screen).removeAll(widgets); widgets.clear(); }

    private void scroll(double mouseX, double mouseY, double vertical) {
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        if (snapshot == null || mouseX < layout.x || mouseX > layout.x + layout.width || mouseY < layout.rowsY || mouseY > layout.rowsY + layout.rows * layout.rowHeight) return;
        int next = Math.max(0, Math.min(snapshot.names().size() - layout.rows, scrollRow + (vertical < 0 ? 1 : -1)));
        if (next != scrollRow) { scrollRow = next; rebuild(); }
    }

    private void render(DrawContext context) {
        TextRenderer text = MinecraftClient.getInstance().textRenderer;
        context.fill(layout.x - 4, layout.y - 4, layout.x + layout.width + 4, layout.bottom(), 0xBE101419);
        context.drawBorder(layout.x - 4, layout.y - 4, layout.width + 8, layout.height, 0xFF3C434B);
        context.drawText(text, Text.literal("HOMES"), layout.x, layout.y + 4, 0xFFFFD05B, false);
        context.drawText(text, Text.literal(""), layout.x, layout.y + 20, 0xFFFFFFFF, false);
        HomesController.Truth truth = GameMenuDashboardClient.HOMES.truth();
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        if (snapshot != null) context.drawText(text, Text.literal(snapshot.count() + " / " + snapshot.limit()), layout.x + 78, layout.y + 4, 0xFFFFFFFF, false);
        context.fill(layout.x, layout.y + 27, layout.x + layout.width, layout.y + 28, 0xFF5A6065);
        if (snapshot == null) {
            String message = truth == HomesController.Truth.FAILED ? "Could not load Homes. Try again." : "Home data not loaded yet.";
            context.drawText(text, Text.literal(message), layout.x, layout.rowsY, 0xFFD0D0D0, false);
            if (truth != HomesController.Truth.FAILED) context.drawText(text, Text.literal("Refresh to populate this panel."), layout.x, layout.rowsY + 12, 0xFFA0A0A0, false);
            if (layout.canShowTravel()) renderTravelLabels(context, text);
            renderRightPlaceholder(context, text);
            return;
        }
        if (snapshot.names().size() > layout.rows) renderScrollbar(context, snapshot.names().size());
        if (deleteCandidate != null) context.drawText(text, Text.literal(clip(text, "Confirm delete Home " + deleteCandidate + "?", layout.width)), layout.x, layout.y + 34, 0xFFFF5555, false);
        if (layout.canShowTravel()) renderTravelLabels(context, text);
        renderRightPlaceholder(context, text);
        context.drawText(text, Text.literal(FOOTER), layout.x, layout.bottom() - 10, 0xFFAAAAAA, false);
    }

    private void renderTravelLabels(DrawContext context, TextRenderer text) {
        context.fill(layout.x, layout.travelY - 32, layout.x + layout.width, layout.travelY - 31, 0xFF5A6065);
        context.drawText(text, Text.literal("SERVER TRAVEL"), layout.x, layout.travelY - 20, 0xFFFFD05B, false);
        context.drawText(text, Text.literal("──────── WARPS ────────"), layout.x, layout.travelY + 25, 0xFF9A9A9A, false);
    }

    private void renderScrollbar(DrawContext context, int count) {
        int trackX = layout.x + layout.width - 3, top = layout.rowsY, height = layout.rows * layout.rowHeight;
        int thumb = Math.max(10, height * layout.rows / count);
        int offset = (height - thumb) * scrollRow / Math.max(1, count - layout.rows);
        context.fill(trackX, top, trackX + 2, top + height, 0x665A6065);
        context.fill(trackX, top + offset, trackX + 2, top + offset + thumb, 0xFFA0A0A0);
    }

    private static String clip(TextRenderer text, String value, int width) { return text.trimToWidth(value, width); }

    /** A visually unmistakable second click for destructive server state. */
    private static final class DangerButton extends ButtonWidget {
        DangerButton(int x, int y, int width, int height, Text message, PressAction action) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = isHovered() || isFocused() ? 0xFFC0392B : 0xFF8E2420;
            context.fill(getX(), getY(), getRight(), getBottom(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFFFF7770);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), getX() + getWidth() / 2, getY() + 6, 0xFFFFFFFF);
        }
    }

    private void renderRightPlaceholder(DrawContext context, TextRenderer text) {
        if (layout.rightWidth <= 0) return;
        int x = layout.rightX, y = layout.y, width = layout.rightWidth;
        context.fill(x - 4, y - 4, x + width + 4, layout.bottom(), 0xBE101419);
        context.drawBorder(x - 4, y - 4, width + 8, layout.height, 0xFF3C434B);
        context.drawText(text, Text.literal("CLAIMS / GTS"), x, y + 4, 0xFFAAAAAA, false);
        context.fill(x, y + 27, x + width, y + 28, 0xFF5A6065);
        context.drawCenteredTextWithShadow(text, "Coming soon!", x + width / 2, y + 115, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(text, "Land claims, GTS, and", x + width / 2, y + 137, 0xFFBBBBBB);
        context.drawCenteredTextWithShadow(text, "more tools will appear here", x + width / 2, y + 149, 0xFFBBBBBB);
    }

    private record Layout(int x, int y, int width, int height, int rowsY, int rows, int rowHeight, int travelY, int rightX, int rightWidth) {
        static Layout forScreen(int width, int height) {
            int sideSpace = width / 2 - 114;
            if (width >= 640 && height >= 330) {
                int panelY = 40, panelHeight = height - panelY - 8;
                return new Layout(16, panelY, 190, panelHeight, panelY + 32, 5, 22, panelY + 176, width - 206, 190);
            }
            int panelWidth = Math.max(0, Math.min(190, width / 2 - 114)), panelHeight = Math.min(250, Math.max(112, height - 48)), panelY = 40;
            return new Layout(8, panelY, panelWidth, panelHeight, panelY + 32, 5, 22, Math.min(panelY + 176, panelY + panelHeight - 110), -1000, 0);
        }
        int bottom() { return y + height; }
        boolean canShowTravel() { return width >= 188 && travelY + 109 <= bottom(); }
    }
}
