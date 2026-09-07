package com.cobbletown.gamemenudashboard.homes;

import java.time.Instant;
import java.util.List;

/** Immutable client-side view of one explicitly requested server Homes list. */
public record HomeSnapshot(List<String> names, int count, int limit, Instant receivedAt) {
    public HomeSnapshot {
        names = List.copyOf(names);
    }
}
