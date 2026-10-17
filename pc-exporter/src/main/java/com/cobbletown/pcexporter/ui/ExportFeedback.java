package com.cobbletown.pcexporter.ui;

import com.cobbletown.pcexporter.snapshot.ExportResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;

/**
 * Concise in-game feedback for an export attempt. Client-local chat only.
 */
public final class ExportFeedback {
    private ExportFeedback() {
    }

    public static void report(ExportResult result) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        switch (result.status) {
            case SUCCESS -> client.player.sendMessage(
                    Text.literal("PC Exporter: exported " + result.count + " Pokémon -> " + fileName(result.path))
                            .formatted(Formatting.GREEN), false);
            case BUSY -> client.player.sendMessage(
                    Text.literal("PC Exporter: an export is already in progress")
                            .formatted(Formatting.YELLOW), false);
            case UNAVAILABLE -> client.player.sendMessage(
                    Text.literal("PC Exporter: " + result.error + " — nothing written")
                            .formatted(Formatting.RED), false);
            case ERROR -> client.player.sendMessage(
                    Text.literal("PC Exporter: " + result.error)
                            .formatted(Formatting.RED), false);
        }
    }

    private static String fileName(String path) {
        if (path == null) {
            return "?";
        }
        try {
            return Path.of(path).getFileName().toString();
        } catch (RuntimeException e) {
            return path;
        }
    }
}
