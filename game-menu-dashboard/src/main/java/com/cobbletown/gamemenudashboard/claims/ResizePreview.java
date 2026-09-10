package com.cobbletown.gamemenudashboard.claims;

/** Local, explicitly previewed Resize proposal. Never replaces server Details geometry. */
public final class ResizePreview {
    private static String claimKey;
    private static ClaimGeometry proposed;
    private static String direction = "North";
    private static String amount = "16";

    private ResizePreview() { }

    public static void show(String key, ClaimGeometry geometry) { claimKey = key; proposed = geometry; }
    public static void clear() { claimKey = null; proposed = null; }
    public static String direction() { return direction; }
    public static String amount() { return amount; }
    public static void plan(String nextDirection, String nextAmount) { direction = nextDirection; amount = nextAmount; }
    public static ClaimGeometry geometryFor(String key) { return key != null && key.equals(claimKey) ? proposed : null; }
    public static boolean activeFor(String key) { return geometryFor(key) != null; }

    public static ClaimGeometry derive(ClaimGeometry current, String direction, int amount) {
        int step = Math.max(1, amount);
        int minX = Math.min(current.lesserX(), current.greaterX()), maxX = Math.max(current.lesserX(), current.greaterX());
        int minZ = Math.min(current.lesserZ(), current.greaterZ()), maxZ = Math.max(current.lesserZ(), current.greaterZ());
        switch (direction.toLowerCase(java.util.Locale.ROOT)) {
            case "north" -> minZ -= step;
            case "south" -> maxZ += step;
            case "west" -> minX -= step;
            case "east" -> maxX += step;
            case "all" -> { minX -= step; maxX += step; minZ -= step; maxZ += step; }
            default -> throw new IllegalArgumentException("Unknown resize direction");
        }
        return new ClaimGeometry(current.world(), minX, current.lesserY(), minZ, maxX, current.greaterY(), maxZ);
    }
}
