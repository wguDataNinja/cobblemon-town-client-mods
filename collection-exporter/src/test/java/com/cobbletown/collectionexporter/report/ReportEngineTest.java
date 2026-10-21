package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.TestFixtures;
import com.cobbletown.collectionexporter.classify.ClassificationCatalog;
import com.cobbletown.collectionexporter.classify.ClassificationCatalogLoader;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportEngineTest {
    private final ClassificationCatalog catalog = ClassificationCatalogLoader.loadDefault();
    private final ReportEngine engine = new ReportEngine(catalog);

    @Test
    void allPokemonAppearExactlyOnceAndPreserveDuplicates() {
        ReportResult result = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.ALL));
        assertEquals(37, result.summary().includedInstances());
        assertEquals(30, result.summary().skinInstances());
        assertEquals(12, result.summary().shinySkinInstances());
        assertEquals(18, result.summary().nonShinySkinInstances());
        assertEquals(23, result.summary().uniqueSkinSpeciesForms());
        assertEquals(8, result.skinGroups().size());
        assertEquals(2, find(result, "Crystal", true, "Golem").count());
        assertEquals(2, find(result, "Western", false, "Venusaur").count());
        int listed = result.skinGroups().stream().mapToInt(SkinGroup::instanceCount).sum()
                + result.legendMyth().stream().mapToInt(ReportEntry::count).sum()
                + result.shiny().stream().mapToInt(ReportEntry::count).sum()
                + result.other().stream().mapToInt(ReportEntry::count).sum();
        assertEquals(result.summary().includedInstances(), listed);
    }

    @Test
    void inventoryCountsAreIndependentOfSelectedFilters() {
        ReportResult all = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.ALL));
        ReportResult skinsOnly = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.SKINS));

        assertEquals(37, skinsOnly.counts().totalPokemon());
        assertEquals(30, skinsOnly.counts().customSkins());
        assertEquals(3, skinsOnly.counts().legendaryMythical());
        assertEquals(14, skinsOnly.counts().shiny());
        assertEquals(all.counts(), skinsOnly.counts());
    }

    @Test
    void unresolvedIssuesPreservePokemonNameAndAspects() {
        ReportResult result = engine.build(TestFixtures.unresolvedShinyInventory(),
                ReportRequest.of(ReportCategory.SHINY));
        assertEquals(1, result.unresolved().size());
        ReportIssue issue = result.unresolved().get(0);
        assertEquals("Gyarados", issue.displayName());
        assertEquals("Unrecognized aspect", issue.reason());
        assertEquals(List.of("magikarp-jump-standard", "gyarados-eye-color-red"), issue.aspects());
        assertEquals(1, issue.count());
    }

    @Test
    void customSpeciesIsNotReportedAsUnresolvedClassification() {
        ReportResult result = engine.build(TestFixtures.customSpeciesInventory(),
                ReportRequest.of(ReportCategory.ALL));
        assertEquals(1, result.summary().customSpeciesInstances());
        assertTrue(result.unresolved().isEmpty());
        assertEquals("Custommon", result.other().get(0).displayName());
    }

    @Test
    void logicalOrIncludesUnionOnce() {
        ReportResult result = engine.build(TestFixtures.exampleInventory(),
                ReportRequest.of(ReportCategory.SHINY, ReportCategory.PARADOX));
        // 14 shiny instances + Great Tusk, which is not shiny and not already included.
        assertEquals(15, result.summary().includedInstances());
        assertTrue(result.other().stream().anyMatch(entry -> entry.displayName().equals("Great Tusk")));
        assertTrue(result.shiny().stream().anyMatch(entry -> entry.displayName().equals("Pikachu")));
    }

    @Test
    void legendFilterPresentsSkinnedLegendaryUnderSkins() {
        ReportResult result = engine.build(TestFixtures.exampleInventory(),
                ReportRequest.of(ReportCategory.LEGEND_MYTH));
        assertEquals(3, result.summary().includedInstances());
        assertEquals(2, result.summary().skinInstances());
        assertEquals(1, result.legendMyth().size());
        assertEquals("Mew", result.legendMyth().get(0).displayName());
        assertTrue(result.skinGroups().stream().anyMatch(group -> group.family().displayName().equals("Neon")));
    }

    @Test
    void skinnedShinyLegendaryAppearsOnceInSkinSection() {
        ReportResult result = engine.build(TestFixtures.skinnedShinyLegendaryInventory(),
                ReportRequest.of(ReportCategory.LEGEND_MYTH));
        assertEquals(1, result.summary().includedInstances());
        assertEquals(1, result.summary().skinInstances());
        assertEquals(1, result.summary().shinySkinInstances());
        assertTrue(result.legendMyth().isEmpty());
        assertEquals("Kimono", result.skinGroups().get(0).family().displayName());
        assertEquals("Mew", result.skinGroups().get(0).shiny().get(0).displayName());
    }

    @Test
    void skinFilterPresentsOnlySkins() {
        ReportResult result = engine.build(TestFixtures.exampleInventory(),
                ReportRequest.of(ReportCategory.SKINS));
        assertEquals(30, result.summary().includedInstances());
        assertTrue(result.legendMyth().isEmpty());
        assertTrue(result.shiny().isEmpty());
        assertTrue(result.other().isEmpty());
        assertEquals("Neon", result.skinGroups().get(2).family().displayName());
    }

    @Test
    void nonSkinAndUnknownAspectsAreNotCountedAsSkins() {
        ReportResult result = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.ALL));
        assertFalse(result.skinGroups().stream().flatMap(group -> group.nonShiny().stream())
                .anyMatch(entry -> entry.displayName().equals("Breloom")));
        assertFalse(result.skinGroups().stream().flatMap(group -> group.nonShiny().stream())
                .anyMatch(entry -> entry.displayName().equals("Snivy")));
        assertTrue(result.diagnostics().stream().anyMatch(line -> line.contains("totally_unknown_token")));
    }

    @Test
    void conflictIsExcludedFromSkinTotalsAndReported() {
        ReportResult result = engine.build(TestFixtures.conflictInventory(),
                new ReportRequest(java.util.Set.of(ReportCategory.ALL), true, true));
        assertEquals(0, result.summary().skinInstances());
        assertEquals(1, result.summary().conflictInstances());
        assertEquals(1, result.other().size());
        assertTrue(result.diagnostics().stream().anyMatch(line -> line.contains("skin conflict")));
    }

    @Test
    void emptyInventoryIsExplicitAndEmpty() {
        ReportResult result = engine.build(TestFixtures.emptyInventory(), ReportRequest.of(ReportCategory.ALL));
        assertTrue(result.isEmpty());
        assertEquals(0, result.summary().includedInstances());
        assertEquals("complete client-synchronized PC store", result.completeness().label());
    }

    @Test
    void partialInventoryKeepsCompletenessFalse() {
        ReportResult result = engine.build(TestFixtures.partialInventory(), ReportRequest.of(ReportCategory.ALL));
        assertFalse(result.completeness().isComplete());
        assertEquals(1, result.summary().includedInstances());
    }

    @Test
    void reportIsDeterministic() {
        ReportResult first = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.ALL));
        ReportResult second = engine.build(TestFixtures.exampleInventory(), ReportRequest.of(ReportCategory.ALL));
        assertEquals(first, second);
    }

    private ReportEntry find(ReportResult result, String family, boolean shiny, String species) {
        return result.skinGroups().stream()
                .filter(group -> group.family().displayName().equals(family))
                .flatMap(group -> (shiny ? group.shiny() : group.nonShiny()).stream())
                .filter(entry -> entry.displayName().equals(species))
                .findFirst()
                .orElseThrow();
    }
}
