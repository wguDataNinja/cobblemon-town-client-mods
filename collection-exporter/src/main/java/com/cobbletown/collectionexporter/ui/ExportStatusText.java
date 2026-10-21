package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;
import com.cobbletown.collectionexporter.report.ReportIssue;
import com.cobbletown.collectionexporter.report.ReportResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/** Pure user-facing text for the export screen. Kept separate so states are unit-tested. */
public final class ExportStatusText {
    public enum Tone {
        MUTED,
        SUCCESS,
        WARNING,
        ERROR
    }

    public record Message(String text, Tone tone) {
    }

    public record SnapshotStatus(String headline, Tone tone, String detail) {
        public SnapshotStatus {
            headline = headline == null ? "PC Not Loaded" : headline;
            tone = tone == null ? Tone.ERROR : tone;
            detail = detail == null || detail.isBlank() ? null : detail;
        }
    }

    private ExportStatusText() {
    }

    /** PC load state for the top of the screen. Technical timestamps stay off this surface. */
    public static SnapshotStatus snapshotStatus(ReportResult report, String readFailure) {
        if (readFailure != null) {
            return new SnapshotStatus("PC Not Loaded", Tone.ERROR, "The PC could not be read.");
        }
        if (report == null) {
            return new SnapshotStatus("PC Not Loaded", Tone.ERROR, "No PC data has been read yet.");
        }
        return switch (report.completeness()) {
            case COMPLETE_SYNCHRONIZED_STORE ->
                    new SnapshotStatus("PC Loaded Successfully", Tone.SUCCESS, null);
            case PARTIAL_SINGLE_STORE_FALLBACK ->
                    new SnapshotStatus("PC Loaded Partially", Tone.WARNING,
                            "PC ownership could not be verified.");
            case PARTIAL_INCOMPLETE_STORE ->
                    new SnapshotStatus("PC Loaded Partially", Tone.WARNING,
                            "Some boxes or slots could not be read.");
            case AMBIGUOUS_MULTIPLE_STORES ->
                    new SnapshotStatus("PC Not Loaded", Tone.ERROR,
                            "Multiple PC stores were found.");
            case UNAVAILABLE ->
                    new SnapshotStatus("PC Not Available", Tone.ERROR,
                            "Open your PC once, then choose Refresh.");
        };
    }

    /** Selected total; every Pokémon is counted once even when it matches several categories. */
    public static String selectedLine(ReportResult report) {
        int selected = report == null ? 0 : report.summary().includedInstances();
        return selected + " Pokémon selected";
    }

    /**
     * Named unresolved line. The count is always kept; only the name list is shortened when
     * {@code maxWidth} cannot hold it.
     */
    public static String unresolvedLine(ReportResult report, int maxWidth, ToIntFunction<String> widthOf) {
        if (report == null || report.unresolved().isEmpty()) {
            return "";
        }
        int count = report.summary().unresolvedInstances();
        List<String> names = new ArrayList<>();
        for (ReportIssue issue : report.unresolved()) {
            names.add(issue.quantityLabel());
        }
        String prefix = "Unresolved: " + count + " (";
        String suffix = ")";
        String full = prefix + String.join(", ", names) + suffix;
        if (widthOf.applyAsInt(full) <= maxWidth) {
            return full;
        }

        String countOnly = "Unresolved: " + count;
        if (widthOf.applyAsInt(countOnly) >= maxWidth) {
            return countOnly;
        }
        String ellipsis = "...";
        int available = maxWidth - widthOf.applyAsInt(prefix)
                - widthOf.applyAsInt(ellipsis) - widthOf.applyAsInt(suffix);
        if (available <= 0) {
            return countOnly;
        }

        StringBuilder shortened = new StringBuilder();
        String joined = String.join(", ", names);
        for (int index = 0; index < joined.length(); index++) {
            String candidate = shortened + String.valueOf(joined.charAt(index));
            if (widthOf.applyAsInt(candidate) > available) {
                break;
            }
            shortened.append(joined.charAt(index));
        }
        if (shortened.isEmpty()) {
            return countOnly;
        }
        return prefix + shortened.toString().stripTrailing() + ellipsis + suffix;
    }

    public static List<String> unresolvedTooltip(ReportResult report) {
        if (report == null || report.unresolved().isEmpty()) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (ReportIssue issue : report.unresolved()) {
            lines.add(issue.quantityLabel() + " — " + issue.reason() + ": "
                    + String.join(", ", issue.aspects()));
        }
        return List.copyOf(lines);
    }

    public static String customSpeciesLine(ReportResult report) {
        if (report == null || report.summary().customSpeciesInstances() <= 0) {
            return "";
        }
        return "Custom/unknown species: " + report.summary().customSpeciesInstances();
    }

    public static Message afterLoad(ReportResult report, String readFailure, boolean anyCategorySelected) {
        if (readFailure != null) {
            return new Message("Could not read the PC: " + readFailure, Tone.ERROR);
        }
        if (report == null) {
            return new Message("No PC data has been read yet. Choose Refresh.", Tone.ERROR);
        }
        if (!anyCategorySelected) {
            return new Message("Select at least one category to build a report.", Tone.WARNING);
        }
        if (report.isEmpty()) {
            return new Message("No Pokémon matched the selected categories.", Tone.MUTED);
        }
        return null;
    }

    public static Message afterCopy(ClipboardResult result, ReportResult report) {
        if (result == null || !result.success()) {
            String error = result == null || result.error() == null ? "unknown clipboard error" : result.error();
            return new Message("Could not copy the report: " + error, Tone.ERROR);
        }
        if (report == null) {
            return new Message("Report copied, but no report data was available.", Tone.WARNING);
        }
        int selected = report.summary().includedInstances();
        if (selected == 0) {
            return new Message("Copied the report; no Pokémon matched.", Tone.MUTED);
        }
        if (!report.completeness().isComplete()) {
            return new Message("Copied " + selected + " Pokémon, but this is a PARTIAL inventory.",
                    Tone.WARNING);
        }
        return new Message("Copied " + selected + " Pokémon to the clipboard.", Tone.SUCCESS);
    }
}
