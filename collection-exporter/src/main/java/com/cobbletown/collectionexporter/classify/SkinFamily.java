package com.cobbletown.collectionexporter.classify;

import java.util.List;
import java.util.Objects;

/**
 * One owner-reviewable cosmetic skin family.
 *
 * <p>{@code officialName} is the full collection name; {@code displayName} is the name used in
 * player-facing reports. {@code aliases} preserves informal names without creating a second
 * collection. {@code evidenceStatus} and {@code identityStatus} are deliberately separate.</p>
 */
public record SkinFamily(
        String aspect,
        String officialName,
        String displayName,
        List<String> aliases,
        String identityStatus,
        String evidenceStatus,
        int reportOrder
) {
    public SkinFamily {
        Objects.requireNonNull(aspect, "aspect");
        Objects.requireNonNull(displayName, "displayName");
        officialName = officialName == null || officialName.isBlank() ? displayName : officialName;
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
        identityStatus = identityStatus == null ? "candidate" : identityStatus;
        evidenceStatus = evidenceStatus == null ? "unresolved" : evidenceStatus;
    }

    public boolean identityConfirmed() {
        return "confirmed".equalsIgnoreCase(identityStatus);
    }
}
