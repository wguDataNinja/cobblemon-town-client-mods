package com.cobbletown.gamemenudashboard.homes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Local-only notes. Server names and Home state are never written here. */
public final class HomeNotesStore {
    private static final int MAX_NOTE_LENGTH = 80;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, String> notes = new LinkedHashMap<>();
    private HomeSnapshot cachedHomes;

    private Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("game-menu-dashboard/destinations.json");
    }

    public void load() {
        try {
            if (!Files.exists(path())) return;
            Stored stored = GSON.fromJson(Files.readString(path()), Stored.class);
            if (stored != null && stored.schemaVersion >= 1 && stored.homeNotes != null) {
                stored.homeNotes.forEach((name, note) -> {
                    if (HomeListParser.isSafeName(name) && note != null) notes.put(name, clip(note));
                });
            }
            if (stored != null && stored.homeNames != null && stored.homeReceivedAt != null
                && stored.homeCount >= 0 && stored.homeLimit >= stored.homeCount
                && stored.homeNames.size() == stored.homeCount
                && stored.homeNames.stream().allMatch(HomeListParser::isSafeName)) {
                cachedHomes = new HomeSnapshot(stored.homeNames, stored.homeCount, stored.homeLimit, Instant.parse(stored.homeReceivedAt));
            }
        } catch (Exception ignored) {
            // A bad local file must not prevent the pause menu from opening.
        }
    }

    public String noteFor(String homeName) {
        return notes.getOrDefault(homeName, "");
    }

    public void setNote(String homeName, String note) {
        if (!HomeListParser.isSafeName(homeName)) return;
        String clipped = clip(note == null ? "" : note);
        if (clipped.isBlank()) notes.remove(homeName); else notes.put(homeName, clipped);
        save();
    }

    public HomeSnapshot cachedHomes() { return cachedHomes; }

    public void setCachedHomes(HomeSnapshot snapshot) {
        cachedHomes = snapshot;
        save();
    }

    private void save() {
        try {
            Files.createDirectories(path().getParent());
            Files.writeString(path(), GSON.toJson(new Stored(new LinkedHashMap<>(notes), cachedHomes)));
        } catch (Exception ignored) {
            // Notes are a convenience; a disk error must not affect server actions.
        }
    }

    private static String clip(String value) {
        return value.length() <= MAX_NOTE_LENGTH ? value : value.substring(0, MAX_NOTE_LENGTH);
    }

    private static final class Stored {
        int schemaVersion = 2;
        Map<String, String> homeNotes = new LinkedHashMap<>();
        List<String> homeNames;
        int homeCount;
        int homeLimit;
        String homeReceivedAt;
        Stored(Map<String, String> homeNotes, HomeSnapshot homes) {
            this.homeNotes = homeNotes;
            if (homes != null) {
                homeNames = homes.names(); homeCount = homes.count(); homeLimit = homes.limit(); homeReceivedAt = homes.receivedAt().toString();
            }
        }
        @SuppressWarnings("unused") Stored() {}
    }
}
