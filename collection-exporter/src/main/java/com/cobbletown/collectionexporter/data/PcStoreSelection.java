package com.cobbletown.collectionexporter.data;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Pure selection policy for Cobblemon's client PC stores.
 *
 * <p>The store matching the local player UUID is the only unambiguous choice. A single
 * non-matching store is a bounded fallback and must be reported as partial; multiple
 * non-matching stores are ambiguous and must not be read.</p>
 */
public final class PcStoreSelection {
    public enum Status {
        OWN_STORE,
        SINGLE_STORE_FALLBACK,
        AMBIGUOUS,
        NONE
    }

    public record Selection<T>(Status status, T store, UUID storeId) {
    }

    private PcStoreSelection() {
    }

    public static <T> Selection<T> select(Map<UUID, T> stores, UUID playerId) {
        Objects.requireNonNull(stores, "stores");
        if (stores.isEmpty()) {
            return new Selection<>(Status.NONE, null, null);
        }
        if (playerId != null) {
            T own = stores.get(playerId);
            if (own != null) {
                return new Selection<>(Status.OWN_STORE, own, playerId);
            }
        }
        if (stores.size() == 1) {
            Map.Entry<UUID, T> only = stores.entrySet().iterator().next();
            return new Selection<>(Status.SINGLE_STORE_FALLBACK, only.getValue(), only.getKey());
        }
        return new Selection<>(Status.AMBIGUOUS, null, null);
    }
}
