package com.cobbletown.trashwarning;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.Screen;

import java.util.IdentityHashMap;
import java.util.Map;

/** Registers only per-screen visual work; it never observes or changes inventory input. */
public final class TrashWarningClient implements ClientModInitializer {
    private final Map<Screen, TrashWarningRenderer.Layout> layouts = new IdentityHashMap<>();

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            TrashWarningRenderer.Layout layout = TrashScreenClassifier.isObservedTrash(screen)
                ? TrashWarningRenderer.layout(client.textRenderer, width, height) : null;
            if (layout == null) {
                layouts.remove(screen);
                return;
            }
            layouts.put(screen, layout);
            ScreenEvents.afterRender(screen).register((current, context, mouseX, mouseY, delta) -> {
                // AFTER_INIT can recur for resize; stale callbacks must not double-draw.
                if (layouts.get(current) == layout) TrashWarningRenderer.render(layout, context, client.textRenderer);
            });
            ScreenEvents.remove(screen).register(layouts::remove);
        });
    }
}
