package com.cobbletown.pcexporter.snapshot;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Resolves the private snapshot output location. Never a tracked repository path.
 *
 * <p>The default is {@code <gameDir>/config/pc-exporter/snapshots}. An optional override
 * file at {@code config/pc-exporter/output-dir.txt} may contain one path (absolute, or
 * relative to the game directory) to redirect snapshots elsewhere. The override lives in
 * instance configuration, not in this source, so no machine-specific path is baked in.
 */
public final class PcPaths {
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'").withZone(ZoneOffset.UTC);

    private PcPaths() {
    }

    public static Path snapshotsDir() {
        FabricLoader loader = FabricLoader.getInstance();
        Path configDir = loader.getConfigDir();
        Path defaultDir = configDir.resolve("pc-exporter").resolve("snapshots");
        Path gameDir = loader.getGameDir();
        return resolveOutputDir(defaultDir, gameDir, readOverride(configDir));
    }

    public static Path nextSnapshotPath() {
        return snapshotsDir().resolve("pc-" + STAMP.format(Instant.now()) + ".json");
    }

    /** Pure resolution rule so the override behavior is unit-testable without Fabric. */
    static Path resolveOutputDir(Path defaultDir, Path gameDir, String override) {
        if (override == null || override.isBlank()) {
            return defaultDir;
        }
        try {
            Path candidate = Path.of(override.trim());
            if (!candidate.isAbsolute()) {
                candidate = gameDir.resolve(candidate);
            }
            return candidate.normalize();
        } catch (InvalidPathException invalid) {
            return defaultDir;
        }
    }

    private static String readOverride(Path configDir) {
        Path overrideFile = configDir.resolve("pc-exporter").resolve("output-dir.txt");
        try {
            if (Files.isRegularFile(overrideFile)) {
                return Files.readString(overrideFile);
            }
        } catch (IOException ignored) {
            // Fail open to the default location.
        }
        return null;
    }
}
