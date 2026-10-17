package com.cobbletown.pcexporter.snapshot;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PcPathsTest {

    private static final Path DEFAULT = Path.of("/game/config/pc-exporter/snapshots");
    private static final Path GAME = Path.of("/game");

    @Test
    void nullOrBlankUsesDefault() {
        assertEquals(DEFAULT, PcPaths.resolveOutputDir(DEFAULT, GAME, null));
        assertEquals(DEFAULT, PcPaths.resolveOutputDir(DEFAULT, GAME, "   "));
        assertEquals(DEFAULT, PcPaths.resolveOutputDir(DEFAULT, GAME, "\n"));
    }

    @Test
    void absoluteOverrideIsUsedAsIs() {
        assertEquals(Path.of("/work/pc-exporter/exports"),
                PcPaths.resolveOutputDir(DEFAULT, GAME, "/work/pc-exporter/exports"));
    }

    @Test
    void relativeOverrideResolvesAgainstGameDir() {
        assertEquals(Path.of("/game/exports"),
                PcPaths.resolveOutputDir(DEFAULT, GAME, "exports"));
    }

    @Test
    void trailingWhitespaceIsTrimmed() {
        assertEquals(Path.of("/work/exports"),
                PcPaths.resolveOutputDir(DEFAULT, GAME, "  /work/exports\n"));
    }

    @Test
    void configuredDirIsNullWhenUnset() {
        assertNull(PcPaths.resolveConfiguredDir(GAME, null));
        assertNull(PcPaths.resolveConfiguredDir(GAME, "   "));
        assertNull(PcPaths.resolveConfiguredDir(GAME, "\n"));
    }

    @Test
    void configuredDirResolvesAbsoluteAndRelative() {
        assertEquals(Path.of("/work/exports"), PcPaths.resolveConfiguredDir(GAME, "/work/exports"));
        assertEquals(Path.of("/game/exports"), PcPaths.resolveConfiguredDir(GAME, "  exports\n"));
    }
}
