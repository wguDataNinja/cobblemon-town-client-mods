package com.cobbletown.collectionexporter.report;

/** Counts for selected Pokémon. Duplicate instances are counted separately. */
public record ReportSummary(
        int includedInstances,
        int skinInstances,
        int shinySkinInstances,
        int nonShinySkinInstances,
        int uniqueSkinSpeciesForms,
        int legendaryInstances,
        int mythicalInstances,
        int paradoxInstances,
        int ultraBeastInstances,
        int shinyInstances,
        int otherInstances,
        int conflictInstances,
        int unresolvedInstances,
        int customSpeciesInstances
) {
}
