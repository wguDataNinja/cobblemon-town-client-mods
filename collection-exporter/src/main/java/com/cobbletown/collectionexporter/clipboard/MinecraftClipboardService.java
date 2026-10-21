package com.cobbletown.collectionexporter.clipboard;

import net.minecraft.client.MinecraftClient;

/**
 * Writes through Minecraft's native keyboard/clipboard bridge.
 *
 * <p>Call from the client thread. The adapter is deliberately tiny; it does not write files or
 * use an OS-specific command.</p>
 */
public final class MinecraftClipboardService implements ClipboardService {
    @Override
    public ClipboardResult copy(String text) {
        if (text == null) {
            return ClipboardResult.failure("no text to copy");
        }
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.keyboard == null) {
                return ClipboardResult.failure("Minecraft clipboard is unavailable");
            }
            client.keyboard.setClipboard(text);
            return ClipboardResult.success(text.length());
        } catch (RuntimeException failure) {
            return ClipboardResult.failure("clipboard write failed: " + failure.getClass().getSimpleName());
        }
    }
}
