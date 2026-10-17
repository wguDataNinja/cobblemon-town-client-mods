package com.cobbletown.pcexporter.snapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A complete exported PC observation. Pure data so it is unit-testable.
 *
 * <p>Scope semantics (see {@link #completeness}): a snapshot states what was actually
 * observed. {@link #completeness} distinguishes a full synchronized-PC traverse from a
 * partial observation, so consumers must not read a partial capture as "the player does
 * not own X".
 */
public final class PcSnapshot {
    public static final int SCHEMA_VERSION = 1;

    public int schemaVersion = SCHEMA_VERSION;
    public String producer;
    public String producerVersion;
    public RuntimeInfo runtime;

    public String generatedAt;
    public String source;

    public StoreInfo store;

    /** Retained for existing consumers; equals {@code store.boxCount}. */
    public int boxCount;

    /** Self-describing indexing contract (consumers must not assume otherwise). */
    public String boxIndexing = "zero-based-store";
    public String slotIndexing = "zero-based-box";

    /** "complete-synchronized-store" or "partial-observed". */
    public String completeness = "partial-observed";

    public List<PcEntry> entries = new ArrayList<>();

    public PcSnapshot() {
    }

    /** Runtime versions relevant to interpreting this snapshot. */
    public static final class RuntimeInfo {
        public String minecraft;
        public String cobblemon;
        public String fabricLoader;
        public String fabricApi;
        public String java;

        public RuntimeInfo() {
        }
    }

    /** The PC store this snapshot came from. */
    public static final class StoreInfo {
        public String uuid;
        public int boxCount;
        public int observedBoxCount;

        public StoreInfo() {
        }
    }
}
