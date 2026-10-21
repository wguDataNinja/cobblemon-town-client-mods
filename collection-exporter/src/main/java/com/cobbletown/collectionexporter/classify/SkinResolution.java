package com.cobbletown.collectionexporter.classify;

import java.util.List;

/** Result of resolving one Pokémon's raw aspects against the confirmed skin allowlist. */
public record SkinResolution(
        SkinResolutionStatus status,
        SkinFamily family,
        List<String> matchedAspects,
        List<String> unknownAspects
) {
    public SkinResolution {
        matchedAspects = matchedAspects == null ? List.of() : List.copyOf(matchedAspects);
        unknownAspects = unknownAspects == null ? List.of() : List.copyOf(unknownAspects);
    }

    public static SkinResolution none(List<String> unknownAspects) {
        if (unknownAspects == null || unknownAspects.isEmpty()) {
            return new SkinResolution(SkinResolutionStatus.NONE, null, List.of(), List.of());
        }
        return new SkinResolution(SkinResolutionStatus.UNRESOLVED, null, List.of(), unknownAspects);
    }

    public boolean resolved() {
        return status == SkinResolutionStatus.RESOLVED && family != null;
    }
}
