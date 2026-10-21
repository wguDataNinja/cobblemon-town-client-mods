package com.cobbletown.collectionexporter.report;

/**
 * Full-inventory counts for the screen filters. These counts are independent of the
 * currently selected filters; selecting a category never changes another category's count.
 */
public record ReportCounts(
        int totalPokemon,
        int customSkins,
        int legendaryMythical,
        int shiny
) {
    public int countFor(ReportCategory category) {
        return switch (category) {
            case ALL -> totalPokemon;
            case SKINS -> customSkins;
            case LEGEND_MYTH -> legendaryMythical;
            case SHINY -> shiny;
            case PARADOX, ULTRA_BEAST -> 0;
        };
    }
}
