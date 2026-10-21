package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.SkinFamily;

import java.util.List;

/** One official skin collection, split into Shiny and Non-Shiny groups. */
public record SkinGroup(
        SkinFamily family,
        List<ReportEntry> shiny,
        List<ReportEntry> nonShiny
) {
    public SkinGroup {
        shiny = shiny == null ? List.of() : List.copyOf(shiny);
        nonShiny = nonShiny == null ? List.of() : List.copyOf(nonShiny);
    }

    public int instanceCount() {
        return shiny.stream().mapToInt(ReportEntry::count).sum()
                + nonShiny.stream().mapToInt(ReportEntry::count).sum();
    }
}
