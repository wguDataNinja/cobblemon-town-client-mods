package com.cobbletown.collectionexporter.data;

/** Read-only access to the player's client-resident PC. */
@FunctionalInterface
public interface PcInventoryReader {
    PcInventorySnapshot read();
}
