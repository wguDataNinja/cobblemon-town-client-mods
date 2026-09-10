package com.cobbletown.gamemenudashboard.claims;

/** Normalized 2D BASIC-claim geometry disclosed by a deliberate server Details action. */
public record ClaimGeometry(String world, int lesserX, int lesserY, int lesserZ,
                            int greaterX, int greaterY, int greaterZ) {
    public boolean containsBlock(String currentWorld, int blockX, int blockZ) {
        return sameWorld(world, currentWorld)
            && blockX >= Math.min(lesserX, greaterX) && blockX <= Math.max(lesserX, greaterX)
            && blockZ >= Math.min(lesserZ, greaterZ) && blockZ <= Math.max(lesserZ, greaterZ);
    }

    public static boolean sameWorld(String a, String b) {
        if (a == null || b == null) return false;
        return normalizeWorld(a).equals(normalizeWorld(b));
    }

    public static String normalizeWorld(String world) {
        String normalized = world.strip().toLowerCase(java.util.Locale.ROOT);
        if (normalized.startsWith("minecraft:")) normalized = normalized.substring("minecraft:".length());
        return normalized;
    }
}
