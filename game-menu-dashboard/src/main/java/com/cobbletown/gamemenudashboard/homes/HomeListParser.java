package com.cobbletown.gamemenudashboard.homes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Parses only the observed, non-overlay {@code Homes [count/limit]: names} GAME message. */
public final class HomeListParser {
    // The captured heading and names are separate Text siblings. The rendered join
    // does not guarantee a literal colon, so only that separator is optional.
    private static final Pattern LIST = Pattern.compile("^Homes\\s*\\[(\\d+)\\s*/\\s*(\\d+)\\]\\s*:?[ \\t]*(.*)$");
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9_-]{1,32}");

    private HomeListParser() {}

    public static Optional<HomeSnapshot> parse(String text, Instant receivedAt) {
        if (text == null || receivedAt == null) return Optional.empty();
        Matcher match = LIST.matcher(text.strip());
        if (!match.matches()) return Optional.empty();
        try {
            int count = Integer.parseInt(match.group(1));
            int limit = Integer.parseInt(match.group(2));
            if (count < 0 || limit < 0 || count > limit) return Optional.empty();
            String tail = match.group(3).strip();
            List<String> names = new ArrayList<>();
            if (!tail.isEmpty()) {
                for (String part : tail.split(",")) {
                    String name = part.strip();
                    if (!isSafeName(name) || names.contains(name)) return Optional.empty();
                    names.add(name);
                }
            }
            // The observed server list's named entries and count agree. Reject malformed lookalikes.
            if (names.size() != count) return Optional.empty();
            return Optional.of(new HomeSnapshot(names, count, limit, receivedAt));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    public static boolean isSafeName(String name) {
        return name != null && SAFE_NAME.matcher(name).matches();
    }
}
