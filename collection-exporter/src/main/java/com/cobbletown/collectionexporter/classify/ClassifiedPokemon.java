package com.cobbletown.collectionexporter.classify;

import com.cobbletown.collectionexporter.data.PokemonRecord;

import java.util.List;
import java.util.Set;

/** One Pokémon instance with its resolved skin and non-skin categories. */
public record ClassifiedPokemon(
        PokemonRecord pokemon,
        SkinResolution skin,
        Set<PokemonCategory> categories,
        boolean customSpecies,
        List<String> unknownAspects,
        List<String> nonSkinAspects
) {
    public ClassifiedPokemon {
        categories = categories == null ? Set.of() : Set.copyOf(categories);
        unknownAspects = unknownAspects == null ? List.of() : List.copyOf(unknownAspects);
        nonSkinAspects = nonSkinAspects == null ? List.of() : List.copyOf(nonSkinAspects);
    }

    public boolean hasSkin() {
        return skin.resolved();
    }

    public boolean hasCategory(PokemonCategory category) {
        return categories.contains(category);
    }

    public boolean legendaryOrMythical() {
        return hasCategory(PokemonCategory.LEGENDARY) || hasCategory(PokemonCategory.MYTHICAL);
    }
}
