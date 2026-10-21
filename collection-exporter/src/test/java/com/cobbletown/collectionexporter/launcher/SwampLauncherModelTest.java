package com.cobbletown.collectionexporter.launcher;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwampLauncherModelTest {
    private static final SwampModDescriptor EGG_BADGES =
            new SwampModDescriptor("egg-badges", "Egg Badges", "Settings", 0);
    private static final SwampModDescriptor COLLECTION_EXPORTER =
            new SwampModDescriptor("collection-exporter", "Collection Exporter", "Export", 100);

    @Test
    void noInstalledModsProducesNoEntry() {
        SwampLauncherModel model = SwampLauncherModel.installed(
                List.of(EGG_BADGES, COLLECTION_EXPORTER), modId -> false);
        assertEquals(SwampLauncherModel.Mode.NONE, model.mode());
        assertTrue(model.entries().isEmpty());
        assertEquals("", model.buttonLabel());
    }

    @Test
    void singleInstalledModProducesDirectEntry() {
        SwampLauncherModel model = SwampLauncherModel.installed(
                List.of(EGG_BADGES, COLLECTION_EXPORTER), Set.of("collection-exporter")::contains);
        assertEquals(SwampLauncherModel.Mode.DIRECT, model.mode());
        assertEquals("Collection Exporter", model.buttonLabel());
        assertEquals("collection-exporter", model.directEntry().orElseThrow().modId());
    }

    @Test
    void multipleInstalledModsProduceSharedSelector() {
        SwampLauncherModel model = SwampLauncherModel.installed(
                List.of(COLLECTION_EXPORTER, EGG_BADGES),
                Set.of("collection-exporter", "egg-badges")::contains);
        assertEquals(SwampLauncherModel.Mode.SELECTOR, model.mode());
        assertEquals("Swamp Mods", model.buttonLabel());
        assertEquals(List.of("Egg Badges", "Collection Exporter"),
                model.entries().stream().map(SwampModDescriptor::displayName).toList());
        assertTrue(model.directEntry().isEmpty());
    }

    @Test
    void uninstalledModsAreNeverAdvertised() {
        SwampLauncherModel model = SwampLauncherModel.installed(
                List.of(EGG_BADGES, COLLECTION_EXPORTER), Set.of("collection-exporter")::contains);
        assertTrue(model.entries().stream()
                .noneMatch(entry -> entry.modId().equals("egg-badges")));
    }

    @Test
    void duplicateDescriptorsCannotCreateDuplicateRows() {
        SwampModDescriptor duplicate =
                new SwampModDescriptor("collection-exporter", "Collection Exporter Duplicate", "duplicate", 50);
        SwampLauncherModel model = SwampLauncherModel.installed(
                List.of(COLLECTION_EXPORTER, duplicate), modId -> true);
        assertEquals(SwampLauncherModel.Mode.DIRECT, model.mode());
        assertEquals(1, model.entries().size());
        assertEquals("Collection Exporter", model.buttonLabel());
    }
}
