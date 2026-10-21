package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.report.ReportCategory;

/**
 * Pure layout math for the export screen. Kept free of Minecraft types so scaling and
 * overlap behavior can be tested without a client.
 */
public record ExportScreenLayout(
        int panelX,
        int panelY,
        int panelWidth,
        int panelHeight,
        int contentX,
        int contentWidth,
        boolean twoColumns,
        int columnWidth,
        int columnGap,
        int titleY,
        int snapshotY,
        int categoryHeadingY,
        int filterTop,
        int filterRowHeight,
        int summaryHeadingY,
        int selectedY,
        int unresolvedY,
        int customSpeciesY,
        int statusY,
        int buttonsY
) {
    public static ExportScreenLayout compute(int width, int height) {
        int safeWidth = Math.max(1, width);
        int safeHeight = Math.max(1, height);
        int outerMargin = Math.max(8, Math.min(16, safeWidth / 20));
        int panelWidth = Math.min(420, Math.max(1, safeWidth - outerMargin * 2));
        int panelX = Math.max(0, (safeWidth - panelWidth) / 2);
        int contentX = panelX + 12;
        int contentWidth = Math.max(1, panelWidth - 24);
        boolean twoColumns = contentWidth >= 300;
        int columnGap = 8;
        int columnWidth = twoColumns ? (contentWidth - columnGap) / 2 : contentWidth;
        boolean compact = safeHeight < 250;
        int rowHeight = compact ? 20 : 22;
        int rows = twoColumns ? 2 : 4;

        int panelY = 8;
        int titleY = panelY + 8;
        int snapshotY = titleY + 12;
        int categoryHeadingY = snapshotY + 22;
        int filterTop = categoryHeadingY + 12;
        int summaryHeadingY = filterTop + rows * rowHeight + 6;
        int selectedY = summaryHeadingY + 12;
        int unresolvedY = selectedY + 10;
        int customSpeciesY = unresolvedY + 10;
        int statusY = customSpeciesY + 10;
        int buttonsY = statusY + 14;
        int desiredHeight = buttonsY - panelY + 20 + 8;
        int panelHeight = Math.min(desiredHeight, Math.max(1, safeHeight - 8));
        panelY = Math.max(4, (safeHeight - panelHeight) / 2);

        int shift = panelY - 8;
        return new ExportScreenLayout(
                panelX, panelY, panelWidth, panelHeight,
                contentX, contentWidth, twoColumns, columnWidth, columnGap,
                titleY + shift,
                snapshotY + shift,
                categoryHeadingY + shift,
                filterTop + shift,
                rowHeight,
                summaryHeadingY + shift,
                selectedY + shift,
                unresolvedY + shift,
                customSpeciesY + shift,
                statusY + shift,
                buttonsY + shift);
    }

    /** Left column: All Pokémon, Custom Skins. Right: Legendary & Mythical, Shiny. */
    public int filterColumn(ReportCategory category) {
        if (!twoColumns) {
            return 0;
        }
        return switch (category) {
            case ALL, SKINS -> 0;
            case LEGEND_MYTH, SHINY, PARADOX, ULTRA_BEAST -> 1;
        };
    }

    public int filterRow(ReportCategory category) {
        if (!twoColumns) {
            return switch (category) {
                case ALL -> 0;
                case SKINS -> 1;
                case LEGEND_MYTH -> 2;
                case SHINY -> 3;
                case PARADOX, ULTRA_BEAST -> 0;
            };
        }
        return switch (category) {
            case ALL, LEGEND_MYTH -> 0;
            case SKINS, SHINY, PARADOX, ULTRA_BEAST -> 1;
        };
    }
}
