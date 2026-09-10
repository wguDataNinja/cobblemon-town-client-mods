package com.cobbletown.gamemenudashboard;

import com.cobbletown.gamemenudashboard.homes.HomeListParser;
import com.cobbletown.gamemenudashboard.homes.HomeSnapshot;
import com.cobbletown.gamemenudashboard.homes.HomesController;
import com.cobbletown.gamemenudashboard.claims.ClaimEntry;
import com.cobbletown.gamemenudashboard.claims.ClaimGeometry;
import com.cobbletown.gamemenudashboard.claims.ClaimsController;
import com.cobbletown.gamemenudashboard.claims.ResizePreview;
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

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** The single-screen Homes workspace beside untouched vanilla Game Menu controls. */
final class GameMenuDashboardPanel {
    private static final String[][] WARPS = {{"Arena", "warp arena"}, {"BattleTower", "warp battletower"}, {"Crates", "warp crates"}, {"End", "warp end"}, {"Nether", "warp nether"}, {"Parkour", "warp parkour"}, {"PokeCenter", "warp pokecenter"}, {"PokeMart", "warp pokemart"}, {"TownArena", "warp townarena"}};
    private static final int CLAIM_ROWS_VISIBLE = 5;
    private static final int CLAIM_ROW_Y = 34;
    private static final int CLAIM_ROW_STEP = 24;
    private static final Map<Screen, GameMenuDashboardPanel> PANELS = new IdentityHashMap<>();
    private final Screen screen;
    private final Layout layout;
    private final List<ClickableWidget> widgets = new ArrayList<>();
    private long renderedVersion = Long.MIN_VALUE;
    private long renderedClaimsVersion = Long.MIN_VALUE;
    private int scrollRow;
    private int claimsScrollRow;
    private int trustScrollRow;
    private String deleteCandidate;
    private boolean creating;
    private ClaimWorkspace claimWorkspace = ClaimWorkspace.NONE;
    private RightTab rightTab = RightTab.CLAIMS;
    private String trustPlayer = "";
    private String resizeDirection;
    private String resizeAmount;

    private enum ClaimWorkspace { NONE, RESIZE }
    private enum RightTab { CLAIMS, TRUST }

    private GameMenuDashboardPanel(Screen screen, Layout layout) {
        this.screen = screen; this.layout = layout;
        this.resizeDirection = ResizePreview.direction(); this.resizeAmount = ResizePreview.amount();
    }

    static void attach(Screen screen, int width, int height) {
        GameMenuDashboardPanel prior = PANELS.remove(screen);
        if (prior != null) prior.removeWidgets();
        GameMenuDashboardPanel panel = new GameMenuDashboardPanel(screen, Layout.forScreen(width, height));
        if (!panel.layout.hasRoom()) return;
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
        if (panel != null && (panel.renderedVersion != GameMenuDashboardClient.HOMES.version()
            || panel.renderedClaimsVersion != GameMenuDashboardClient.CLAIMS.version())) panel.rebuild();
    }

    private void rebuild() {
        removeWidgets();
        renderedVersion = GameMenuDashboardClient.HOMES.version();
        renderedClaimsVersion = GameMenuDashboardClient.CLAIMS.version();
        if (GameMenuDashboardClient.CLAIMS.selected() == null) { claimWorkspace = ClaimWorkspace.NONE; rightTab = RightTab.CLAIMS; }
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        int maxScroll = snapshot == null ? 0 : Math.max(0, snapshot.names().size() - layout.rows);
        scrollRow = Math.min(scrollRow, maxScroll);
        boolean homesStale = GameMenuDashboardClient.HOMES.truth() == HomesController.Truth.STALE;
        String loadLabel = snapshot == null ? "Load Homes" : homesStale ? "Refresh" : "⟳";
        int reloadWidth = snapshot == null ? 86 : homesStale ? 64 : layout.reloadSize;
        ButtonWidget reload = homesStale
            ? accentButton(Text.literal(loadLabel), button -> { deleteCandidate = null; GameMenuDashboardClient.HOMES.refresh(); }, layout.x + layout.width - reloadWidth, layout.y + 2, reloadWidth, 20)
            : button(Text.literal(loadLabel), button -> { deleteCandidate = null; GameMenuDashboardClient.HOMES.refresh(); },
                snapshot == null ? layout.x : layout.x + layout.width - reloadWidth, layout.y + 2, reloadWidth, layout.reloadSize);
        reload.setTooltip(Tooltip.of(Text.literal(homesStale ? "Refresh stale Homes data" : "Reload Homes")));
        add(reload);
        if (layout.canShowTravel()) addServerTravel();
        addRightTabs();
        if (rightTab == RightTab.CLAIMS) addClaimsControls(); else addTrustTabControls();
        if (snapshot == null) return;
        int y = layout.rowsY;
        int start = scrollRow;
        int end = Math.min(snapshot.names().size(), start + layout.rows);
        for (int index = start; index < end; index++) { addHomeRow(snapshot.names().get(index), y); y += layout.rowHeight; }
        if (end == snapshot.names().size() && snapshot.count() < snapshot.limit()
            && y < layout.rowsY + layout.rows * layout.rowHeight) addCreateRow(y);
    }

    private void addHomeRow(String name, int y) {
        add(button(Text.literal(name), button -> GameMenuDashboardClient.HOMES.go(name), layout.x, y,
            layout.width - layout.deleteWidth - 4, layout.homeButtonHeight));
        ButtonWidget deleteButton = name.equals(deleteCandidate)
            ? new DangerButton(layout.screenX(layout.x + layout.width - layout.deleteWidth), layout.screenY(y), layout.screenSize(layout.deleteWidth), layout.screenSize(layout.homeButtonHeight), Text.literal("X"), button -> {
                GameMenuDashboardClient.HOMES.delete(name); deleteCandidate = null; rebuild();
            }, layout.scale)
            : button(Text.literal("×").formatted(Formatting.RED), button -> { deleteCandidate = name; rebuild(); },
                layout.x + layout.width - layout.deleteWidth, y, layout.deleteWidth, layout.homeButtonHeight);
        deleteButton.setTooltip(Tooltip.of(Text.literal(name.equals(deleteCandidate) ? "Confirm delete " + name : "Delete " + name)));
        add(deleteButton);
    }

    private void addCreateRow(int y) {
        if (!creating) {
            ButtonWidget create = button(Text.literal("+ New Home"), button -> { creating = true; rebuild(); },
                layout.x, y, layout.width, layout.homeButtonHeight);
            add(create);
            return;
        }
        int nameWidth = 116;
        TextFieldWidget name = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, layout.screenX(layout.x), layout.screenY(y), layout.screenSize(nameWidth), layout.screenSize(layout.homeButtonHeight), Text.literal("Home name"));
        name.setMaxLength(32); name.setPlaceholder(Text.literal("enter Home name")); name.setFocused(true); add(name); screen.setFocused(name);
        add(button(Text.literal("Set Here"), button -> {
            String value = name.getText().strip();
            if (HomeListParser.isSafeName(value)) { creating = false; GameMenuDashboardClient.HOMES.setHome(value); }
        }, layout.x + nameWidth + 4, y, layout.width - nameWidth - 4, layout.homeButtonHeight));
    }

    private void add(ClickableWidget widget) { Screens.getButtons(screen).add(widget); widgets.add(widget); }

    private ButtonWidget button(Text label, ButtonWidget.PressAction action, int x, int y, int width, int height) {
        int screenX = layout.screenX(x), screenY = layout.screenY(y);
        int screenWidth = layout.screenSize(width), screenHeight = layout.screenHeight(height);
        float textScale = fittedTextScale(label, width, layout.scale);
        return textScale < 0.999F
            ? new ScaledButton(screenX, screenY, screenWidth, screenHeight, label, action, textScale)
            : ButtonWidget.builder(label, action).dimensions(screenX, screenY, screenWidth, screenHeight).build();
    }

    private ButtonWidget rightButton(Text label, ButtonWidget.PressAction action, int localX, int y, int width, int height) {
        int screenX = layout.rightScreenX(localX), screenY = layout.screenY(y);
        int screenWidth = layout.rightScreenSize(width), screenHeight = layout.screenHeight(height);
        float textScale = fittedTextScale(label, width, layout.scale);
        return textScale < 0.999F
            ? new ScaledButton(screenX, screenY, screenWidth, screenHeight, label, action, textScale)
            : ButtonWidget.builder(label, action).dimensions(screenX, screenY, screenWidth, screenHeight).build();
    }

    private ButtonWidget accentRightButton(Text label, ButtonWidget.PressAction action, int localX, int y, int width, int height) {
        int screenX = layout.rightScreenX(localX), screenY = layout.screenY(y);
        int screenWidth = layout.rightScreenSize(width), screenHeight = layout.screenHeight(height);
        return new AccentButton(screenX, screenY, screenWidth, screenHeight, label, action, fittedTextScale(label, width, layout.scale));
    }

    private ButtonWidget accentButton(Text label, ButtonWidget.PressAction action, int x, int y, int width, int height) {
        return new AccentButton(layout.screenX(x), layout.screenY(y), layout.screenSize(width), layout.screenHeight(height), label, action, fittedTextScale(label, width, layout.scale));
    }

    private ButtonWidget workspaceButton(Text label, ButtonWidget.PressAction action, WorkspaceLayout workspace, int localX, int localY, int width, int height) {
        int screenX = workspace.screenX() + layout.screenSize(localX), screenY = workspace.screenY() + layout.screenSize(localY);
        int screenWidth = layout.screenSize(width), screenHeight = layout.screenHeight(height);
        float textScale = fittedTextScale(label, width, layout.scale);
        return textScale < 0.999F
            ? new ScaledButton(screenX, screenY, screenWidth, screenHeight, label, action, textScale)
            : ButtonWidget.builder(label, action).dimensions(screenX, screenY, screenWidth, screenHeight).build();
    }

    /** Keeps glyphs proportional and within their logical control padding. */
    private static float fittedTextScale(Text label, int logicalWidth, float panelScale) {
        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(label);
        if (textWidth <= 0) return panelScale;
        return panelScale * Math.min(1.0F, Math.max(0.55F, (logicalWidth - 8.0F) / textWidth));
    }

    private void addServerTravel() {
        int y = layout.travelY;
        ButtonWidget back = button(Text.literal("← Back"), b -> sendTravel("back"),
            layout.x + layout.width - 64, y - 28, 64, 18);
        back.setTooltip(Tooltip.of(Text.literal("Back to previous server destination")));
        add(back);
        int half = (layout.width - layout.travelGap) / 2;
        add(button(Text.literal("Spawn"), b -> sendTravel("spawn"), layout.x, y, half, layout.travelButtonHeight));
        add(button(Text.literal("RTP"), b -> sendTravel("rtp"), layout.x + half + layout.travelGap, y, layout.width - half - layout.travelGap, layout.travelButtonHeight));
        for (int i = 0; i < WARPS.length; i++) {
            int column = i % layout.travelColumns, row = i / layout.travelColumns;
            String label = WARPS[i][0], command = WARPS[i][1];
            add(button(Text.literal(label), b -> sendTravel(command),
                layout.x + column * (layout.travelButtonWidth + layout.travelGap),
                layout.warpsY + row * layout.travelRowStep, layout.travelButtonWidth, layout.travelButtonHeight));
        }
    }

    private void addClaimsControls() {
        ClaimsController controller = GameMenuDashboardClient.CLAIMS;
        boolean initialLoad = controller.claims().isEmpty() && controller.truth() == ClaimsController.Truth.UNLOADED;
        String refresh = initialLoad ? "Load" : "Refresh";
        ButtonWidget reload = initialLoad || controller.truth() == ClaimsController.Truth.STALE
            ? accentRightButton(Text.literal(refresh), button -> controller.refresh(), 128, layout.y + 2, 62, 20)
            : rightButton(Text.literal(refresh), button -> controller.refresh(), 128, layout.y + 2, 62, 20);
        reload.setTooltip(Tooltip.of(Text.literal("Request your owned Claims list from the server")));
        add(reload);

        int maxScroll = Math.max(0, controller.claims().size() - CLAIM_ROWS_VISIBLE);
        claimsScrollRow = Math.min(claimsScrollRow, maxScroll);
        int start = claimsScrollRow, end = Math.min(controller.claims().size(), start + CLAIM_ROWS_VISIBLE), rowY = layout.y + CLAIM_ROW_Y;
        for (int index = start; index < end; index++) {
            ClaimEntry entry = controller.claims().get(index);
            boolean hasGeometry = entry.geometry() != null;
            boolean detailsReady = entry.key().equals(controller.selectedKey()) && controller.detailsAvailable();
            if (hasGeometry) {
                HitboxButton row = new HitboxButton(layout.rightScreenX(0), layout.screenY(rowY), layout.rightScreenSize(136), layout.screenHeight(20),
                    Text.literal("Select " + entry.label()), button -> { controller.select(entry.key()); rebuild(); });
                row.setTooltip(Tooltip.of(Text.literal("Select this known Claim")));
                add(row);
            } else {
                String actionLabel = detailsReady ? "DETAILS" : "LOAD";
                ButtonWidget action = accentRightButton(Text.literal(actionLabel), button -> {
                    if (detailsReady) controller.loadDetails(); else controller.openInfo(entry.key());
                }, 142, rowY, 48, 20);
                action.setTooltip(Tooltip.of(Text.literal(detailsReady
                    ? "Load Details geometry for " + entry.label() + " (one server action)" : "Load Claim Info for " + entry.label() + " (one server action)")));
                add(action);
            }
            rowY += CLAIM_ROW_STEP;
        }

        ClaimEntry selected = controller.selected();
        if (selected != null && selected.geometry() != null) {
            ButtonWidget map = selected.displayEnabled()
                ? rightButton(Text.literal("MAP: ON"), button -> { controller.toggleDisplay(selected.key()); rebuild(); }, 0, layout.y + 242, 92, 18)
                : accentRightButton(Text.literal("MAP: OFF"), button -> { controller.toggleDisplay(selected.key()); rebuild(); }, 0, layout.y + 242, 92, 18);
            map.setTooltip(Tooltip.of(Text.literal(selected.displayEnabled() ? "Hide this Claim from Xaero maps" : "Show this Claim on Xaero maps")));
            add(map);
            ButtonWidget boundary = rightButton(Text.literal("Boundary: " + (GameMenuDashboardClient.CLAIMS_DISPLAY.boundariesVisible() ? "ON" : "OFF")), button -> {
                GameMenuDashboardClient.CLAIMS_DISPLAY.toggleBoundaries(); rebuild();
            }, 96, layout.y + 242, 94, 18);
            boundary.setTooltip(Tooltip.of(Text.literal("Show known Claim boundaries in the world")));
            add(boundary);
            add(rightButton(Text.literal("Trust"), button -> { rightTab = RightTab.TRUST; claimWorkspace = ClaimWorkspace.NONE; rebuild(); }, 0, layout.y + 222, 92, 18));
            add(rightButton(Text.literal("Resize Claim"), button -> openWorkspace(ClaimWorkspace.RESIZE), 96, layout.y + 222, 94, 18));
            addResizeWorkspaceControls();
        }
    }

    private void addRightTabs() {
        add(tabButton(Text.literal("CLAIMS").formatted(Formatting.YELLOW), button -> { rightTab = RightTab.CLAIMS; rebuild(); }, 0, layout.y + 2, 60, 20, rightTab == RightTab.CLAIMS));
        ButtonWidget trust = tabButton(Text.literal("TRUST").formatted(Formatting.YELLOW), button -> { rightTab = RightTab.TRUST; rebuild(); }, 64, layout.y + 2, 60, 20, rightTab == RightTab.TRUST);
        trust.active = GameMenuDashboardClient.CLAIMS.selected() != null && GameMenuDashboardClient.CLAIMS.selected().geometry() != null;
        if (!trust.active) trust.setTooltip(Tooltip.of(Text.literal("Select a Claim with loaded Details first")));
        add(trust);
    }

    private ButtonWidget tabButton(Text label, ButtonWidget.PressAction action, int localX, int y, int width, int height, boolean selected) {
        return new TabButton(layout.rightScreenX(localX), layout.screenY(y), layout.rightScreenSize(width), layout.screenHeight(height), label, action, selected, fittedTextScale(label, width, layout.scale));
    }

    private void addTrustTabControls() {
        ClaimsController controller = GameMenuDashboardClient.CLAIMS;
        ClaimEntry selected = controller.selected();
        if (selected == null || selected.geometry() == null) return;
        boolean rosterLoaded = controller.trustTruth() == ClaimsController.TrustTruth.FRESH || controller.trustTruth() == ClaimsController.TrustTruth.STALE;
        boolean trustStale = controller.trustTruth() == ClaimsController.TrustTruth.STALE;
        String label = switch (controller.trustTruth()) {
            case READY_FOR_ALL -> "Load Players";
            case AWAITING_PANEL, AWAITING_ALL -> "Loading…";
            case FRESH -> "↻";
            case STALE -> "Refresh";
            default -> "Load Trust";
        };
        ButtonWidget load = rosterLoaded
            ? (trustStale
                ? accentRightButton(Text.literal(label), button -> { controller.loadTrustList(); rebuild(); }, 116, layout.y + 48, 74, 18)
                : rightButton(Text.literal(label), button -> { controller.loadTrustList(); rebuild(); }, 158, layout.y + 48, 32, 16))
            : accentRightButton(Text.literal(label), button -> { if (controller.trustTruth() == ClaimsController.TrustTruth.READY_FOR_ALL) controller.loadAllTrusted(); else controller.loadTrustList(); rebuild(); }, 0, layout.y + 72, 110, 18);
        load.active = controller.trustTruth() != ClaimsController.TrustTruth.AWAITING_PANEL && controller.trustTruth() != ClaimsController.TrustTruth.AWAITING_ALL;
        load.setTooltip(Tooltip.of(Text.literal(rosterLoaded ? "Refresh trusted players" : "Load current trusted players"))); add(load);
        int maxScroll = Math.max(0, controller.trustedPlayers().size() - 6);
        trustScrollRow = Math.min(trustScrollRow, maxScroll);
        int rowY = layout.y + 70;
        for (ClaimsController.TrustedPlayer trusted : controller.trustedPlayers().stream().skip(trustScrollRow).limit(6).toList()) {
            ButtonWidget remove = rightButton(Text.literal("×").formatted(Formatting.RED), button -> { controller.revokeTrust(trusted.player()); rebuild(); }, 172, rowY, 18, 16);
            remove.setTooltip(Tooltip.of(Text.literal("Remove trust for " + trusted.player()))); add(remove);
            rowY += 20;
        }
        TextFieldWidget player = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, layout.rightScreenX(0), layout.screenY(layout.y + 222), layout.rightScreenSize(190), layout.screenHeight(18), Text.literal("Player"));
        player.setText(trustPlayer); player.setPlaceholder(Text.literal("Enter player name")); add(player);
        ButtonWidget container = rightButton(Text.literal("Container"), button -> {}, 0, layout.y + 256, 60, 16);
        container.active = false; container.setTooltip(Tooltip.of(Text.literal("Use storage and interact. Server verification is still needed."))); add(container);
        ButtonWidget builder = accentRightButton(Text.literal("Builder"), button -> { controller.grantBuilder(trustPlayer.strip()); rebuild(); }, 65, layout.y + 256, 60, 16);
        builder.active = trustPlayer.strip().matches("[A-Za-z0-9_]{3,16}"); builder.setTooltip(Tooltip.of(Text.literal("Build and edit this Claim"))); add(builder);
        ButtonWidget manager = rightButton(Text.literal("Manager"), button -> {}, 130, layout.y + 256, 60, 16);
        manager.active = false; manager.setTooltip(Tooltip.of(Text.literal("Manage this Claim. Server verification is still needed."))); add(manager);
        // Widgets are not rebuilt for ordinary text entry. This listener repairs
        // the dev.17 regression where Builder stayed disabled after a valid name was typed.
        player.setChangedListener(value -> {
            trustPlayer = value;
            builder.active = value.strip().matches("[A-Za-z0-9_]{3,16}");
        });
    }

    private void openWorkspace(ClaimWorkspace workspace) {
        claimWorkspace = claimWorkspace == workspace ? ClaimWorkspace.NONE : workspace;
        rebuild();
    }

    private void closeWorkspace() { claimWorkspace = ClaimWorkspace.NONE; rebuild(); }

    private void addResizeWorkspaceControls() {
        if (claimWorkspace != ClaimWorkspace.RESIZE) return;
        WorkspaceLayout workspace = workspaceLayout();
        if (workspace == null) return;
        add(workspaceButton(Text.literal("← Back"), button -> closeWorkspace(), workspace, workspace.logicalWidth() - 62, 4, 58, 18));
        {
            if (workspace.logicalHeight() < 180) return;
            int mid = workspace.logicalWidth() / 2;
            add(workspaceButton(Text.literal("North"), b -> chooseResizeDirection("North"), workspace, mid - 28, 72, 56, 18));
            add(workspaceButton(Text.literal("West"), b -> chooseResizeDirection("West"), workspace, mid - 60, 94, 52, 18));
            add(workspaceButton(Text.literal("East"), b -> chooseResizeDirection("East"), workspace, mid + 8, 94, 52, 18));
            add(workspaceButton(Text.literal("South"), b -> chooseResizeDirection("South"), workspace, mid - 28, 116, 56, 18));
            add(workspaceButton(Text.literal("All"), b -> chooseResizeDirection("All"), workspace, mid - 22, 138, 44, 18));
            TextFieldWidget amount = new TextFieldWidget(MinecraftClient.getInstance().textRenderer, workspace.screenX() + layout.screenSize(4), workspace.screenY() + layout.screenSize(160), layout.screenSize(72), layout.screenHeight(18), Text.literal("Amount"));
            amount.setText(resizeAmount); amount.setMaxLength(6); amount.setPlaceholder(Text.literal("amount"));
            amount.setChangedListener(value -> { if (value.matches("[0-9]{0,6}")) { resizeAmount = value; ResizePreview.plan(resizeDirection, resizeAmount); refreshActivePreview(); } }); add(amount);
            add(workspaceButton(Text.literal("Preview"), b -> previewResize(), workspace, 80, 160, 64, 18));
            add(workspaceButton(Text.literal("Clear"), b -> { ResizePreview.clear(); rebuild(); }, workspace, 148, 160, 54, 18));
        }
    }

    private void previewResize() {
        ClaimEntry selected = GameMenuDashboardClient.CLAIMS.selected();
        if (selected == null || selected.geometry() == null) return;
        try { ResizePreview.plan(resizeDirection, resizeAmount); ResizePreview.show(selected.key(), ResizePreview.derive(selected.geometry(), resizeDirection, Integer.parseInt(resizeAmount))); rebuild(); }
        catch (IllegalArgumentException ignored) { }
    }
    private void chooseResizeDirection(String direction) { resizeDirection = direction; ResizePreview.plan(resizeDirection, resizeAmount); refreshActivePreview(); rebuild(); }
    private void refreshActivePreview() {
        ClaimEntry selected = GameMenuDashboardClient.CLAIMS.selected();
        if (selected != null && selected.geometry() != null && ResizePreview.activeFor(selected.key())) {
            ResizePreview.show(selected.key(), ResizePreview.derive(selected.geometry(), resizeDirection, parseResizeAmount()));
        }
    }

    private static void sendTravel(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() != null) client.getNetworkHandler().sendChatCommand(command);
    }
    private void removeWidgets() { Screens.getButtons(screen).removeAll(widgets); widgets.clear(); }

    private void scroll(double mouseX, double mouseY, double vertical) {
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        double logicalX = layout.logicalX(mouseX), logicalY = layout.logicalY(mouseY);
        if (snapshot != null && logicalX >= layout.x && logicalX <= layout.x + layout.width
            && logicalY >= layout.rowsY && logicalY <= layout.rowsY + layout.rows * layout.rowHeight) {
            int next = Math.max(0, Math.min(snapshot.names().size() - layout.rows, scrollRow + (vertical < 0 ? 1 : -1)));
            if (next != scrollRow) { scrollRow = next; rebuild(); }
            return;
        }
        double rightLogicalX = (mouseX - layout.rightX) / layout.scale;
        if (rightLogicalX < 0 || rightLogicalX > Layout.CANONICAL_WIDTH || logicalY < layout.y + CLAIM_ROW_Y
            || logicalY > layout.y + CLAIM_ROW_Y + CLAIM_ROWS_VISIBLE * CLAIM_ROW_STEP) return;
        if (rightTab == RightTab.TRUST) {
            int maxTrust = Math.max(0, GameMenuDashboardClient.CLAIMS.trustedPlayers().size() - 6);
            int nextTrust = Math.max(0, Math.min(maxTrust, trustScrollRow + (vertical < 0 ? 1 : -1)));
            if (nextTrust != trustScrollRow) { trustScrollRow = nextTrust; rebuild(); }
            return;
        }
        int maxScroll = Math.max(0, GameMenuDashboardClient.CLAIMS.claims().size() - CLAIM_ROWS_VISIBLE);
        int next = Math.max(0, Math.min(maxScroll, claimsScrollRow + (vertical < 0 ? 1 : -1)));
        if (next != claimsScrollRow) { claimsScrollRow = next; rebuild(); }
    }

    private void render(DrawContext context) {
        TextRenderer text = MinecraftClient.getInstance().textRenderer;
        context.getMatrices().push();
        context.getMatrices().translate(layout.x, layout.y, 0);
        context.getMatrices().scale(layout.scale, layout.scale, 1.0F);
        context.getMatrices().translate(-layout.x, -layout.y, 0);
        renderLeft(context, text);
        context.getMatrices().pop();
        renderClaimManagementWorkspace(context, text);
        renderRightPlaceholder(context, text);
    }

    private void renderLeft(DrawContext context, TextRenderer text) {
        context.fill(layout.x - 4, layout.y - 4, layout.x + layout.width + 4, layout.bottom(), 0xBE101419);
        context.drawBorder(layout.x - 4, layout.y - 4, layout.width + 8, layout.height, 0xFF3C434B);
        context.drawText(text, Text.literal("HOMES"), layout.x, layout.y + 4, 0xFFFFD05B, false);
        context.drawText(text, Text.literal(""), layout.x, layout.y + 20, 0xFFFFFFFF, false);
        HomesController.Truth truth = GameMenuDashboardClient.HOMES.truth();
        HomeSnapshot snapshot = GameMenuDashboardClient.HOMES.snapshot();
        if (snapshot != null) context.drawText(text, Text.literal(snapshot.count() + " / " + snapshot.limit()), layout.x + 78, layout.y + 4, 0xFFFFFFFF, false);
        context.fill(layout.x, layout.y + 27, layout.x + layout.width, layout.y + 28, 0xFF5A6065);
        renderFooter(context, text);
        if (snapshot == null) {
            String message = truth == HomesController.Truth.FAILED ? "Could not load Homes. Try again." : "Home data not loaded yet.";
            context.drawText(text, Text.literal(message), layout.x, layout.rowsY, 0xFFD0D0D0, false);
            if (truth != HomesController.Truth.FAILED) context.drawText(text, Text.literal("Refresh to populate this panel."), layout.x, layout.rowsY + 12, 0xFFA0A0A0, false);
            if (layout.canShowTravel()) renderTravelLabels(context, text);
            return;
        }
        if (snapshot.names().size() > layout.rows) renderScrollbar(context, snapshot.names().size());
        if (deleteCandidate != null) context.drawText(text, Text.literal(clip(text, "Confirm delete Home " + deleteCandidate + "?", layout.width)), layout.x, layout.y + 34, 0xFFFF5555, false);
        if (layout.canShowTravel()) renderTravelLabels(context, text);
    }

    private void renderTravelLabels(DrawContext context, TextRenderer text) {
        int dividerY = layout.travelY - 32;
        context.fill(layout.x, dividerY, layout.x + layout.width, dividerY + 1, 0xFF5A6065);
        context.drawText(text, Text.literal("SERVER TRAVEL"), layout.x, layout.travelY - 20, 0xFFFFD05B, false);
        context.drawText(text, Text.literal("──────── WARPS ────────"), layout.x, layout.travelY + 25, 0xFF9A9A9A, false);
    }

    private void renderFooter(DrawContext context, TextRenderer text) {
        // Scale down one step so the full product name stays within the left panel.
        context.getMatrices().push();
        context.getMatrices().translate(layout.x, layout.bottom() - 17, 0);
        context.getMatrices().scale(0.75F, 0.75F, 1.0F);
        context.drawText(text, Text.literal(DashboardBranding.footerLineOne()), 0, 0, 0xFFAAAAAA, false);
        context.drawText(text, Text.literal(DashboardBranding.footerLineTwo()), 0, 10, 0xFFAAAAAA, false);
        context.getMatrices().pop();
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
        private final float textScale;
        DangerButton(int x, int y, int width, int height, Text message, PressAction action, float textScale) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
            this.textScale = textScale;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = isHovered() || isFocused() ? 0xFFC0392B : 0xFF8E2420;
            context.fill(getX(), getY(), getRight(), getBottom(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFFFF7770);
            context.getMatrices().push();
            context.getMatrices().translate(getX() + getWidth() / 2.0F, getY() + getHeight() / 2.0F, 0);
            context.getMatrices().scale(textScale, textScale, 1.0F);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), 0, -4, 0xFFFFFFFF);
            context.getMatrices().pop();
        }
    }

    /** A physically scaled widget keeps visible controls and click bounds in one model. */
    private static final class ScaledButton extends ButtonWidget {
        private final float textScale;
        ScaledButton(int x, int y, int width, int height, Text message, PressAction action, float textScale) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
            this.textScale = textScale;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = isHovered() || isFocused() ? 0xFF4A4F55 : 0xFF2B2F34;
            context.fill(getX(), getY(), getRight(), getBottom(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFF111417);
            context.getMatrices().push();
            context.getMatrices().translate(getX() + getWidth() / 2.0F, getY() + getHeight() / 2.0F, 0);
            context.getMatrices().scale(textScale, textScale, 1.0F);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), 0, -4, 0xFFFFFFFF);
            context.getMatrices().pop();
        }
    }

    /** Click-only claim-card hit target; static card rendering remains the sole visual. */
    private static final class HitboxButton extends ButtonWidget {
        HitboxButton(int x, int y, int width, int height, Text message, PressAction action) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
        }
        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) { }
    }

    /** Explicit yellow tab control; selected state is local navigation only. */
    private static final class TabButton extends ButtonWidget {
        private final boolean selected;
        private final float textScale;
        TabButton(int x, int y, int width, int height, Text message, PressAction action, boolean selected, float textScale) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
            this.selected = selected; this.textScale = textScale;
        }
        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = selected ? 0xFF44402A : (isHovered() || isFocused() ? 0xFF383B3F : 0xFF25292D);
            context.fill(getX(), getY(), getRight(), getBottom(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), selected ? 0xFFFFD05B : 0xFF5A6065);
            context.getMatrices().push();
            context.getMatrices().translate(getX() + getWidth() / 2.0F, getY() + getHeight() / 2.0F, 0);
            context.getMatrices().scale(textScale, textScale, 1.0F);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), 0, -4, 0xFFFFD05B);
            context.getMatrices().pop();
        }
    }

    /** Green identifies deliberate, non-destructive data-loading actions. */
    private static final class AccentButton extends ButtonWidget {
        private final float textScale;
        AccentButton(int x, int y, int width, int height, Text message, PressAction action, float textScale) {
            super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
            this.textScale = textScale;
        }
        @Override protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            int fill = isHovered() || isFocused() ? 0xFF327A48 : 0xFF235D37;
            context.fill(getX(), getY(), getRight(), getBottom(), fill);
            context.drawBorder(getX(), getY(), getWidth(), getHeight(), 0xFF86D99B);
            context.getMatrices().push();
            context.getMatrices().translate(getX() + getWidth() / 2.0F, getY() + getHeight() / 2.0F, 0);
            context.getMatrices().scale(textScale, textScale, 1.0F);
            context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, getMessage(), 0, -4, 0xFFFFFFFF);
            context.getMatrices().pop();
        }
    }

    private void renderRightPlaceholder(DrawContext context, TextRenderer text) {
        int x = 0, y = 0, width = Layout.CANONICAL_WIDTH;
        context.getMatrices().push();
        context.getMatrices().translate(layout.rightX, layout.y, 0);
        context.getMatrices().scale(layout.scale, layout.scale, 1.0F);
        context.fill(x - 4, y - 4, x + width + 4, Layout.CANONICAL_HEIGHT, 0xBE101419);
        context.drawBorder(x - 4, y - 4, width + 8, layout.height, 0xFF3C434B);
        ClaimsController controller = GameMenuDashboardClient.CLAIMS;
        context.fill(x, y + 27, x + width, y + 28, 0xFF5A6065);
        if (rightTab == RightTab.CLAIMS) {
            renderClaimRows(context, text, controller);
            if (controller.claims().size() > CLAIM_ROWS_VISIBLE) renderRightScrollbar(context, controller.claims().size());
            context.fill(x, y + 156, x + width, y + 157, 0xFF5A6065);
            renderClaimWorkspace(context, text, controller, x, y, width);
        } else renderTrustTab(context, text, controller, x, y, width);
        context.getMatrices().pop();
    }

    private void renderClaimManagementWorkspace(DrawContext context, TextRenderer text) {
        if (claimWorkspace == ClaimWorkspace.NONE || GameMenuDashboardClient.CLAIMS.selected() == null) return;
        ClaimsController controller = GameMenuDashboardClient.CLAIMS;
        WorkspaceLayout workspace = workspaceLayout();
        if (workspace == null) return;
        ClaimEntry selected = GameMenuDashboardClient.CLAIMS.selected();
        context.getMatrices().push();
        context.getMatrices().translate(workspace.screenX(), workspace.screenY(), 0);
        context.getMatrices().scale(layout.scale, layout.scale, 1.0F);
        context.fill(-4, -4, workspace.logicalWidth() + 4, workspace.logicalHeight(), 0xD8101419);
        context.drawBorder(-4, -4, workspace.logicalWidth() + 8, workspace.logicalHeight() + 4, 0xFF56707B);
        String title = "RESIZE CLAIM";
        context.drawText(text, Text.literal(title), 0, 4, 0xFFFFD05B, false);
        String workspaceIdentity = selected.geometry() == null ? selected.label() : selected.label() + " @ " + center(selected.geometry());
        context.drawText(text, Text.literal(clip(text, workspaceIdentity, workspace.logicalWidth() - 70)), 0, 20, 0xFFD0D0D0, false);
        context.fill(0, 35, workspace.logicalWidth(), 36, 0xFF5A6065);
        {
            ClaimGeometry current = selected.geometry();
            int amount = parseResizeAmount();
            ClaimGeometry proposed = current == null ? null : ResizePreview.derive(current, resizeDirection, amount);
            context.drawText(text, Text.literal("Current: " + dimensions(current)), 0, 48, 0xFFB7D6E2, false);
            context.drawText(text, Text.literal("Proposed: " + dimensions(proposed)), 0, 60, 0xFFFFB560, false);
            context.drawText(text, Text.literal("Choose direction + amount, then Preview."), 0, 184, 0xFFBBBBBB, false);
            context.drawText(text, Text.literal(ResizePreview.activeFor(selected.key()) ? "Preview ON · close menu to inspect" : "Preview is local; Apply is not enabled."), 0, 196, ResizePreview.activeFor(selected.key()) ? 0xFFFFB560 : 0xFF888888, false);
        }
        context.getMatrices().pop();
        // The arrow intentionally sits just outside Claims, pointing left toward the
        // temporary lower-center task surface rather than suggesting a fourth module.
        context.drawText(text, Text.literal("<"), layout.rightX - layout.screenSize(11), workspace.screenY() + 7, 0xFFFFD05B, false);
    }

    private int parseResizeAmount() { try { return Math.max(1, Integer.parseInt(resizeAmount)); } catch (NumberFormatException ignored) { return 1; } }
    private static String dimensions(ClaimGeometry geometry) {
        if (geometry == null) return "Details required";
        return (Math.abs(geometry.greaterX() - geometry.lesserX()) + 1) + " × " + (Math.abs(geometry.greaterZ() - geometry.lesserZ()) + 1);
    }

    /** Positioned from vanilla's actual Disconnect button, not a guessed menu row. */
    private WorkspaceLayout workspaceLayout() {
        int disconnectBottom = Screens.getButtons(screen).stream()
            .filter(widget -> widget.getMessage().getString().equalsIgnoreCase("Disconnect"))
            .mapToInt(widget -> widget.getY() + widget.getHeight())
            .max()
            .orElse(Math.round(layout.screenHeight() * 0.72F));
        int top = disconnectBottom + 8;
        int availableWidth = layout.rightX - layout.screenX(layout.x + layout.width) - 28;
        int logicalWidth = Math.min(260, Math.round(availableWidth / layout.scale));
        int availableHeight = layout.screenHeight() - top - 8;
        int logicalHeight = Math.min(218, Math.round(availableHeight / layout.scale));
        if (logicalWidth < 160 || logicalHeight < 76) return null;
        return new WorkspaceLayout((layout.screenWidth() - layout.screenSize(logicalWidth)) / 2, top, logicalWidth, logicalHeight);
    }

    private ClaimEntry currentClaim() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return null;
        String world = client.world.getRegistryKey().getValue().toString();
        var pos = client.player.getBlockPos();
        List<ClaimEntry> matches = GameMenuDashboardClient.CLAIMS.claims().stream().filter(entry -> entry.geometry() != null && !entry.geometryStale()
            && entry.geometry().containsBlock(world, pos.getX(), pos.getZ())).toList();
        return matches.size() == 1 ? matches.getFirst() : null;
    }

    private void renderRightScrollbar(DrawContext context, int count) {
        int trackX = Layout.CANONICAL_WIDTH - 3, top = CLAIM_ROW_Y, height = CLAIM_ROWS_VISIBLE * CLAIM_ROW_STEP - 4;
        int thumb = Math.max(10, height * CLAIM_ROWS_VISIBLE / count);
        int offset = (height - thumb) * claimsScrollRow / Math.max(1, count - CLAIM_ROWS_VISIBLE);
        context.fill(trackX, top, trackX + 2, top + height, 0x665A6065);
        context.fill(trackX, top + offset, trackX + 2, top + offset + thumb, 0xFFA0A0A0);
    }

    /** Labels are drawn once in fixed cards; only actions are widgets, avoiding text-widget overlap. */
    private void renderClaimRows(DrawContext context, TextRenderer text, ClaimsController controller) {
        int start = claimsScrollRow, end = Math.min(controller.claims().size(), start + CLAIM_ROWS_VISIBLE), rowY = CLAIM_ROW_Y;
        ClaimEntry here = currentClaim();
        for (int index = start; index < end; index++) {
            ClaimEntry entry = controller.claims().get(index);
            boolean selected = entry.key().equals(controller.selectedKey());
            boolean playerHere = here != null && entry.key().equals(here.key());
            context.fill(0, rowY, 136, rowY + 20, playerHere ? 0xCC245A3D : selected ? 0xCC244A39 : 0xCC171D23);
            context.drawBorder(0, rowY, 136, 20, playerHere ? 0xFF69F0AE : selected ? 0xFF86D99B : 0xFF303941);
            String label = entry.label();
            if (entry.geometry() != null && label.equalsIgnoreCase("basic")) label = "Basic @ " + center(entry.geometry());
            if (playerHere) label = "HERE · " + label;
            context.drawText(text, Text.literal(clip(text, label, 128)), 4, rowY + 6, playerHere ? 0xFFB6FFCA : 0xFFF2F2F2, false);
            rowY += CLAIM_ROW_STEP;
        }
    }

    private void renderClaimWorkspace(DrawContext context, TextRenderer text, ClaimsController controller, int x, int y, int width) {
        ClaimEntry selected = controller.selected();
        if (selected == null) {
            String message = switch (controller.truth()) {
                case UNLOADED -> "Load Claims to begin your land registry.";
                case AWAITING_LIST -> "Loading your owned Claims…";
                case FAILED -> "Could not load Claims. Try Refresh.";
                case STALE -> "(!) Refresh needed";
                case FRESH -> "Select a known Claim to view it.";
            };
            context.drawText(text, Text.literal(message), x, y + 168, controller.truth() == ClaimsController.Truth.STALE ? 0xFFFFAA55 : 0xFFBBBBBB, false);
            return;
        }
        if (selected.geometry() == null) {
            context.drawText(text, Text.literal(controller.detailsAvailable() ? "Details ready → select DETAILS" : "Load Details to discover this Claim."), x, y + 168, 0xFFBBBBBB, false);
            return;
        }
        ClaimGeometry geometry = selected.geometry();
        ClaimEntry here = currentClaim();
        if (here != null && here.key().equals(selected.key()) && !selected.geometryStale()) context.drawText(text, Text.literal("YOU ARE HERE"), x, y + 166, 0xFF69F0AE, false);
        else context.drawText(text, Text.literal(selected.label() + " · " + selected.type()), x, y + 166, 0xFFFFFFFF, false);
        context.drawText(text, Text.literal("World: " + geometry.world()), x, y + 178, 0xFFBBBBBB, false);
        context.drawText(text, Text.literal("Size: " + (Math.abs(geometry.greaterX() - geometry.lesserX()) + 1) + " × " + (Math.abs(geometry.greaterZ() - geometry.lesserZ()) + 1)
            + (selected.blocks().isBlank() ? "" : " · " + selected.blocks() + " blocks")), x, y + 190, 0xFFBBBBBB, false);
        context.drawText(text, Text.literal("Center: " + center(geometry)), x, y + 202, 0xFFBBBBBB, false);
        if (selected.geometryStale()) context.drawText(text, Text.literal("(!) Refresh needed"), x, y + 214, 0xFFFFAA55, false);
        else context.drawText(text, Text.literal("Open Trust or Resize Claim"), x, y + 214, 0xFF888888, false);
    }

    private void renderTrustTab(DrawContext context, TextRenderer text, ClaimsController controller, int x, int y, int width) {
        ClaimEntry selected = controller.selected();
        if (selected == null || selected.geometry() == null) {
            context.drawText(text, Text.literal("Select a Claim to manage access."), x, y + 48, 0xFFBBBBBB, false);
            return;
        }
        context.drawText(text, Text.literal(clip(text, selected.label() + " @ " + center(selected.geometry()), width)), x, y + 32, 0xFFD0D0D0, false);
        context.fill(x, y + 44, x + width, y + 45, 0xFF5A6065);
        context.drawText(text, Text.literal("TRUSTED PLAYERS"), x, y + 50, 0xFFFFD05B, false);
        String status = switch (controller.trustTruth()) {
            case UNLOADED -> "Current access hasn't been loaded.";
            case AWAITING_PANEL, AWAITING_ALL -> "Loading…";
            case READY_FOR_ALL -> "Load trusted players to continue.";
            case FRESH -> controller.trustedPlayers().isEmpty() ? "No one currently has access." : "";
            case STALE -> "(!) Refresh needed";
            case FAILED -> "Current access could not be loaded.";
        };
        context.drawText(text, Text.literal(clip(text, status, width)), x, y + 62, controller.trustTruth() == ClaimsController.TrustTruth.STALE ? 0xFFFFAA55 : 0xFFBBBBBB, false);
        int rowY = controller.trustTruth() == ClaimsController.TrustTruth.STALE ? y + 80 : y + 70;
        for (ClaimsController.TrustedPlayer trusted : controller.trustedPlayers().stream().skip(trustScrollRow).limit(6).toList()) {
            context.fill(x, rowY - 2, x + width - 22, rowY + 14, 0x66171D23);
            String access = trusted.permission().isBlank() ? "" : "  " + trusted.permission();
            context.drawText(text, Text.literal(clip(text, trusted.player() + access, width - 28)), x + 4, rowY + 1, controller.trustTruth() == ClaimsController.TrustTruth.STALE ? 0xFFAAAAAA : 0xFFF2F2F2, false);
            rowY += 20;
        }
        context.fill(x, y + 204, x + width, y + 205, 0xFF5A6065);
        context.drawText(text, Text.literal("ADD TRUST"), x, y + 210, 0xFFFFD05B, false);
        context.drawText(text, Text.literal("Give this player:"), x, y + 244, 0xFFBBBBBB, false);
    }

    private static String center(ClaimGeometry geometry) { return ((geometry.lesserX() + geometry.greaterX()) / 2) + ", " + ((geometry.lesserZ() + geometry.greaterZ()) / 2); }

    private record Layout(int x, int y, int width, int height, int rowsY, int rows, int rowHeight,
                          int homeButtonHeight, int deleteWidth, int reloadSize, int travelY, int warpsY,
                          int travelColumns, int travelButtonWidth, int travelButtonHeight, int travelRowStep,
                          int travelGap, int rightX, int screenWidth, int screenHeight, float scale) {
        private static final int CANONICAL_WIDTH = 190;
        private static final int CANONICAL_HEIGHT = 302;
        private static final float MIN_INTERACTIVE_SCALE = 0.40F;
        private static final int OUTER_MARGIN = 8;
        private static final int GUTTER_BREATHING_ROOM = 24;

        static Layout forScreen(int width, int height) {
            int centerLeft = width / 2 - 102;
            int originX = OUTER_MARGIN;
            int leftGutter = Math.max(0, centerLeft - OUTER_MARGIN - GUTTER_BREATHING_ROOM);
            int rightGutter = Math.max(0, width - (width / 2 + 102) - OUTER_MARGIN - GUTTER_BREATHING_ROOM);

            // Mac's approved composition establishes a 190×302 side module. Never
            // stretch it on a broad monitor. When either side is constrained, scale
            // both modules uniformly together, leaving intentional breathing room.
            // A single uniform transform preserves Minecraft glyph aspect ratio.
            float horizontalScale = Math.min(leftGutter, rightGutter) / (float) CANONICAL_WIDTH;
            float verticalScale = (height - OUTER_MARGIN * 2) / (float) CANONICAL_HEIGHT;
            float scale = Math.min(1.0F, Math.min(horizontalScale, verticalScale));
            int originY = Math.max(OUTER_MARGIN, Math.round((height - CANONICAL_HEIGHT * Math.max(0.0F, scale)) / 2.0F));
            return new Layout(originX, originY, CANONICAL_WIDTH, CANONICAL_HEIGHT, originY + 32, 5, 22,
                20, 22, 20, originY + 176, originY + 219, 3, 60, 18, 22, 4,
                width - OUTER_MARGIN - Math.round(CANONICAL_WIDTH * scale), width, height, scale);
        }
        int bottom() { return y + height; }
        boolean hasRoom() { return scale >= MIN_INTERACTIVE_SCALE; }
        boolean canShowTravel() { return hasRoom(); }
        int screenX(int logical) { return Math.round(x + (logical - x) * scale); }
        int screenY(int logical) { return Math.round(y + (logical - y) * scale); }
        int screenSize(int logical) { return Math.max(1, Math.round(logical * scale)); }
        int screenHeight(int logical) { return Math.max(1, Math.round(logical * scale)); }
        int rightScreenX(int local) { return rightX + Math.round(local * scale); }
        int rightScreenSize(int logical) { return Math.max(1, Math.round(logical * scale)); }
        double logicalX(double screen) { return x + (screen - x) / scale; }
        double logicalY(double screen) { return y + (screen - y) / scale; }
    }

    private record WorkspaceLayout(int screenX, int screenY, int logicalWidth, int logicalHeight) { }
}
