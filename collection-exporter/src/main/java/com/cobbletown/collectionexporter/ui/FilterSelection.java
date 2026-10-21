package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.report.ReportCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Filter state for the export screen. */
public final class FilterSelection {
    private final EnumSet<ReportCategory> selected;

    public FilterSelection() {
        this.selected = EnumSet.noneOf(ReportCategory.class);
    }

    private FilterSelection(EnumSet<ReportCategory> selected) {
        this.selected = selected;
    }

    /** Approved trial defaults: Custom Skins, Legendary &amp; Mythical, and Shiny; All is off. */
    public static FilterSelection defaults() {
        EnumSet<ReportCategory> selected = EnumSet.noneOf(ReportCategory.class);
        selected.add(ReportCategory.SKINS);
        selected.add(ReportCategory.LEGEND_MYTH);
        selected.add(ReportCategory.SHINY);
        return new FilterSelection(selected);
    }

    public boolean isSelected(ReportCategory category) {
        return selected.contains(category);
    }

    public void setSelected(ReportCategory category, boolean value) {
        if (value) {
            selected.add(category);
        } else {
            selected.remove(category);
        }
    }

    public void toggle(ReportCategory category) {
        setSelected(category, !isSelected(category));
    }

    public void clear() {
        selected.clear();
    }

    public Set<ReportCategory> asSet() {
        return Collections.unmodifiableSet(EnumSet.copyOf(selected));
    }
}
