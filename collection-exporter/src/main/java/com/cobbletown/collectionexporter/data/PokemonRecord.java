package com.cobbletown.collectionexporter.data;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * UI-independent record of one client-observed PC Pokémon.
 *
 * <p>The raw {@code aspects} list is preserved in the order Cobblemon exposes it. Classification
 * must not infer a skin from an unfamiliar token, and consumers must keep the instance UUID so
 * duplicate species/forms are never silently merged.</p>
 */
public record PokemonRecord(
        UUID uuid,
        int box,
        int slot,
        String speciesId,
        String speciesName,
        int dexNumber,
        String formId,
        String formName,
        List<String> aspects,
        boolean shiny,
        int level,
        String gender,
        String nickname
) {
    public PokemonRecord {
        Objects.requireNonNull(speciesId, "speciesId");
        aspects = aspects == null ? List.of() : List.copyOf(aspects);
    }

    public String displaySpecies() {
        if (formId == null || formId.isBlank() || "normal".equals(formId)) {
            return speciesName == null || speciesName.isBlank() ? speciesId : speciesName;
        }
        String base = speciesName == null || speciesName.isBlank() ? speciesId : speciesName;
        return base + " (" + formId + ")";
    }

    public boolean hasAspect(String token) {
        return aspects.contains(token);
    }
}
