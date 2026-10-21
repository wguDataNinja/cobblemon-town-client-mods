package com.cobbletown.collectionexporter.data;

import java.util.List;
import java.util.Objects;

/**
 * One read-only observation of the client-resident PC.
 *
 * <p>{@code completeness} is part of the data contract. A report must expose it and must not
 * describe a partial observation as a complete inventory.</p>
 */
public record PcInventorySnapshot(
        String capturedAt,
        String source,
        String storeUuid,
        InventoryCompleteness completeness,
        int boxCount,
        int observedBoxCount,
        List<PokemonRecord> entries,
        List<String> diagnostics
) {
    public PcInventorySnapshot {
        Objects.requireNonNull(capturedAt, "capturedAt");
        source = source == null ? "cobblemon-client-pc" : source;
        completeness = completeness == null ? InventoryCompleteness.UNAVAILABLE : completeness;
        entries = entries == null ? List.of() : List.copyOf(entries);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public static PcInventorySnapshot unavailable(String capturedAt, String diagnostic) {
        return new PcInventorySnapshot(
                capturedAt,
                "cobblemon-client-pc",
                null,
                InventoryCompleteness.UNAVAILABLE,
                0,
                0,
                List.of(),
                List.of(diagnostic));
    }
}
