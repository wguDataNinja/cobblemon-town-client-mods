package com.cobbletown.trashwarning;

/** Pure, fail-closed representation of the live-observed Trash screen contract. */
public final class TrashFingerprint {
    public static final String FLATTENED_TITLE = "Trash";
    public static final String STRUCTURED_TITLE = "{\"text\":\"Trash\",\"color\":\"dark_gray\"}";
    public static final int ROWS = 3;
    public static final int SLOT_COUNT = 63;
    public static final int UPPER_SLOTS = 27;
    public static final int PLAYER_SLOTS = 36;

    private TrashFingerprint() { }

    public static boolean matches(Snapshot value) {
        // Structured title equality is intentional: flat "Trash" alone is not sufficient.
        return value.genericScreen()
            && value.genericHandler()
            && value.rows() == ROWS
            && value.slotCount() == SLOT_COUNT
            && value.upperSlots() == UPPER_SLOTS
            && value.playerSlots() == PLAYER_SLOTS
            && value.exactSlotTopology()
            && FLATTENED_TITLE.equals(value.flattenedTitle())
            && STRUCTURED_TITLE.equals(value.structuredTitle());
    }

    public record Snapshot(
        boolean genericScreen,
        boolean genericHandler,
        int rows,
        int slotCount,
        int upperSlots,
        int playerSlots,
        boolean exactSlotTopology,
        String flattenedTitle,
        String structuredTitle
    ) { }
}
