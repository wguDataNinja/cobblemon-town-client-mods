package com.cobbletown.collectionexporter.report;

import java.util.List;

/** One grouped classification problem with the player-facing identity and raw evidence. */
public record ReportIssue(
        String displayName,
        String reason,
        List<String> aspects,
        boolean customSpecies,
        int count
) {
    public ReportIssue {
        displayName = displayName == null || displayName.isBlank() ? "Unknown Pokémon" : displayName;
        reason = reason == null || reason.isBlank() ? "Unrecognized aspect" : reason;
        aspects = aspects == null ? List.of() : List.copyOf(aspects);
        count = Math.max(1, count);
    }

    public String quantityLabel() {
        return count > 1 ? displayName + " ×" + count : displayName;
    }
}
