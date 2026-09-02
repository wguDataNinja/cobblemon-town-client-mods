package com.cobbletown.trashwarning;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrashFingerprintTest {
    private static final String TRASH_JSON = TrashFingerprint.STRUCTURED_TITLE;

    @Test
    void matchesOnlyTheObservedTrashContract() {
        assertTrue(TrashFingerprint.matches(observed()));
    }

    @Test
    void rejectsNormalChestAndWrongTitleRepresentations() {
        assertFalse(TrashFingerprint.matches(with("Chest", "{\"translate\":\"container.chest\",\"fallback\":\"container.chest\"}")));
        assertFalse(TrashFingerprint.matches(with("Trash", "{\"text\":\"Trash\",\"color\":\"red\"}")));
        assertFalse(TrashFingerprint.matches(with("Trash", "{\"text\":\"Trash\"}")));
    }

    @Test
    void rejectsWrongStructure() {
        assertFalse(TrashFingerprint.matches(new TrashFingerprint.Snapshot(true, true, 6, 90, 54, 36, true, "Trash", TRASH_JSON)));
        assertFalse(TrashFingerprint.matches(new TrashFingerprint.Snapshot(true, true, 3, 62, 27, 35, true, "Trash", TRASH_JSON)));
        assertFalse(TrashFingerprint.matches(new TrashFingerprint.Snapshot(true, true, 3, 63, 27, 36, false, "Trash", TRASH_JSON)));
        assertFalse(TrashFingerprint.matches(new TrashFingerprint.Snapshot(false, true, 3, 63, 27, 36, true, "Trash", TRASH_JSON)));
        assertFalse(TrashFingerprint.matches(new TrashFingerprint.Snapshot(true, false, 3, 63, 27, 36, true, "Trash", TRASH_JSON)));
    }

    private static TrashFingerprint.Snapshot observed() {
        return new TrashFingerprint.Snapshot(true, true, 3, 63, 27, 36, true, "Trash", TRASH_JSON);
    }

    private static TrashFingerprint.Snapshot with(String flat, String structured) {
        return new TrashFingerprint.Snapshot(true, true, 3, 63, 27, 36, true, flat, structured);
    }
}
