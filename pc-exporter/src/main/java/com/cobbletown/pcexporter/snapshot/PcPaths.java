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
 * Resolves the snapshot output location. Never a tracked repository path.
 *
 * <p>The default is {@code <gameDir>/config/pc-exporter/snapshots}. A configured override lives in
 * {@code config/pc-exporter/output-dir.txt} (one path; absolute, or relative to the game
 * directory). An existing, valid override counts as an already-configured location, so the folder
 * picker is not shown again. The override is instance configuration, so no machine-specific path
 * is baked into the source.
 */
public final class PcPaths {
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS'Z'").withZone(ZoneOffset.UTC);

    private PcPaths() {
    }

    public static Path configRoot() {
        return FabricLoader.getInstance().getConfigDir().resolve("pc-exporter");
    }

    public static Path defaultSnapshotsDir() {
        return configRoot().resolve("snapshots");
    }

    public static Path outputDirFile() {
        return configRoot().resolve("output-dir.txt");
    }

    /** The configured export directory, or {@code null} when none is configured. */
    public static Path configuredDir() {
        return resolveConfiguredDir(FabricLoader.getInstance().getGameDir(), readOverride());
    }

    /** True when the player has already chosen an export directory (or one is pre-configured). */
    public static boolean isConfigured() {
        return configuredDir() != null;
    }

    /** The directory snapshots go to: the configured one, else the default. */
    public static Path snapshotsDir() {
        Path configured = configuredDir();
        return configured != null ? configured : defaultSnapshotsDir();
    }

    public static String snapshotFileName() {
        return "pc-" + STAMP.format(Instant.now()) + ".json";
    }

    public static Path nextSnapshotPath() {
        return snapshotsDir().resolve(snapshotFileName());
    }

    /**
     * Persists the chosen export directory to {@code output-dir.txt}. Returns false on failure
     * (in which case the previous configuration is left untouched).
     */
    public static boolean setOutputDir(Path dir) {
        if (dir == null) {
            return false;
        }
        try {
            Files.createDirectories(configRoot());
            Files.writeString(outputDirFile(), dir.toAbsolutePath().normalize() + System.lineSeparator());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Pure: resolves a configured override, or {@code null} when blank/invalid. */
    static Path resolveConfiguredDir(Path gameDir, String override) {
        if (override == null || override.isBlank()) {
            return null;
        }
        try {
            Path candidate = Path.of(override.trim());
            if (!candidate.isAbsolute()) {
                candidate = gameDir.resolve(candidate);
            }
            return candidate.normalize();
        } catch (InvalidPathException invalid) {
            return null;
        }
    }

    /** Pure: the configured directory, else the default. Kept for unit testing. */
    static Path resolveOutputDir(Path defaultDir, Path gameDir, String override) {
        Path configured = resolveConfiguredDir(gameDir, override);
        return configured != null ? configured : defaultDir;
    }

    private static String readOverride() {
        Path overrideFile = outputDirFile();
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
