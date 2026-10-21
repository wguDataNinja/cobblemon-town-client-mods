package com.cobbletown.collectionexporter.classify;

import java.util.List;

/** One species entry in an owner-reviewable non-skin category. */
public record CategorySpecies(
        String speciesId,
        String name,
        int dexNumber,
        List<String> labels
) {
    public CategorySpecies {
        labels = labels == null ? List.of() : List.copyOf(labels);
    }
}
