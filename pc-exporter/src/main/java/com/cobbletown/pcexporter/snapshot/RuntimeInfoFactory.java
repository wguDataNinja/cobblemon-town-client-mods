package com.cobbletown.pcexporter.snapshot;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;

import java.util.Optional;

/**
 * Best-effort discovery of the runtime versions relevant to interpreting a snapshot.
 * All fields are best-effort; unknown values are null rather than invented.
 */
public final class RuntimeInfoFactory {
    private RuntimeInfoFactory() {
    }

    public static PcSnapshot.RuntimeInfo capture() {
        PcSnapshot.RuntimeInfo info = new PcSnapshot.RuntimeInfo();
        FabricLoader loader = FabricLoader.getInstance();
        info.fabricLoader = modVersion(loader, "fabricloader");
        info.fabricApi = modVersion(loader, "fabric-api");
        info.minecraft = modVersion(loader, "minecraft");
        info.cobblemon = modVersion(loader, "cobblemon");
        info.java = System.getProperty("java.version");
        return info;
    }

    private static String modVersion(FabricLoader loader, String id) {
        Optional<ModContainer> mod = loader.getModContainer(id);
        return mod.map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse(null);
    }
}
