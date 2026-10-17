package com.cobbletown.pcexporter.ui;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobbletown.pcexporter.platform.NativeFolders;
import com.cobbletown.pcexporter.snapshot.ExportResult;
import com.cobbletown.pcexporter.snapshot.PcPaths;
import com.cobbletown.pcexporter.snapshot.PcSnapshotService;
import net.minecraft.client.MinecraftClient;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Orchestrates the Export button and the chat actions.
 *
 * <p>First export (no configured folder) opens a native folder picker, persists the choice, then
 * exports into it; cancelling cancels the export and leaves configuration untouched. Later exports
 * go straight to the configured folder. The picker runs on a background thread; results are
 * marshalled back to the render thread.
 */
public final class ExportController {
    private static final String PICKER_TITLE = "Select the PC Exporter export folder";

    private ExportController() {
    }

    /** Export button pressed. */
    public static void onExport(PCGUI pcGui) {
        if (PcPaths.isConfigured()) {
            exportNow(pcGui);
            return;
        }
        pickFolder(selected -> exportNow(pcGui), true);
    }

    /** Chat action: choose a different export folder (does not export). */
    public static void changeFolder() {
        pickFolder(ExportFeedback::folderChanged, false);
    }

    /** Chat action: open the configured folder in the OS file browser. */
    public static void openFolder() {
        Path dir = PcPaths.configuredDir();
        Path target = dir != null ? dir : PcPaths.defaultSnapshotsDir();
        runAsync("open-folder", () -> {
            if (!NativeFolders.openFolder(target)) {
                ExportFeedback.error("could not open the export folder: " + target);
            }
        });
    }

    private static void exportNow(PCGUI pcGui) {
        ExportResult result = PcSnapshotService.export(pcGui.getPc(), PcPaths.snapshotsDir());
        ExportFeedback.report(result);
    }

    private static void pickFolder(Consumer<Path> onChosen, boolean exporting) {
        Path initial = PcPaths.configuredDir();
        String defaultPath = (initial != null ? initial : PcPaths.defaultSnapshotsDir()).toString();
        runAsync("folder-picker", () -> {
            String selected = NativeFolders.selectFolder(PICKER_TITLE, defaultPath);
            MinecraftClient client = MinecraftClient.getInstance();
            client.execute(() -> {
                if (selected == null) {
                    ExportFeedback.folderSelectionCancelled(exporting);
                    return;
                }
                Path chosen;
                try {
                    chosen = Path.of(selected);
                } catch (InvalidPathException invalid) {
                    ExportFeedback.error("the selected folder path is not valid");
                    return;
                }
                if (!PcPaths.setOutputDir(chosen)) {
                    ExportFeedback.error("could not save the export folder (previous setting kept)");
                    return;
                }
                onChosen.accept(chosen);
            });
        });
    }

    private static void runAsync(String name, Runnable task) {
        Thread thread = new Thread(task, "pc-exporter-" + name);
        thread.setDaemon(true);
        thread.start();
    }
}
