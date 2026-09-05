package com.cobbletown.eggbadges;

import java.util.Arrays;
import java.util.BitSet;

/** Active masks are rendered; pending masks are rebuilt and swapped atomically. */
final class BadgeMaskBuffers {
    private int[] active = new int[0];
    private int[] pending = new int[0];
    private final BitSet activeMarked = new BitSet();
    private final BitSet pendingMarked = new BitSet();
    private final BitSet swapMarked = new BitSet();

    void resize(int size) {
        if (active.length == size) return;
        active = new int[size];
        pending = new int[size];
        activeMarked.clear();
        pendingMarked.clear();
    }

    void clearAll() {
        Arrays.fill(active, 0);
        Arrays.fill(pending, 0);
        activeMarked.clear();
        pendingMarked.clear();
    }

    void clearPending() {
        Arrays.fill(pending, 0);
        pendingMarked.clear();
    }

    void setPending(int slot, int mask) {
        pending[slot] = mask;
        if (mask == 0) pendingMarked.clear(slot); else pendingMarked.set(slot);
    }

    void setActive(int slot, int mask) {
        active[slot] = mask;
        if (mask == 0) activeMarked.clear(slot); else activeMarked.set(slot);
    }

    void clearActive(int slot) { setActive(slot, 0); }

    void swap() {
        int[] masks = active; active = pending; pending = masks;
        swapMarked.clear(); swapMarked.or(activeMarked);
        activeMarked.clear(); activeMarked.or(pendingMarked);
        pendingMarked.clear(); pendingMarked.or(swapMarked);
    }

    int nextActive(int from) { return activeMarked.nextSetBit(from); }
    int activeMask(int slot) { return active[slot]; }
    int size() { return active.length; }
}
