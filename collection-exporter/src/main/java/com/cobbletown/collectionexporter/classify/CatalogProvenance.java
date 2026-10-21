package com.cobbletown.collectionexporter.classify;

/** Provenance of the generated classification catalog. */
public record CatalogProvenance(
        String generatedAt,
        String cobblemonJar,
        String cobblemonJarSha256,
        String speciesDataGlob,
        String skinRegistryPath,
        String skinRegistrySha256,
        String skinRegistrySchemaVersion
) {
}
