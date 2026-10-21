package com.cobbletown.collectionexporter.classify;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassificationCatalogTest {
    private final ClassificationCatalog catalog = ClassificationCatalogLoader.loadDefault();

    @Test
    void loadsBundledCatalogWithProvenance() {
        assertEquals(1, catalog.schemaVersion());
        assertNotNull(catalog.provenance());
        assertEquals("Cobblemon-fabric-1.8.2+1.21.1.jar", catalog.provenance().cobblemonJar());
        assertFalse(catalog.provenance().cobblemonJarSha256().isBlank());
        assertFalse(catalog.provenance().skinRegistrySha256().isBlank());
        assertEquals(8, catalog.skins().size());
    }

    @Test
    void easterIsAConfirmedSkin() {
        SkinFamily easter = catalog.skin("easter2026");
        assertNotNull(easter);
        assertEquals("Easter", easter.displayName());
        assertTrue(easter.identityConfirmed());
    }

    @Test
    void tronIsNeonWithTronAlias() {
        SkinFamily neon = catalog.skin("tron2026");
        assertNotNull(neon);
        assertEquals("Neon", neon.displayName());
        assertEquals("June 2026 Neon Skins", neon.officialName());
        assertTrue(neon.aliases().contains("Tron"));
    }

    @Test
    void nonSkinCorrectionsArePresent() {
        assertTrue(catalog.nonSkinAspects().contains("radiation"));
        assertTrue(catalog.nonSkinAspects().contains("archelian"));
        assertTrue(catalog.nonSkinAspects().contains("alatian"));
        assertTrue(catalog.nonSkinAspects().contains("mega"));
        assertTrue(catalog.nonSkinAspects().contains("alpha"));
        assertTrue(catalog.nonSkinAspects().contains("gmax"));
    }

    @Test
    void categoryListsAreComplete() {
        assertEquals(71, catalog.categorySpecies(PokemonCategory.LEGENDARY).size());
        assertEquals(23, catalog.categorySpecies(PokemonCategory.MYTHICAL).size());
        assertEquals(20, catalog.categorySpecies(PokemonCategory.PARADOX).size());
        assertEquals(11, catalog.categorySpecies(PokemonCategory.ULTRA_BEAST).size());
        assertTrue(catalog.categoriesFor("cobblemon:mewtwo").contains(PokemonCategory.LEGENDARY));
        assertTrue(catalog.categoriesFor("cobblemon:mew").contains(PokemonCategory.MYTHICAL));
        assertTrue(catalog.categoriesFor("cobblemon:greattusk").contains(PokemonCategory.PARADOX));
        assertTrue(catalog.categoriesFor("cobblemon:buzzwole").contains(PokemonCategory.ULTRA_BEAST));
    }

    @Test
    void baseSpeciesAndObservedCustomSpeciesAreSeparated() {
        assertTrue(catalog.isBaseSpecies("cobblemon:pikachu"));
        assertFalse(catalog.isBaseSpecies("cobblemon:bambolt"));
        assertTrue(catalog.customSpeciesObserved().contains("cobblemon:bambolt"));
    }
}
