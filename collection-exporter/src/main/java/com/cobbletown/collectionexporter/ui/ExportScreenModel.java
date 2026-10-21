package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.data.InventoryCompleteness;
import com.cobbletown.collectionexporter.report.ReportCategory;
import com.cobbletown.collectionexporter.report.ReportIssue;
import com.cobbletown.collectionexporter.report.ReportRequest;
import com.cobbletown.collectionexporter.report.ReportResult;

import java.util.List;
import java.util.Set;

/**
 * View model for the export screen. It contains no rendering or layout code.
 *
 * <p>The approved trial defaults are Custom Skins, Legendary &amp; Mythical, and Shiny; All is off.
 * Category selection is a logical OR. Selecting All clears the specific categories and
 * selecting a specific category clears All, which keeps the visible state unambiguous while
 * preserving the report engine's OR semantics.</p>
 */
public final class ExportScreenModel {
    private final FilterSelection filters = FilterSelection.defaults();
    private boolean includeCounts = true;
    private boolean includeDiagnostics;
    private ReportResult report;
    private String reportText;
    private ClipboardResult clipboardResult;

    public Set<ReportCategory> selectedCategories() {
        return filters.asSet();
    }

    public boolean isSelected(ReportCategory category) {
        return filters.isSelected(category);
    }

    public void setSelected(ReportCategory category, boolean selected) {
        filters.setSelected(category, selected);
    }

    /** Applies one user-facing checkbox change with All/specific exclusivity. */
    public void selectCategory(ReportCategory category, boolean selected) {
        if (category == ReportCategory.ALL) {
            filters.clear();
            if (selected) {
                filters.setSelected(ReportCategory.ALL, true);
            }
            return;
        }
        if (selected) {
            filters.setSelected(ReportCategory.ALL, false);
            filters.setSelected(category, true);
        } else {
            filters.setSelected(category, false);
        }
    }

    public void resetFilters() {
        filters.clear();
        FilterSelection defaults = FilterSelection.defaults();
        for (ReportCategory category : defaults.asSet()) {
            filters.setSelected(category, true);
        }
    }

    public boolean includeCounts() {
        return includeCounts;
    }

    public void setIncludeCounts(boolean includeCounts) {
        this.includeCounts = includeCounts;
    }

    public boolean includeDiagnostics() {
        return includeDiagnostics;
    }

    public void setIncludeDiagnostics(boolean includeDiagnostics) {
        this.includeDiagnostics = includeDiagnostics;
    }

    public ReportRequest request() {
        return new ReportRequest(filters.asSet(), includeCounts, includeDiagnostics);
    }

    public ReportResult refresh(ExportController controller) {
        report = controller.refresh(request());
        reportText = controller.lastText();
        clipboardResult = null;
        return report;
    }

    /** Rebuild from the cached observation after a local filter change. */
    public ReportResult rebuild(ExportController controller) {
        report = controller.rebuild(request());
        reportText = controller.lastText();
        clipboardResult = null;
        return report;
    }

    public ClipboardResult copy(ExportController controller) {
        rebuild(controller);
        clipboardResult = controller.copyLastReport();
        return clipboardResult;
    }

    public ReportResult report() {
        return report;
    }

    public String reportText() {
        return reportText;
    }

    public ClipboardResult clipboardResult() {
        return clipboardResult;
    }

    public InventoryCompleteness completeness() {
        return report == null ? InventoryCompleteness.UNAVAILABLE : report.completeness();
    }

    /** Full-inventory count for a filter; unaffected by which filters are currently selected. */
    public int categoryCount(ReportCategory category) {
        return report == null ? 0 : report.counts().countFor(category);
    }

    public List<ReportIssue> unresolved() {
        return report == null ? List.of() : report.unresolved();
    }

    public List<String> diagnostics() {
        return report == null ? List.of() : report.diagnostics();
    }
}
