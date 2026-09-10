package com.cobbletown.gamemenudashboard.claims;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Versioned local cache for normalized own-claim fields and Details geometry only. */
public final class ClaimCacheStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Path path() { return FabricLoader.getInstance().getConfigDir().resolve("game-menu-dashboard/claims-cache.json"); }

    public Cached load() {
        try {
            if (!Files.exists(path())) return null;
            Stored stored = GSON.fromJson(Files.readString(path()), Stored.class);
            if (stored == null || (stored.schemaVersion != 1 && stored.schemaVersion != 2 && stored.schemaVersion != 3) || stored.claims == null || stored.receivedAt == null) return null;
            Instant receivedAt = Instant.parse(stored.receivedAt);
            List<ClaimEntry> entries = new ArrayList<>();
            for (StoredClaim claim : stored.claims) {
                if (claim == null || blank(claim.key) || blank(claim.label)) return null;
                ClaimGeometry geometry = claim.geometry == null ? null : new ClaimGeometry(claim.geometry.world, claim.geometry.lesserX,
                    claim.geometry.lesserY, claim.geometry.lesserZ, claim.geometry.greaterX, claim.geometry.greaterY, claim.geometry.greaterZ);
                // v1/v2 allowed a collection of persisted MAP choices.  That made
                // distant rectangles pile up at Xaero's edge after an upgrade.
                // Migrate those legacy choices to a quiet state; v3 restores a map
                // only through one fresh, explicit Claim selection.
                boolean mapSelected = stored.schemaVersion >= 3 && claim.displayEnabled;
                entries.add(new ClaimEntry(claim.key, claim.label, empty(claim.owner), empty(claim.type), empty(claim.area), empty(claim.blocks), geometry, true, mapSelected));
            }
            return new Cached(List.copyOf(entries), receivedAt);
        } catch (Exception ignored) {
            return null; // cache is convenience only
        }
    }

    public void save(List<ClaimEntry> claims, Instant receivedAt) {
        try {
            Files.createDirectories(path().getParent());
            List<StoredClaim> storedClaims = new ArrayList<>();
            for (ClaimEntry entry : claims) storedClaims.add(new StoredClaim(entry));
            Files.writeString(path(), GSON.toJson(new Stored(storedClaims, receivedAt.toString())));
        } catch (Exception ignored) {
            // Never let a local disk issue affect server navigation or the pause menu.
        }
    }

    /** Geometry fingerprint is the strongest currently proven local map-choice identity. */
    public boolean displayEnabledFor(ClaimGeometry geometry) {
        Cached cached = load();
        if (cached == null || geometry == null) return false;
        return cached.claims().stream().anyMatch(entry -> sameGeometry(entry.geometry(), geometry) && entry.displayEnabled());
    }
    private static boolean sameGeometry(ClaimGeometry a, ClaimGeometry b) {
        return a != null && b != null && ClaimGeometry.sameWorld(a.world(), b.world())
            && a.lesserX() == b.lesserX() && a.lesserZ() == b.lesserZ()
            && a.greaterX() == b.greaterX() && a.greaterZ() == b.greaterZ();
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static String empty(String value) { return value == null ? "" : value; }
    public record Cached(List<ClaimEntry> claims, Instant receivedAt) { }

    private static final class Stored {
        int schemaVersion = 3;
        List<StoredClaim> claims;
        String receivedAt;
        Stored(List<StoredClaim> claims, String receivedAt) { this.claims = claims; this.receivedAt = receivedAt; }
        @SuppressWarnings("unused") Stored() { }
    }
    private static final class StoredClaim {
        String key, label, owner, type, area, blocks;
        boolean displayEnabled;
        StoredGeometry geometry;
        StoredClaim(ClaimEntry entry) {
            key = entry.key(); label = entry.label(); owner = entry.owner(); type = entry.type(); area = entry.area(); blocks = entry.blocks(); displayEnabled = entry.displayEnabled();
            geometry = entry.geometry() == null ? null : new StoredGeometry(entry.geometry());
        }
        @SuppressWarnings("unused") StoredClaim() { }
    }
    private static final class StoredGeometry {
        String world;
        int lesserX, lesserY, lesserZ, greaterX, greaterY, greaterZ;
        StoredGeometry(ClaimGeometry geometry) {
            world = geometry.world(); lesserX = geometry.lesserX(); lesserY = geometry.lesserY(); lesserZ = geometry.lesserZ();
            greaterX = geometry.greaterX(); greaterY = geometry.greaterY(); greaterZ = geometry.greaterZ();
        }
        @SuppressWarnings("unused") StoredGeometry() { }
    }
}
