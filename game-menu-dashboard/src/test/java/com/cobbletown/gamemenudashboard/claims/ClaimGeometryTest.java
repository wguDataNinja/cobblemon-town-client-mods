package com.cobbletown.gamemenudashboard.claims;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClaimGeometryTest {
    private final ClaimGeometry geometry = new ClaimGeometry("overworld", -20, -64, -10, 20, 319, 10);

    @Test void acceptsInclusiveBlockBoundsAndMinecraftDimensionAlias() {
        assertTrue(geometry.containsBlock("minecraft:overworld", -20, -10));
        assertTrue(geometry.containsBlock("overworld", 20, 10));
    }

    @Test void rejectsOtherDimensionAndOutOfBoundsBlocks() {
        assertFalse(geometry.containsBlock("minecraft:the_nether", 0, 0));
        assertFalse(geometry.containsBlock("overworld", 21, 0));
    }
}
