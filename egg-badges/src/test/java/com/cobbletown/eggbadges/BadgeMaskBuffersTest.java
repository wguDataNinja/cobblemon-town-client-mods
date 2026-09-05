package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BadgeMaskBuffersTest {
    @Test void pendingResultsBecomeVisibleOnlyAtSwap() {
        BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        buffers.resize(3);
        buffers.setActive(0, 1);
        buffers.setPending(1, 2);

        assertEquals(1, buffers.activeMask(0));
        assertEquals(0, buffers.activeMask(1));
        buffers.swap();
        assertEquals(0, buffers.activeMask(0));
        assertEquals(2, buffers.activeMask(1));
        assertEquals(1, buffers.nextActive(0));
    }

    @Test void changedSlotCanBeClearedWithoutClearingUnchangedBadges() {
        BadgeMaskBuffers buffers = new BadgeMaskBuffers();
        buffers.resize(3);
        buffers.setActive(0, 1);
        buffers.setActive(2, 4);
        buffers.clearActive(0);

        assertEquals(2, buffers.nextActive(0));
        assertEquals(4, buffers.activeMask(2));
    }
}
