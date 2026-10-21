package com.cobbletown.collectionexporter.launcher;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;

/** Native selector for installed Swamp Mods. It never lists an unavailable feature. */
public final class SwampModsScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;

    private final Screen parent;
    private final List<SwampModDescriptor> entries;

    public SwampModsScreen(Screen parent, List<SwampModDescriptor> entries) {
        super(Text.literal("Swamp Mods"));
        this.parent = parent;
        this.entries = List.copyOf(entries);
    }

    @Override
    protected void init() {
        int x = (width - BUTTON_WIDTH) / 2;
        int start = Math.max(38, height / 4);
        for (int index = 0; index < entries.size(); index++) {
            SwampModDescriptor descriptor = entries.get(index);
            int y = start + index * 24;
            if (y + 20 > height - 32) {
                break;
            }
            ButtonWidget button = ButtonWidget.builder(
                            Text.literal(descriptor.displayName()),
                            pressed -> SwampMenuIntegration.open(
                                    MinecraftClient.getInstance(), this, descriptor))
                    .dimensions(x, y, BUTTON_WIDTH, 20)
                    .tooltip(Tooltip.of(Text.literal(descriptor.description())))
                    .build();
            addDrawableChild(button);
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> close())
                .dimensions((width - 100) / 2, height - 28, 100, 20)
                .build());
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 16, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
