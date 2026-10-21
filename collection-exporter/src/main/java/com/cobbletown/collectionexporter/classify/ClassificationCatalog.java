package com.cobbletown.collectionexporter.classify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable, owner-reviewable classification data loaded from the mod's bundled catalog.
 *
 * <p>The catalog separates confirmed skin families from non-skin aspects, unresolved observed
 * aspects, and species-level categories. Unknown tokens never become skins by default.</p>
 */
public final class ClassificationCatalog {
    private final int schemaVersion;
    private final CatalogProvenance provenance;
    private final List<SkinFamily> skins;
    private final Map<String, SkinFamily> skinsByAspect;
    private final Set<String> neutralAspects;
    private final Set<String> nonSkinAspects;
    private final Set<String> unresolvedAspects;
    private final Map<PokemonCategory, List<CategorySpecies>> categorySpecies;
    private final Map<String, Set<PokemonCategory>> categoriesBySpecies;
    private final Set<String> baseSpecies;
    private final Set<String> customSpeciesObserved;
    private final List<String> conflicts;

    public ClassificationCatalog(
            int schemaVersion,
            CatalogProvenance provenance,
            List<SkinFamily> skins,
            Set<String> neutralAspects,
            Set<String> nonSkinAspects,
            Set<String> unresolvedAspects,
            Map<PokemonCategory, List<CategorySpecies>> categorySpecies,
            Set<String> baseSpecies,
            Set<String> customSpeciesObserved,
            List<String> conflicts) {
        this.schemaVersion = schemaVersion;
        this.provenance = provenance;
        this.skins = List.copyOf(new ArrayList<>(skins));
        Map<String, SkinFamily> byAspect = new LinkedHashMap<>();
        for (SkinFamily skin : this.skins) {
            byAspect.put(skin.aspect(), skin);
        }
        this.skinsByAspect = Collections.unmodifiableMap(byAspect);
        this.neutralAspects = Collections.unmodifiableSet(new LinkedHashSet<>(neutralAspects));
        this.nonSkinAspects = Collections.unmodifiableSet(new LinkedHashSet<>(nonSkinAspects));
        this.unresolvedAspects = Collections.unmodifiableSet(new LinkedHashSet<>(unresolvedAspects));
        EnumMap<PokemonCategory, List<CategorySpecies>> categories = new EnumMap<>(PokemonCategory.class);
        Map<String, Set<PokemonCategory>> bySpecies = new LinkedHashMap<>();
        for (Map.Entry<PokemonCategory, List<CategorySpecies>> entry : categorySpecies.entrySet()) {
            List<CategorySpecies> values = List.copyOf(entry.getValue());
            categories.put(entry.getKey(), values);
            for (CategorySpecies species : values) {
                bySpecies.computeIfAbsent(species.speciesId(), ignored -> new LinkedHashSet<>())
                        .add(entry.getKey());
            }
        }
        this.categorySpecies = Collections.unmodifiableMap(categories);
        Map<String, Set<PokemonCategory>> immutableBySpecies = new LinkedHashMap<>();
        bySpecies.forEach((species, values) -> immutableBySpecies.put(species, Collections.unmodifiableSet(values)));
        this.categoriesBySpecies = Collections.unmodifiableMap(immutableBySpecies);
        this.baseSpecies = Collections.unmodifiableSet(new LinkedHashSet<>(baseSpecies));
        this.customSpeciesObserved = Collections.unmodifiableSet(new LinkedHashSet<>(customSpeciesObserved));
        this.conflicts = List.copyOf(conflicts);
    }

    public int schemaVersion() {
        return schemaVersion;
    }

    public CatalogProvenance provenance() {
        return provenance;
    }

    public List<SkinFamily> skins() {
        return skins;
    }

    public SkinFamily skin(String aspect) {
        return skinsByAspect.get(aspect);
    }

    public boolean isConfirmedSkinAspect(String aspect) {
        SkinFamily family = skinsByAspect.get(aspect);
        return family != null && family.identityConfirmed();
    }

    public Set<String> neutralAspects() {
        return neutralAspects;
    }

    public Set<String> nonSkinAspects() {
        return nonSkinAspects;
    }

    public Set<String> unresolvedAspects() {
        return unresolvedAspects;
    }

    public List<CategorySpecies> categorySpecies(PokemonCategory category) {
        return categorySpecies.getOrDefault(category, List.of());
    }

    public Set<PokemonCategory> categoriesFor(String speciesId) {
        return categoriesBySpecies.getOrDefault(speciesId, Set.of());
    }

    public boolean isBaseSpecies(String speciesId) {
        return baseSpecies.contains(speciesId);
    }

    public Set<String> baseSpecies() {
        return baseSpecies;
    }

    public Set<String> customSpeciesObserved() {
        return customSpeciesObserved;
    }

    public List<String> conflicts() {
        return conflicts;
    }
}
