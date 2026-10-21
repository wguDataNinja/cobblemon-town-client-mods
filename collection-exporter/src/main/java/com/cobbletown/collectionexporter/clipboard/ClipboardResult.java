package com.cobbletown.collectionexporter.clipboard;

/** Result of a clipboard write, suitable for a future screen/confirmation message. */
public record ClipboardResult(boolean success, int characters, String error) {
    public static ClipboardResult success(int characters) {
        return new ClipboardResult(true, characters, null);
    }

    public static ClipboardResult failure(String error) {
        return new ClipboardResult(false, 0, error);
    }
}
