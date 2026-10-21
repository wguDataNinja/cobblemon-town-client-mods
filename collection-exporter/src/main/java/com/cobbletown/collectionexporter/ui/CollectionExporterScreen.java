package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;
import com.cobbletown.collectionexporter.report.ReportCategory;
import com.cobbletown.collectionexporter.report.ReportResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Native export screen. It reads the client-synchronized PC once when opened, rebuilds from
 * that cached observation for local filter changes, and copies the generated report through
 * the existing clipboard adapter. It never reads the PC during rendering.
 */
public final class CollectionExporterScreen extends Screen {
    private static final int MUTED = 0xFFB8C6D8;
    private static final int LABEL = 0xFFDDE6F2;
    private static final int TITLE = 0xFFFFFFFF;
    private static final int SUCCESS = 0xFF8FE388;
    private static final int WARNING = 0xFFFFD54A;
    private static final int ERROR = 0xFFFF7A7A;
    private static final int DIVIDER = 0x30FFFFFF;

    private final Screen parent;
    private final ExportController controller;
    private final ExportScreenModel model = new ExportScreenModel();

    private boolean loaded;
    private String loadFailure;
    private ExportStatusText.Message status;
    private ButtonWidget copyButton;
    private ExportScreenLayout layout;

    private int unresolvedX;
    private int unresolvedY;
    private int unresolvedWidth;
    private int unresolvedHeight;
    private int statusX;
    private int statusY;
    private int statusWidth;
    private int statusHeight;

    public CollectionExporterScreen(Screen parent, ExportController controller) {
        super(Text.literal("Collection Exporter"));
        this.parent = parent;
        this.controller = controller;
    }

    @Override
    protected void init() {
        layout = ExportScreenLayout.compute(width, height);
        ensureLoaded();
        addDrawable((context, mouseX, mouseY, delta) -> drawPanel(context));
        addFilterWidgets();
        addActionWidgets();
        updateCopyState();
    }

    private void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        readPc(true);
    }

    private void readPc(boolean freshRead) {
        try {
            if (freshRead) {
                model.refresh(controller);
            } else {
                model.rebuild(controller);
            }
            loadFailure = null;
        } catch (RuntimeException failure) {
            loadFailure = shortFailure(failure);
        }
        status = ExportStatusText.afterLoad(
                model.report(),
                loadFailure,
                !model.selectedCategories().isEmpty());
    }

    private void addFilterWidgets() {
        addFilter(ReportCategory.ALL, "All Pokémon");
        addFilter(ReportCategory.SKINS, "Custom Skins");
        addFilter(ReportCategory.LEGEND_MYTH, "Legendary & Mythical");
        addFilter(ReportCategory.SHINY, "Shiny");
    }

    private void addFilter(ReportCategory category, String label) {
        int x = layout.contentX()
                + layout.filterColumn(category) * (layout.columnWidth() + layout.columnGap());
        int y = layout.filterTop() + layout.filterRow(category) * layout.filterRowHeight();
        String countedLabel = label + " (" + model.categoryCount(category) + ")";
        CheckboxWidget checkbox = CheckboxWidget.builder(Text.literal(countedLabel), textRenderer)
                .pos(x, y)
                .checked(model.isSelected(category))
                .maxWidth(layout.columnWidth())
                .callback((widget, checked) -> onFilterChanged(category, checked))
                .build();
        addDrawableChild(checkbox);
    }

    private void addActionWidgets() {
        int gap = 6;
        int contentWidth = layout.contentWidth();
        int backWidth = Math.min(84, Math.max(58, contentWidth / 4));
        int refreshWidth = Math.min(90, Math.max(62, contentWidth / 4));
        int copyWidth = contentWidth - backWidth - refreshWidth - gap * 2;
        if (copyWidth < 76) {
            copyWidth = 76;
            int remaining = Math.max(40, contentWidth - copyWidth - gap * 2);
            backWidth = remaining / 2;
            refreshWidth = remaining - backWidth;
        }
        String copyLabel = copyWidth < 120 ? "Copy" : "Copy to Clipboard";

        addDrawableChild(ButtonWidget.builder(Text.literal("Refresh"), button -> refresh())
                .dimensions(layout.contentX(), layout.buttonsY(), refreshWidth, 20).build());
        copyButton = addDrawableChild(ButtonWidget.builder(Text.literal(copyLabel), button -> copy())
                .dimensions(layout.contentX() + refreshWidth + gap, layout.buttonsY(), copyWidth, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions(layout.contentX() + refreshWidth + gap + copyWidth + gap,
                        layout.buttonsY(), backWidth, 20)
                .build());
    }

    private void onFilterChanged(ReportCategory category, boolean checked) {
        model.selectCategory(category, checked);
        readPc(false);
        rebuildWidgets();
    }

    private void refresh() {
        readPc(true);
        rebuildWidgets();
    }

    private void copy() {
        try {
            ClipboardResult result = model.copy(controller);
            loadFailure = null;
            status = ExportStatusText.afterCopy(result, model.report());
        } catch (RuntimeException failure) {
            loadFailure = shortFailure(failure);
            status = new ExportStatusText.Message(
                    "Could not read the PC before copying: " + loadFailure,
                    ExportStatusText.Tone.ERROR);
        }
        updateCopyState();
    }

    private void updateCopyState() {
        if (copyButton == null) {
            return;
        }
        InventoryCompleteness completeness = model.completeness();
        copyButton.active = model.report() != null
                && loadFailure == null
                && !model.selectedCategories().isEmpty()
                && completeness != InventoryCompleteness.UNAVAILABLE
                && completeness != InventoryCompleteness.AMBIGUOUS_MULTIPLE_STORES;
    }

    private void rebuildWidgets() {
        clearChildren();
        init();
    }

    private static String shortFailure(RuntimeException failure) {
        String message = failure.getMessage();
        return message == null || message.isBlank()
                ? failure.getClass().getSimpleName()
                : failure.getClass().getSimpleName() + ": " + message;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawText(context, mouseX, mouseY);
    }

    private void drawPanel(DrawContext context) {
        context.fill(layout.panelX(), layout.panelY(),
                layout.panelX() + layout.panelWidth(), layout.panelY() + layout.panelHeight(), 0x90000000);
        context.fill(layout.panelX(), layout.panelY(),
                layout.panelX() + layout.panelWidth(), layout.panelY() + 1, 0x40FFFFFF);
        context.fill(layout.panelX(), layout.panelY() + layout.panelHeight() - 1,
                layout.panelX() + layout.panelWidth(), layout.panelY() + layout.panelHeight(), 0x40FFFFFF);
    }

    private void drawText(DrawContext context, int mouseX, int mouseY) {
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, layout.titleY(), TITLE);

        ExportStatusText.SnapshotStatus snapshot =
                ExportStatusText.snapshotStatus(model.report(), loadFailure);
        context.drawTextWithShadow(textRenderer, snapshot.headline(),
                layout.contentX(), layout.snapshotY(), toneColor(snapshot.tone()));
        if (snapshot.detail() != null) {
            context.drawTextWithShadow(textRenderer,
                    ellipsize(snapshot.detail(), layout.contentWidth()),
                    layout.contentX(), layout.snapshotY() + 10, MUTED);
        }

        drawSectionHeading(context, "CATEGORIES", layout.categoryHeadingY());
        drawSectionHeading(context, "EXPORT SUMMARY", layout.summaryHeadingY());

        ReportResult report = model.report();
        context.drawTextWithShadow(textRenderer,
                ExportStatusText.selectedLine(report),
                layout.contentX(), layout.selectedY(), LABEL);

        String unresolved = ExportStatusText.unresolvedLine(
                report, layout.contentWidth(), textRenderer::getWidth);
        unresolvedX = layout.contentX();
        unresolvedY = layout.unresolvedY();
        unresolvedWidth = layout.contentWidth();
        unresolvedHeight = 10;
        if (!unresolved.isEmpty()) {
            context.drawTextWithShadow(textRenderer, ellipsize(unresolved, layout.contentWidth()),
                    layout.contentX(), layout.unresolvedY(), WARNING);
        }

        String customSpecies = ExportStatusText.customSpeciesLine(report);
        if (!customSpecies.isEmpty()) {
            context.drawTextWithShadow(textRenderer, ellipsize(customSpecies, layout.contentWidth()),
                    layout.contentX(), layout.customSpeciesY(), MUTED);
        }

        statusX = layout.contentX();
        statusY = layout.statusY();
        statusWidth = layout.contentWidth();
        statusHeight = 10;
        if (status != null) {
            context.drawTextWithShadow(textRenderer, ellipsize(status.text(), layout.contentWidth()),
                    layout.contentX(), layout.statusY(), toneColor(status.tone()));
        }

        if (!unresolved.isEmpty() && inside(mouseX, mouseY, unresolvedX, unresolvedY,
                unresolvedWidth, unresolvedHeight)) {
            List<Text> tooltip = ExportStatusText.unresolvedTooltip(report).stream()
                    .<Text>map(Text::literal)
                    .toList();
            if (!tooltip.isEmpty()) {
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        } else if (status != null && inside(mouseX, mouseY, statusX, statusY, statusWidth, statusHeight)) {
            context.drawTooltip(textRenderer, List.of(Text.literal(status.text())), mouseX, mouseY);
        }
    }

    private void drawSectionHeading(DrawContext context, String heading, int y) {
        context.drawTextWithShadow(textRenderer, heading, layout.contentX(), y, LABEL);
        context.fill(layout.contentX(), y + 10,
                layout.contentX() + layout.contentWidth(), y + 11, DIVIDER);
    }

    private String ellipsize(String text, int maxWidth) {
        if (text == null || text.isEmpty() || textRenderer.getWidth(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int available = Math.max(0, maxWidth - textRenderer.getWidth(ellipsis));
        return textRenderer.trimToWidth(text, available) + ellipsis;
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private int toneColor(ExportStatusText.Tone tone) {
        return switch (tone) {
            case MUTED -> MUTED;
            case SUCCESS -> SUCCESS;
            case WARNING -> WARNING;
            case ERROR -> ERROR;
        };
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
