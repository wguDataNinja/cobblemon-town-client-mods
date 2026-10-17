package com.cobbletown.pcexporter.ui;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

/**
 * Client-side commands backing the clickable chat actions. Registered on the client dispatcher, so
 * they run locally and are never sent to the server.
 */
public final class ExportCommands {
    public static final String ROOT = "pcexporter";
    public static final String OPEN_FOLDER_COMMAND = "/" + ROOT + " openfolder";
    public static final String CHANGE_FOLDER_COMMAND = "/" + ROOT + " changefolder";

    private ExportCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal(ROOT)
                        .then(ClientCommandManager.literal("openfolder")
                                .executes(context -> {
                                    ExportController.openFolder();
                                    return 1;
                                }))
                        .then(ClientCommandManager.literal("changefolder")
                                .executes(context -> {
                                    ExportController.changeFolder();
                                    return 1;
                                }))));
    }
}
