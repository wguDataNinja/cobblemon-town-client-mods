package com.cobbletown.collectionexporter.classify;

import com.cobbletown.collectionexporter.data.PokemonRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassificationServiceTest {
    private final ClassificationCatalog catalog = ClassificationCatalogLoader.loadDefault();
    private final ClassificationService service = new ClassificationService(catalog);

    @Test
    void easterSnorlaxResolvesToEaster() {
        ClassifiedPokemon result = service.classify(record("cobblemon:snorlax", "Snorlax",
                List.of("male", "easter2026"), false));
        assertTrue(result.hasSkin());
        assertEquals("Easter", result.skin().family().displayName());
        assertFalse(result.pokemon().shiny());
    }

    @Test
    void radiationBreloomIsNotASkin() {
        ClassifiedPokemon result = service.classify(record("cobblemon:breloom", "Breloom",
                List.of("male", "radiation"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.NONE, result.skin().status());
        assertTrue(result.nonSkinAspects().contains("radiation"));
    }

    @Test
    void eiscueIceFaceIsAFormNotASkin() {
        ClassifiedPokemon result = service.classify(record("cobblemon:eiscue", "Eiscue",
                List.of("male", "ice_face"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.NONE, result.skin().status());
        assertTrue(result.nonSkinAspects().contains("ice_face"));
    }

    @Test
    void archelianIsAFormNotASkin() {
        ClassifiedPokemon result = service.classify(record("cobblemon:snivy", "Snivy",
                List.of("female", "archelian"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.NONE, result.skin().status());
        assertTrue(result.nonSkinAspects().contains("archelian"));
    }

    @Test
    void tron2026ResolvesAsNeonWithAlias() {
        ClassifiedPokemon result = service.classify(record("cobblemon:dragapult", "Dragapult",
                List.of("shiny", "male", "tron2026"), true));
        assertTrue(result.hasSkin());
        assertEquals("Neon", result.skin().family().displayName());
        assertTrue(result.skin().family().aliases().contains("Tron"));
        assertTrue(result.pokemon().shiny());
    }

    @Test
    void formTokenAlongsideSkinDoesNotBlockResolution() {
        ClassifiedPokemon result = service.classify(record("cobblemon:aegislash", "Aegislash",
                List.of("shiny", "male", "shield-forme", "july2026kimono"), true));
        assertTrue(result.hasSkin());
        assertEquals("Kimono", result.skin().family().displayName());
        assertTrue(result.nonSkinAspects().contains("shield-forme"));
    }

    @Test
    void unknownAspectIsUnresolvedNotSkin() {
        ClassifiedPokemon result = service.classify(record("cobblemon:eevee", "Eevee",
                List.of("female", "totally_unknown_token"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.UNRESOLVED, result.skin().status());
        assertTrue(result.unknownAspects().contains("totally_unknown_token"));
    }

    @Test
    void observedButUnapprovedTokenIsUnresolved() {
        ClassifiedPokemon result = service.classify(record("cobblemon:magikarp", "Magikarp",
                List.of("male", "magikarp-jump-standard"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.UNRESOLVED, result.skin().status());
    }

    @Test
    void multipleConfirmedSkinTokensConflict() {
        ClassifiedPokemon result = service.classify(record("cobblemon:snorlax", "Snorlax",
                List.of("male", "easter2026", "petal2026"), false));
        assertFalse(result.hasSkin());
        assertEquals(SkinResolutionStatus.CONFLICT, result.skin().status());
        assertEquals(2, result.skin().matchedAspects().size());
    }

    @Test
    void customSpeciesIsMarkedSeparately() {
        ClassifiedPokemon result = service.classify(record("cobblemon:bambolt", "Bambolt",
                List.of("male"), false));
        assertTrue(result.customSpecies());
        assertTrue(result.hasCategory(PokemonCategory.CUSTOM_SPECIES));
    }

    @Test
    void officialSpeciesCategoriesResolve() {
        assertTrue(service.classify(record("cobblemon:mewtwo", "Mewtwo", List.of("genderless"), false))
                .hasCategory(PokemonCategory.LEGENDARY));
        assertTrue(service.classify(record("cobblemon:mew", "Mew", List.of("genderless"), false))
                .hasCategory(PokemonCategory.MYTHICAL));
        assertTrue(service.classify(record("cobblemon:greattusk", "Great Tusk", List.of("genderless"), false))
                .hasCategory(PokemonCategory.PARADOX));
        assertTrue(service.classify(record("cobblemon:buzzwole", "Buzzwole", List.of("male"), false))
                .hasCategory(PokemonCategory.ULTRA_BEAST));
    }

    private PokemonRecord record(String species, String name, List<String> aspects, boolean shiny) {
        return new PokemonRecord(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                0, 0, species, name, 0, "normal", "Normal", aspects, shiny, 50, "male", null);
    }
}
