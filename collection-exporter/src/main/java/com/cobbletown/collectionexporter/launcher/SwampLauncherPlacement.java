package com.cobbletown.collectionexporter.launcher;

import java.util.List;
import java.util.Optional;

/** Occupied-bounds-aware placement for the single Escape-menu entry. */
public final class SwampLauncherPlacement {
    public static final int BUTTON_HEIGHT = 20;

    public record Bounds(int x, int y, int width, int height) {
        public Bounds {
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("bounds must be positive");
            }
        }

        public boolean intersects(Bounds other) {
            return x < other.x + other.width && x + width > other.x
                    && y < other.y + other.height && y + height > other.y;
        }

        public boolean within(int screenWidth, int screenHeight) {
            return x >= 4 && y >= 4
                    && x + width <= screenWidth - 4
                    && y + height <= screenHeight - 4;
        }
    }

    private SwampLauncherPlacement() {
    }

    /**
     * Prefers the gap directly below the vanilla exit button, then directly above it, then
     * the bottom or top center. Returns empty rather than overlapping another control.
     */
    public static Optional<Bounds> choose(int screenWidth,
                                          int screenHeight,
                                          Bounds exitButton,
                                          List<Bounds> occupied,
                                          int desiredWidth) {
        int width = Math.max(1, Math.min(desiredWidth, screenWidth - 8));
        int centeredX = clamp((screenWidth - width) / 2, 4, screenWidth - width - 4);
        int exitX = clamp(exitButton.x() + (exitButton.width() - width) / 2,
                4, screenWidth - width - 4);

        List<Bounds> candidates = List.of(
                new Bounds(exitX, exitButton.y() + exitButton.height() + 4, width, BUTTON_HEIGHT),
                new Bounds(exitX, exitButton.y() - BUTTON_HEIGHT - 4, width, BUTTON_HEIGHT),
                new Bounds(centeredX, screenHeight - BUTTON_HEIGHT - 6, width, BUTTON_HEIGHT),
                new Bounds(centeredX, 6, width, BUTTON_HEIGHT));

        for (Bounds candidate : candidates) {
            if (!candidate.within(screenWidth, screenHeight)) {
                continue;
            }
            boolean free = occupied.stream().noneMatch(candidate::intersects);
            if (free) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
