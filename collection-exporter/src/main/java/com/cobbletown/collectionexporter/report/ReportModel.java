package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.ClassifiedPokemon;

import java.util.List;

/**
 * The classified instances used to produce a {@link ReportResult}. Presentation experiments
 * reuse this model so classification and inclusion semantics are not duplicated.
 */
public record ReportModel(List<ClassifiedPokemon> included, ReportResult result) {
    public ReportModel {
        included = included == null ? List.of() : List.copyOf(included);
    }
}
