package com.cobbletown.collectionexporter.launcher;

import java.util.Objects;

/** One installed, reachable Swamp Mod entry. It never describes an unavailable feature. */
public record SwampModDescriptor(
        String modId,
        String displayName,
        String description,
        int order
) {
    public SwampModDescriptor {
        Objects.requireNonNull(modId, "modId");
        Objects.requireNonNull(displayName, "displayName");
        description = description == null ? "" : description;
    }
}
