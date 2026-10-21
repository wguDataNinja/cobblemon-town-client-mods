package com.cobbletown.collectionexporter.launcher;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwampLauncherPlacementTest {
    private static final SwampLauncherPlacement.Bounds EXIT =
            new SwampLauncherPlacement.Bounds(40, 200, 204, 20);

    @Test
    void prefersTheGapDirectlyBelowTheExitButton() {
        Optional<SwampLauncherPlacement.Bounds> placement = SwampLauncherPlacement.choose(
                320, 300, EXIT, List.of(EXIT), 204);
        assertTrue(placement.isPresent());
        assertEquals(224, placement.orElseThrow().y());
        assertEquals(40, placement.orElseThrow().x());
    }

    @Test
    void usesTheGapAboveWhenBelowIsOccupied() {
        SwampLauncherPlacement.Bounds other =
                new SwampLauncherPlacement.Bounds(40, 224, 204, 20);
        Optional<SwampLauncherPlacement.Bounds> placement = SwampLauncherPlacement.choose(
                320, 300, EXIT, List.of(EXIT, other), 204);
        assertTrue(placement.isPresent());
        assertEquals(176, placement.orElseThrow().y());
    }

    @Test
    void fallsBackToAnUnoccupiedScreenEdge() {
        SwampLauncherPlacement.Bounds other =
                new SwampLauncherPlacement.Bounds(40, 176, 204, 20);
        Optional<SwampLauncherPlacement.Bounds> placement = SwampLauncherPlacement.choose(
                320, 240, EXIT, List.of(EXIT, other), 204);
        assertTrue(placement.isPresent());
        assertFalse(placement.orElseThrow().intersects(EXIT));
        assertTrue(placement.orElseThrow().within(320, 240));
    }

    @Test
    void returnsEmptyWhenNoCandidateFits() {
        SwampLauncherPlacement.Bounds full =
                new SwampLauncherPlacement.Bounds(4, 4, 312, 292);
        Optional<SwampLauncherPlacement.Bounds> placement = SwampLauncherPlacement.choose(
                320, 300, EXIT, List.of(full), 204);
        assertTrue(placement.isEmpty());
    }

    @Test
    void narrowScreensClampTheButtonInsideTheScreen() {
        Optional<SwampLauncherPlacement.Bounds> placement = SwampLauncherPlacement.choose(
                180, 200, new SwampLauncherPlacement.Bounds(10, 120, 160, 20), List.of(), 204);
        assertTrue(placement.isPresent());
        assertTrue(placement.orElseThrow().within(180, 200));
        assertEquals(172, placement.orElseThrow().width());
    }
}
