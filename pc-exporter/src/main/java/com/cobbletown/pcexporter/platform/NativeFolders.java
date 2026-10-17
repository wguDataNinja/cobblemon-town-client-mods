package com.cobbletown.pcexporter.platform;

import com.cobbletown.pcexporter.PcExporterMod;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Native OS folder picker and file-browser helpers.
 *
 * <p>The folder picker uses LWJGL's tinyfd. On macOS tinyfd shells out to {@code osascript}, so it
 * is safe off the render thread; on Windows it uses a native COM dialog. Callers run these on a
 * background thread and marshal results back to the render thread.
 */
public final class NativeFolders {
    private NativeFolders() {
    }

    /** Opens a native folder picker. Returns the selected path, or null when cancelled or failed. */
    public static String selectFolder(String title, String defaultPath) {
        try {
            String selected = TinyFileDialogs.tinyfd_selectFolderDialog(
                    title, defaultPath == null ? "" : defaultPath);
            return selected == null || selected.isBlank() ? null : selected;
        } catch (Throwable t) {
            PcExporterMod.LOGGER.warn("PC Exporter: folder picker failed: {}", t.toString());
            return null;
        }
    }

    /** Opens the directory in the OS file browser. Returns false when it cannot be opened. */
    public static boolean openFolder(Path dir) {
        try {
            if (dir == null || !Files.isDirectory(dir)) {
                return false;
            }
            String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
            List<String> command;
            if (os.contains("win")) {
                command = List.of("explorer", dir.toString());
            } else if (os.contains("mac")) {
                command = List.of("open", dir.toString());
            } else {
                command = List.of("xdg-open", dir.toString());
            }
            new ProcessBuilder(command).start();
            return true;
        } catch (Throwable t) {
            PcExporterMod.LOGGER.warn("PC Exporter: could not open folder: {}", t.toString());
            return false;
        }
    }
}
