package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.PokemonCategory;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Produces deterministic, player-facing plain text suitable for Minecraft and Discord. */
public final class ReportTextFormatter {
    private static final int DIVIDER_WIDTH = 44;
    private static final String HEAVY_DIVIDER = "━".repeat(DIVIDER_WIDTH);
    private static final String LIGHT_DIVIDER = "─".repeat(DIVIDER_WIDTH);
    private static final DateTimeFormatter CAPTURE_TIME =
            DateTimeFormatter.ofPattern("MMMM d, yyyy · h:mm a z", Locale.US);

    private final ZoneId zoneId;

    public ReportTextFormatter() {
        this(ZoneId.systemDefault());
    }

    public ReportTextFormatter(ZoneId zoneId) {
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
    }

    public String format(ReportResult result, ReportRequest request) {
        StringBuilder out = new StringBuilder();
        appendHeader(out, result);
        if (result.isEmpty()) {
            out.append("\nNo Pokémon matched the selected categories.\n");
        } else {
            appendSkins(out, result.skinGroups());
            appendLegendaryAndMythical(out, result);
            appendEntries(out, "SHINY", result.shiny());
            appendEntries(out, "OTHER", result.other());
            appendIssues(out, "UNRESOLVED", result.unresolved());
            appendIssues(out, "CONFLICTS", result.conflicts());
        }
        if (request.includeCounts()) {
            appendSummary(out, result.summary());
        }
        if (request.includeDiagnostics()) {
            appendDiagnostics(out, result.diagnostics());
        }
        return out.toString().stripTrailing() + "\n";
    }

    private void appendHeader(StringBuilder out, ReportResult result) {
        out.append(HEAVY_DIVIDER).append('\n');
        out.append(center("COBBLEMON TOWN")).append('\n');
        out.append(center("Pokémon Collection")).append('\n');
        out.append(HEAVY_DIVIDER).append('\n');
        out.append(localCaptureTime(result.generatedAt())).append('\n');
        out.append("PC Snapshot: ").append(snapshotState(result.completeness())).append('\n');
        if (!result.completeness().isComplete()) {
            out.append("WARNING: This is not a complete inventory.\n");
        }
    }

    private void appendSkins(StringBuilder out, List<SkinGroup> groups) {
        if (groups.isEmpty()) {
            return;
        }
        appendSectionHeading(out, "CUSTOM SKINS");
        for (SkinGroup group : groups) {
            out.append("  ").append(group.family().displayName()).append('\n');
            out.append("    Shiny: ").append(names(group.shiny(), true)).append('\n');
            out.append("    Non-Shiny: ").append(names(group.nonShiny(), true)).append('\n');
        }
    }

    private void appendLegendaryAndMythical(StringBuilder out, ReportResult result) {
        List<ReportEntry> mythical = result.mythicalEntries();
        List<ReportEntry> legendary = result.legendaryEntries();
        if (mythical.isEmpty() && legendary.isEmpty()) {
            return;
        }
        appendSectionHeading(out, "LEGENDARY & MYTHICAL");
        appendSubgroup(out, "Mythical", mythical);
        appendSubgroup(out, "Legendary", legendary);
    }

    private void appendSubgroup(StringBuilder out, String heading, List<ReportEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        out.append(heading).append('\n');
        for (ReportEntry entry : entries) {
            out.append("  ").append(legendaryLabel(entry)).append('\n');
        }
    }

    private String legendaryLabel(ReportEntry entry) {
        return entry.shiny() ? entry.quantityLabel() + " (Shiny)" : entry.quantityLabel();
    }

    private void appendEntries(StringBuilder out, String heading, List<ReportEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        appendSectionHeading(out, heading);
        for (ReportEntry entry : entries) {
            out.append("  ").append(entry.quantityLabel()).append('\n');
        }
    }

    private void appendIssues(StringBuilder out, String heading, List<ReportIssue> issues) {
        if (issues.isEmpty()) {
            return;
        }
        appendSectionHeading(out, heading);
        for (ReportIssue issue : issues) {
            out.append("  ").append(issue.quantityLabel()).append('\n');
            out.append("    Reason: ").append(issue.reason()).append('\n');
            out.append("    Aspect: ").append(String.join(", ", issue.aspects())).append('\n');
            if (issue.customSpecies()) {
                out.append("    Species: custom/unknown\n");
            }
        }
    }

    private void appendSectionHeading(StringBuilder out, String heading) {
        out.append('\n').append(heading).append('\n').append(LIGHT_DIVIDER).append('\n');
    }

    private String names(List<ReportEntry> entries, boolean withinSkins) {
        if (entries.isEmpty()) {
            return "None";
        }
        List<String> labels = new ArrayList<>();
        for (ReportEntry entry : entries) {
            labels.add(label(entry, withinSkins));
        }
        return String.join(", ", labels);
    }

    private String label(ReportEntry entry, boolean withinSkins) {
        StringBuilder label = new StringBuilder(entry.quantityLabel());
        if (withinSkins) {
            if (entry.categories().contains(PokemonCategory.LEGENDARY)) {
                label.append(" (Legendary)");
            } else if (entry.categories().contains(PokemonCategory.MYTHICAL)) {
                label.append(" (Mythical)");
            }
        }
        return label.toString();
    }

    private void appendSummary(StringBuilder out, ReportSummary summary) {
        appendSectionHeading(out, "SUMMARY");
        out.append("Total Pokémon: ").append(summary.includedInstances()).append('\n');
        out.append("Custom Skins: ").append(summary.skinInstances()).append('\n');
        if (summary.skinInstances() > 0) {
            out.append("  Shiny: ").append(summary.shinySkinInstances()).append('\n');
            out.append("  Non-Shiny: ").append(summary.nonShinySkinInstances()).append('\n');
            out.append("  Unique Species/Forms: ").append(summary.uniqueSkinSpeciesForms()).append('\n');
        }
        appendNonZero(out, "Legendary", summary.legendaryInstances());
        appendNonZero(out, "Mythical", summary.mythicalInstances());
        appendNonZero(out, "Shiny Pokémon", summary.shinyInstances());
        appendNonZero(out, "Paradox", summary.paradoxInstances());
        appendNonZero(out, "Ultra Beasts", summary.ultraBeastInstances());
        appendNonZero(out, "Custom/Unknown Species", summary.customSpeciesInstances());
        appendNonZero(out, "Conflicts", summary.conflictInstances());
        appendNonZero(out, "Unresolved", summary.unresolvedInstances());
        appendNonZero(out, "Other Pokémon", summary.otherInstances());
    }

    private void appendNonZero(StringBuilder out, String label, int value) {
        if (value > 0) {
            out.append(label).append(": ").append(value).append('\n');
        }
    }

    private void appendDiagnostics(StringBuilder out, List<String> diagnostics) {
        if (diagnostics.isEmpty()) {
            return;
        }
        appendSectionHeading(out, "DIAGNOSTICS");
        for (String diagnostic : diagnostics) {
            out.append("  ").append(diagnostic).append('\n');
        }
    }

    private String localCaptureTime(String generatedAt) {
        if (generatedAt == null || generatedAt.isBlank() || "unknown".equalsIgnoreCase(generatedAt)) {
            return "Unknown capture time";
        }
        try {
            return CAPTURE_TIME.withZone(zoneId).format(Instant.parse(generatedAt));
        } catch (DateTimeParseException invalid) {
            return "Unknown capture time";
        }
    }

    private static String snapshotState(InventoryCompleteness completeness) {
        return switch (completeness) {
            case COMPLETE_SYNCHRONIZED_STORE -> "Complete";
            case PARTIAL_SINGLE_STORE_FALLBACK, PARTIAL_INCOMPLETE_STORE -> "Partial";
            case AMBIGUOUS_MULTIPLE_STORES, UNAVAILABLE -> "Unavailable";
        };
    }

    private static String center(String text) {
        int padding = Math.max(0, (DIVIDER_WIDTH - text.length()) / 2);
        return " ".repeat(padding) + text;
    }
}
