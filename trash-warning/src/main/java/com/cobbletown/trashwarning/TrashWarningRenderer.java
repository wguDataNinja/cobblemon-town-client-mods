package com.cobbletown.trashwarning;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Additive visual-only renderer. All layout is calculated once per screen initialization. */
public final class TrashWarningRenderer {
    private static final int RED = 0xFFFF4040;
    // Vanilla generic-container panel gray in the observed 1.21.1 screen texture.
    private static final int TITLE_PANEL_GRAY = 0xFFC6C6C6;
    private static final int PRESENTATION_Z = 200;
    private static final Text HEADING = Text.translatable("trash-warning.heading");
    private static final Text LINE_ONE = Text.translatable("trash-warning.warning_line_one");
    private static final Text LINE_TWO = Text.translatable("trash-warning.warning_line_two");

    private TrashWarningRenderer() { }

    public static Layout layout(TextRenderer renderer, int scaledWidth, int scaledHeight) {
        int guiX = (scaledWidth - 176) / 2;
        int guiY = (scaledHeight - 168) / 2;
        return new Layout(
            guiX + 5,
            guiY + 4,
            guiX + 171,
            guiY + 17,
            guiX + (176 - renderer.getWidth(HEADING)) / 2,
            guiY + 6,
            guiX + (176 - renderer.getWidth(LINE_ONE)) / 2,
            guiY - 19,
            guiX + (176 - renderer.getWidth(LINE_TWO)) / 2,
            guiY - 10
        );
    }

    public static void render(Layout layout, DrawContext context, TextRenderer renderer) {
        // afterRender follows vanilla foreground text; raise this tiny presentation layer above it.
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, PRESENTATION_Z);
        context.fill(layout.titleCoverLeft(), layout.titleCoverTop(), layout.titleCoverRight(), layout.titleCoverBottom(), TITLE_PANEL_GRAY);
        context.drawText(renderer, HEADING, layout.headingX(), layout.headingY(), RED, false);
        context.drawTextWithShadow(renderer, LINE_ONE, layout.lineOneX(), layout.lineOneY(), RED);
        context.drawTextWithShadow(renderer, LINE_TWO, layout.lineTwoX(), layout.lineTwoY(), RED);
        context.getMatrices().pop();
    }

    public record Layout(
        int titleCoverLeft,
        int titleCoverTop,
        int titleCoverRight,
        int titleCoverBottom,
        int headingX,
        int headingY,
        int lineOneX,
        int lineOneY,
        int lineTwoX,
        int lineTwoY
    ) { }
}
