package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.PokemonCategory;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;

import java.util.List;

/**
 * Structured report data. Text is produced by {@link ReportTextFormatter}; the screen decides
 * how much of this to show, including the full-inventory filter counts and named issues.
 */
public record ReportResult(
        String generatedAt,
        InventoryCompleteness completeness,
        List<SkinGroup> skinGroups,
        List<ReportEntry> legendMyth,
        List<ReportEntry> shiny,
        List<ReportEntry> other,
        ReportSummary summary,
        List<String> diagnostics,
        ReportCounts counts,
        List<ReportIssue> unresolved,
        List<ReportIssue> conflicts
) {
    public ReportResult {
        skinGroups = skinGroups == null ? List.of() : List.copyOf(skinGroups);
        legendMyth = legendMyth == null ? List.of() : List.copyOf(legendMyth);
        shiny = shiny == null ? List.of() : List.copyOf(shiny);
        other = other == null ? List.of() : List.copyOf(other);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        counts = counts == null ? new ReportCounts(0, 0, 0, 0) : counts;
        unresolved = unresolved == null ? List.of() : List.copyOf(unresolved);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
    }

    public boolean isEmpty() {
        return skinGroups.isEmpty() && legendMyth.isEmpty() && shiny.isEmpty() && other.isEmpty();
    }

    public List<ReportEntry> legendaryEntries() {
        return legendMyth.stream()
                .filter(entry -> entry.categories().contains(PokemonCategory.LEGENDARY))
                .toList();
    }

    public List<ReportEntry> mythicalEntries() {
        return legendMyth.stream()
                .filter(entry -> entry.categories().contains(PokemonCategory.MYTHICAL))
                .toList();
    }
}
