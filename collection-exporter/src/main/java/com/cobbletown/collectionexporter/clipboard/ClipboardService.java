package com.cobbletown.collectionexporter.clipboard;

/** Small adapter so report generation stays independent of the screen and Minecraft client. */
@FunctionalInterface
public interface ClipboardService {
    ClipboardResult copy(String text);
}
