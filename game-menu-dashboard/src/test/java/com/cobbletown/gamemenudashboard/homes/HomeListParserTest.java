package com.cobbletown.gamemenudashboard.homes;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

final class HomeListParserTest {
    private static final Instant NOW = Instant.parse("2026-09-07T00:00:00Z");

    @Test void parsesOnlyTheObservedListShape() {
        var parsed = HomeListParser.parse("Homes [4/5]: base, market, mine, temp", NOW);
        assertTrue(parsed.isPresent());
        assertEquals(4, parsed.get().count());
        assertEquals(5, parsed.get().limit());
        assertEquals(java.util.List.of("base", "market", "mine", "temp"), parsed.get().names());
    }

    @Test void acceptsTheObservedSiblingJoinWithoutAnInventedColon() {
        var parsed = HomeListParser.parse("Homes [1/5] base", NOW);
        assertTrue(parsed.isPresent());
        assertEquals(java.util.List.of("base"), parsed.get().names());
    }

    @Test void allowsAnEmptyServerList() {
        var parsed = HomeListParser.parse("Homes [0/5]:", NOW);
        assertTrue(parsed.isPresent());
        assertTrue(parsed.get().names().isEmpty());
    }

    @Test void rejectsAutocompleteLikeOrUnsafeText() {
        assertTrue(HomeListParser.parse("home base", NOW).isEmpty());
        assertTrue(HomeListParser.parse("Homes [1/5]: base;op someone", NOW).isEmpty());
        assertTrue(HomeListParser.parse("Homes [2/5]: base", NOW).isEmpty());
        assertTrue(HomeListParser.parse("Homes (1/5): base", NOW).isEmpty());
        assertFalse(HomeListParser.isSafeName("base other"));
    }
}
