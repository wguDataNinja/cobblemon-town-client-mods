package com.cobbletown.collectionexporter.data;

/**
 * How much of the player's PC the client actually had synchronized when the inventory was read.
 *
 * <p>{@link #COMPLETE_SYNCHRONIZED_STORE} means the whole client-resident store was traversed.
 * It does not prove server-side truth beyond the last synchronization. Anything else must never
 * be presented as a complete inventory.</p>
 */
public enum InventoryCompleteness {
    COMPLETE_SYNCHRONIZED_STORE(
            true,
            "complete client-synchronized PC store"),
    PARTIAL_SINGLE_STORE_FALLBACK(
            false,
            "partial: single PC store found but ownership was not proven"),
    PARTIAL_INCOMPLETE_STORE(
            false,
            "partial: the synchronized PC store was incomplete"),
    AMBIGUOUS_MULTIPLE_STORES(
            false,
            "partial: multiple PC stores found and none matched the player"),
    UNAVAILABLE(
            false,
            "unavailable: no synchronized PC store is present");

    private final boolean complete;
    private final String label;

    InventoryCompleteness(boolean complete, String label) {
        this.complete = complete;
        this.label = label;
    }

    public boolean isComplete() {
        return complete;
    }

    public String label() {
        return label;
    }
}
