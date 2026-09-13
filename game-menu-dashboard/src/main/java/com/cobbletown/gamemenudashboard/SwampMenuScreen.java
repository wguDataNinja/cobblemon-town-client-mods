package com.cobbletown.gamemenudashboard;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Swamp-owned child screen retaining the originating Game Menu as its parent. */
final class SwampMenuScreen extends Screen {
    private final Screen parent;

    SwampMenuScreen(Screen parent) {
        super(Text.literal("Swamp Menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), button -> close())
            .dimensions(width - 78, 6, 72, 20).build());
        GameMenuDashboardPanel.attachOwned(this, width, height);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(parent);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, getTitle(), width / 2, 12, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }
}
