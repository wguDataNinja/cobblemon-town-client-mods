package com.cobbletown.collectionexporter.classify;

import com.cobbletown.collectionexporter.data.PokemonRecord;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies the owner-reviewable catalog to one Pokémon record.
 *
 * <p>Only confirmed skin aspects can resolve a skin. Unknown tokens and known non-skin/form
 * aspects are preserved as diagnostics and never promoted into the skin inventory.</p>
 */
public final class ClassificationService {
    private final ClassificationCatalog catalog;

    public ClassificationService(ClassificationCatalog catalog) {
        this.catalog = catalog;
    }

    public ClassifiedPokemon classify(PokemonRecord pokemon) {
        List<String> matchedSkins = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        List<String> nonSkin = new ArrayList<>();

        for (String aspect : pokemon.aspects()) {
            if (catalog.neutralAspects().contains(aspect) || "shiny".equals(aspect)) {
                continue;
            }
            if (catalog.isConfirmedSkinAspect(aspect)) {
                matchedSkins.add(aspect);
            } else if (catalog.nonSkinAspects().contains(aspect)) {
                nonSkin.add(aspect);
            } else {
                unknown.add(aspect);
            }
        }

        SkinResolution skin;
        if (matchedSkins.size() == 1) {
            skin = new SkinResolution(
                    SkinResolutionStatus.RESOLVED,
                    catalog.skin(matchedSkins.get(0)),
                    matchedSkins,
                    unknown);
        } else if (matchedSkins.size() > 1) {
            skin = new SkinResolution(
                    SkinResolutionStatus.CONFLICT,
                    null,
                    matchedSkins,
                    unknown);
        } else {
            skin = SkinResolution.none(unknown);
        }

        Set<PokemonCategory> categories = EnumSet.noneOf(PokemonCategory.class);
        categories.addAll(catalog.categoriesFor(pokemon.speciesId()));
        boolean custom = !catalog.isBaseSpecies(pokemon.speciesId());
        if (custom) {
            categories.add(PokemonCategory.CUSTOM_SPECIES);
        }

        return new ClassifiedPokemon(
                pokemon,
                skin,
                categories,
                custom,
                List.copyOf(new LinkedHashSet<>(unknown)),
                List.copyOf(new LinkedHashSet<>(nonSkin)));
    }
}
