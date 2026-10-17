package com.cobbletown.pcexporter.snapshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Deterministic JSON serialization for a snapshot. Kept independent of any screen/mixin code.
 */
public final class SnapshotJson {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    private SnapshotJson() {
    }

    public static String toJson(PcSnapshot snapshot) {
        return GSON.toJson(snapshot);
    }
}
