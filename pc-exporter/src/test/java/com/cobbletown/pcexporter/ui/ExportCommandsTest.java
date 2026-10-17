package com.cobbletown.pcexporter.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportCommandsTest {

    @Test
    void clickCommandsMatchTheClientCommandRoot() {
        assertEquals("/pcexporter openfolder", ExportCommands.OPEN_FOLDER_COMMAND);
        assertEquals("/pcexporter changefolder", ExportCommands.CHANGE_FOLDER_COMMAND);
        assertTrue(ExportCommands.OPEN_FOLDER_COMMAND.startsWith("/" + ExportCommands.ROOT + " "));
        assertTrue(ExportCommands.CHANGE_FOLDER_COMMAND.startsWith("/" + ExportCommands.ROOT + " "));
    }
}
