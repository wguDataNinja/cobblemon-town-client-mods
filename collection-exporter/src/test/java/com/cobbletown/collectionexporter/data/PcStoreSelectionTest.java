package com.cobbletown.collectionexporter.data;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PcStoreSelectionTest {
    private static final UUID PLAYER = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void selectsOwnStoreByPlayerUuid() {
        Map<UUID, String> stores = new LinkedHashMap<>();
        stores.put(OTHER, "other");
        stores.put(PLAYER, "own");
        PcStoreSelection.Selection<String> result = PcStoreSelection.select(stores, PLAYER);
        assertEquals(PcStoreSelection.Status.OWN_STORE, result.status());
        assertEquals("own", result.store());
        assertEquals(PLAYER, result.storeId());
    }

    @Test
    void singleNonMatchingStoreIsFallback() {
        PcStoreSelection.Selection<String> result = PcStoreSelection.select(Map.of(OTHER, "other"), PLAYER);
        assertEquals(PcStoreSelection.Status.SINGLE_STORE_FALLBACK, result.status());
        assertEquals("other", result.store());
        assertEquals(OTHER, result.storeId());
    }

    @Test
    void multipleNonMatchingStoresAreAmbiguous() {
        Map<UUID, String> stores = new LinkedHashMap<>();
        stores.put(OTHER, "other");
        stores.put(UUID.fromString("00000000-0000-0000-0000-000000000003"), "third");
        PcStoreSelection.Selection<String> result = PcStoreSelection.select(stores, PLAYER);
        assertEquals(PcStoreSelection.Status.AMBIGUOUS, result.status());
        assertNull(result.store());
    }

    @Test
    void noStoresIsNone() {
        PcStoreSelection.Selection<String> result = PcStoreSelection.select(Map.of(), PLAYER);
        assertEquals(PcStoreSelection.Status.NONE, result.status());
        assertNull(result.store());
    }

    @Test
    void nullPlayerUsesSingleFallback() {
        PcStoreSelection.Selection<String> result = PcStoreSelection.select(Map.of(OTHER, "other"), null);
        assertEquals(PcStoreSelection.Status.SINGLE_STORE_FALLBACK, result.status());
        assertEquals("other", result.store());
    }
}
