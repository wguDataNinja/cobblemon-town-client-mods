package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.report.ReportCategory;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FilterSelectionTest {
    @Test
    void emptyConstructorIsEmpty() {
        FilterSelection selection = new FilterSelection();
        assertTrue(selection.asSet().isEmpty());
    }

    @Test
    void approvedDefaultsAreSkinsLegendAndShiny() {
        FilterSelection selection = FilterSelection.defaults();
        assertEquals(Set.of(ReportCategory.SKINS, ReportCategory.LEGEND_MYTH, ReportCategory.SHINY),
                selection.asSet());
        assertFalse(selection.isSelected(ReportCategory.ALL));
        assertFalse(selection.isSelected(ReportCategory.PARADOX));
        assertFalse(selection.isSelected(ReportCategory.ULTRA_BEAST));
    }

    @Test
    void togglesAndClearsDeterministically() {
        FilterSelection selection = new FilterSelection();
        selection.toggle(ReportCategory.SKINS);
        assertTrue(selection.isSelected(ReportCategory.SKINS));
        selection.toggle(ReportCategory.SKINS);
        assertFalse(selection.isSelected(ReportCategory.SKINS));
        selection.setSelected(ReportCategory.SHINY, true);
        assertTrue(selection.asSet().contains(ReportCategory.SHINY));
        selection.clear();
        assertTrue(selection.asSet().isEmpty());
    }
}
