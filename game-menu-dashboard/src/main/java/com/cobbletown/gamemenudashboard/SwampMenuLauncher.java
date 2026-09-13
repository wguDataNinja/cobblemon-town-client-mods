package com.cobbletown.gamemenudashboard;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Compact v1 entry point used only when Essential controls are actually mounted. */
final class SwampMenuLauncher {
    private static final Map<Screen, List<ClickableWidget>> LAUNCHERS = new IdentityHashMap<>();

    private SwampMenuLauncher() { }

    static void attach(Screen pauseScreen, int width, int height) {
        remove(pauseScreen);
        List<PauseMenuFallback.Bounds> occupied = Screens.getButtons(pauseScreen).stream()
            .map(widget -> new PauseMenuFallback.Bounds(widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight()))
            .toList();
        PauseMenuFallback.launcherPlacement(width, height, occupied).ifPresent(placement -> {
            ButtonWidget launcher = ButtonWidget.builder(Text.literal("Swamp Menu"), button ->
                    MinecraftClient.getInstance().setScreen(new SwampMenuScreen(pauseScreen)))
                .dimensions(placement.x(), placement.y(), placement.width(), placement.height())
                .build();
            Screens.getButtons(pauseScreen).add(launcher);
            LAUNCHERS.put(pauseScreen, new ArrayList<>(List.of(launcher)));
        });
        ScreenEvents.remove(pauseScreen).register(removed -> remove(removed));
    }

    private static void remove(Screen screen) {
        List<ClickableWidget> widgets = LAUNCHERS.remove(screen);
        if (widgets != null) Screens.getButtons(screen).removeAll(widgets);
    }
}
