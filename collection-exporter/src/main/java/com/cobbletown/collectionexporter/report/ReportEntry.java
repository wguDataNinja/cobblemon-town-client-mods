package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.PokemonCategory;
import com.cobbletown.collectionexporter.classify.SkinFamily;
import com.cobbletown.collectionexporter.data.PokemonRecord;

import java.util.List;
import java.util.Set;

/** A display group of one or more preserved Pokémon instances. */
public record ReportEntry(
        String speciesId,
        String displayName,
        String formId,
        boolean shiny,
        SkinFamily skinFamily,
        Set<PokemonCategory> categories,
        List<PokemonRecord> instances
) {
    public ReportEntry {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        instances = instances == null ? List.of() : List.copyOf(instances);
    }

    public int count() {
        return instances.size();
    }

    public String quantityLabel() {
        return count() > 1 ? displayName + " ×" + count() : displayName;
    }
}
