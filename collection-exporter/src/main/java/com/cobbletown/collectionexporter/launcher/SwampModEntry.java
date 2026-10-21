package com.cobbletown.collectionexporter.launcher;

import net.minecraft.client.gui.screen.Screen;

import java.util.Objects;

/** A descriptor plus the lazy factory for its native screen. */
public record SwampModEntry(SwampModDescriptor descriptor, ScreenFactory screenFactory) {
    public SwampModEntry {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(screenFactory, "screenFactory");
    }

    @FunctionalInterface
    public interface ScreenFactory {
        Screen create(Screen parent);
    }
}
