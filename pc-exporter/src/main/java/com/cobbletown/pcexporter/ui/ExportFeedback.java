package com.cobbletown.pcexporter.ui;

import com.cobbletown.pcexporter.snapshot.ExportResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.file.Path;

/**
 * Concise in-game feedback. Success shows the exported count plus clickable {@code [Open Folder]}
 * and {@code [Change Folder]} actions backed by client-side commands.
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
            case SUCCESS -> {
                client.player.sendMessage(
                        Text.literal("PC Exporter: Exported " + result.count + " Pokémon.")
                                .formatted(Formatting.GREEN), false);
                client.player.sendMessage(actionsLine(), false);
            }
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

    public static void folderSelectionCancelled(boolean exporting) {
        send(Text.literal(exporting
                        ? "PC Exporter: export cancelled — no folder selected."
                        : "PC Exporter: export folder unchanged.")
                .formatted(Formatting.YELLOW));
    }

    public static void folderChanged(Path dir) {
        send(Text.literal("PC Exporter: export folder set to " + dir + ".")
                .formatted(Formatting.GREEN));
    }

    public static void error(String message) {
        send(Text.literal("PC Exporter: " + message).formatted(Formatting.RED));
    }

    /** The clickable action line. Pure data, so it is unit-testable without a running client. */
    public static MutableText actionsLine() {
        return action("[Open Folder]", ExportCommands.OPEN_FOLDER_COMMAND,
                "Open the export folder in your file browser")
                .append(Text.literal("  "))
                .append(action("[Change Folder]", ExportCommands.CHANGE_FOLDER_COMMAND,
                        "Choose a different export folder"));
    }

    private static MutableText action(String label, String command, String hover) {
        return Text.literal(label).setStyle(Style.EMPTY
                .withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(hover))));
    }

    private static void send(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) {
            client.player.sendMessage(text, false);
        }
    }
}
