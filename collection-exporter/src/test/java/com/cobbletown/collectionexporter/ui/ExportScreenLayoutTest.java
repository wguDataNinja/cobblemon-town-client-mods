package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.report.ReportCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportScreenLayoutTest {
    @Test
    void wideScreenUsesTwoColumnsWithAlignedLeftAndRightRows() {
        ExportScreenLayout layout = ExportScreenLayout.compute(427, 240);
        assertTrue(layout.twoColumns());
        assertEquals(2 * layout.filterRowHeight(), layout.summaryHeadingY() - layout.filterTop() - 6);
        assertTrue(layout.columnWidth() < layout.contentWidth());
        assertTrue(layout.filterTop() > layout.categoryHeadingY());
        assertEquals(0, layout.filterColumn(ReportCategory.ALL));
        assertEquals(1, layout.filterColumn(ReportCategory.LEGEND_MYTH));
        assertEquals(0, layout.filterColumn(ReportCategory.SKINS));
        assertEquals(1, layout.filterColumn(ReportCategory.SHINY));
        assertEquals(0, layout.filterRow(ReportCategory.ALL));
        assertEquals(0, layout.filterRow(ReportCategory.LEGEND_MYTH));
        assertEquals(1, layout.filterRow(ReportCategory.SKINS));
        assertEquals(1, layout.filterRow(ReportCategory.SHINY));
        assertTrue(layout.buttonsY() + 20 <= layout.panelY() + layout.panelHeight());
    }

    @Test
    void narrowScreenFallsBackToFourRowsWithoutOverlap() {
        ExportScreenLayout layout = ExportScreenLayout.compute(320, 240);
        assertFalse(layout.twoColumns());
        assertEquals(4 * layout.filterRowHeight(), layout.summaryHeadingY() - layout.filterTop() - 6);
        assertEquals(0, layout.filterColumn(ReportCategory.LEGEND_MYTH));
        assertEquals(0, layout.filterRow(ReportCategory.ALL));
        assertEquals(1, layout.filterRow(ReportCategory.SKINS));
        assertEquals(2, layout.filterRow(ReportCategory.LEGEND_MYTH));
        assertEquals(3, layout.filterRow(ReportCategory.SHINY));
        assertTrue(layout.summaryHeadingY() > layout.filterTop() + 3 * layout.filterRowHeight());
        assertTrue(layout.unresolvedY() > layout.selectedY());
        assertTrue(layout.customSpeciesY() > layout.unresolvedY());
        assertTrue(layout.statusY() > layout.customSpeciesY());
        assertTrue(layout.buttonsY() > layout.statusY());
        assertTrue(layout.buttonsY() + 20 <= layout.panelY() + layout.panelHeight());
    }

    @Test
    void layoutStaysInsideSmallWindow() {
        ExportScreenLayout layout = ExportScreenLayout.compute(320, 240);
        assertTrue(layout.panelX() >= 0);
        assertTrue(layout.panelY() >= 0);
        assertTrue(layout.panelX() + layout.panelWidth() <= 320);
        assertTrue(layout.panelY() + layout.panelHeight() <= 240);
        assertTrue(layout.contentX() + layout.contentWidth() <= layout.panelX() + layout.panelWidth());
    }
}
