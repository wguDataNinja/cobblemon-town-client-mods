package com.cobbletown.gamemenudashboard.claims;

/** A privacy-local row from the player's OWN server list; callbacks never belong here. */
public record ClaimEntry(String key, String label, String owner, String type, String area,
                         String blocks, ClaimGeometry geometry, boolean geometryStale, boolean displayEnabled) {
    public ClaimEntry withGeometry(ClaimGeometry value, boolean stale) {
        return new ClaimEntry(key, label, owner, type, area, blocks, value, stale, displayEnabled);
    }
    public ClaimEntry withDisplayEnabled(boolean value) { return new ClaimEntry(key, label, owner, type, area, blocks, geometry, geometryStale, value); }
}
