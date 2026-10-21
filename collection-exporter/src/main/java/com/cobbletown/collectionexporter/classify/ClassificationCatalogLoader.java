package com.cobbletown.collectionexporter.classify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Loads and validates the bundled classification catalog. */
public final class ClassificationCatalogLoader {
    public static final String RESOURCE_PATH = "/assets/collection-exporter/classification-catalog.json";

    private ClassificationCatalogLoader() {
    }

    public static ClassificationCatalog loadDefault() {
        try (InputStream stream = ClassificationCatalogLoader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                throw new CatalogException("missing bundled catalog: " + RESOURCE_PATH);
            }
            return load(stream);
        } catch (IOException failure) {
            throw new CatalogException("could not read bundled catalog: " + failure.getMessage(), failure);
        }
    }

    public static ClassificationCatalog load(InputStream stream) {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) {
                throw new CatalogException("catalog root must be a JSON object");
            }
            return parse(root.getAsJsonObject());
        } catch (CatalogException failure) {
            throw failure;
        } catch (RuntimeException | IOException failure) {
            throw new CatalogException("malformed classification catalog: " + failure.getMessage(), failure);
        }
    }

    private static ClassificationCatalog parse(JsonObject root) {
        int schemaVersion = requiredInt(root, "schemaVersion");
        CatalogProvenance provenance = parseProvenance(requiredObject(root, "provenance"));
        List<SkinFamily> skins = parseSkins(requiredArray(root, "skins"));
        Set<String> neutralAspects = readStringSet(root, "neutralAspects");
        Set<String> nonSkinAspects = readAspectSet(root, "nonSkinAspects");
        Set<String> unresolvedAspects = readAspectSet(root, "unresolvedAspects");
        Map<PokemonCategory, List<CategorySpecies>> categories = parseCategories(requiredObject(root, "categories"));
        Set<String> baseSpecies = readStringSet(root, "baseSpecies");
        Set<String> customSpeciesObserved = readStringSet(root, "customSpeciesObserved");
        List<String> conflicts = readStringList(root, "conflicts");
        return new ClassificationCatalog(
                schemaVersion,
                provenance,
                skins,
                neutralAspects,
                nonSkinAspects,
                unresolvedAspects,
                categories,
                baseSpecies,
                customSpeciesObserved,
                conflicts);
    }

    private static CatalogProvenance parseProvenance(JsonObject provenance) {
        JsonObject registry = provenance.has("skinRegistry") && provenance.get("skinRegistry").isJsonObject()
                ? provenance.getAsJsonObject("skinRegistry")
                : new JsonObject();
        return new CatalogProvenance(
                stringOrNull(provenance, "generatedAt"),
                stringOrNull(provenance, "cobblemonJar"),
                stringOrNull(provenance, "cobblemonJarSha256"),
                stringOrNull(provenance, "speciesDataGlob"),
                stringOrNull(registry, "path"),
                stringOrNull(registry, "sha256"),
                stringOrNull(registry, "schemaVersion"));
    }

    private static List<SkinFamily> parseSkins(JsonArray array) {
        List<SkinFamily> skins = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new CatalogException("each skin entry must be an object");
            }
            JsonObject skin = element.getAsJsonObject();
            skins.add(new SkinFamily(
                    requiredString(skin, "aspect"),
                    stringOrNull(skin, "officialName"),
                    requiredString(skin, "displayName"),
                    readStringList(skin, "aliases"),
                    stringOrNull(skin, "identityStatus"),
                    stringOrNull(skin, "evidenceStatus"),
                    requiredInt(skin, "reportOrder")));
        }
        skins.sort(Comparator.comparingInt(SkinFamily::reportOrder)
                .thenComparing(SkinFamily::displayName));
        return skins;
    }

    private static Map<PokemonCategory, List<CategorySpecies>> parseCategories(JsonObject categories) {
        EnumMap<PokemonCategory, List<CategorySpecies>> parsed = new EnumMap<>(PokemonCategory.class);
        for (PokemonCategory category : PokemonCategory.values()) {
            String key = switch (category) {
                case LEGENDARY -> "legendary";
                case MYTHICAL -> "mythical";
                case PARADOX -> "paradox";
                case ULTRA_BEAST -> "ultraBeast";
                case CUSTOM_SPECIES -> "customSpecies";
            };
            if (!categories.has(key)) {
                parsed.put(category, List.of());
                continue;
            }
            JsonArray array = categories.getAsJsonArray(key);
            List<CategorySpecies> species = new ArrayList<>();
            for (JsonElement element : array) {
                JsonObject value = element.getAsJsonObject();
                species.add(new CategorySpecies(
                        requiredString(value, "species"),
                        requiredString(value, "name"),
                        requiredInt(value, "dex"),
                        readStringList(value, "labels")));
            }
            species.sort(Comparator.comparingInt(CategorySpecies::dexNumber)
                    .thenComparing(CategorySpecies::name));
            parsed.put(category, List.copyOf(species));
        }
        return parsed;
    }

    private static Set<String> readAspectSet(JsonObject root, String key) {
        Set<String> aspects = new LinkedHashSet<>();
        if (!root.has(key)) {
            return aspects;
        }
        for (JsonElement element : root.getAsJsonArray(key)) {
            if (element.isJsonPrimitive()) {
                aspects.add(element.getAsString());
            } else if (element.isJsonObject()) {
                aspects.add(requiredString(element.getAsJsonObject(), "aspect"));
            } else {
                throw new CatalogException(key + " entries must be strings or objects");
            }
        }
        return aspects;
    }

    private static Set<String> readStringSet(JsonObject root, String key) {
        return new LinkedHashSet<>(readStringList(root, key));
    }

    private static List<String> readStringList(JsonObject object, String key) {
        if (!object.has(key)) {
            return List.of();
        }
        JsonArray array = object.getAsJsonArray(key);
        List<String> values = new ArrayList<>();
        for (JsonElement element : array) {
            if (!element.isJsonPrimitive()) {
                throw new CatalogException(key + " entries must be strings");
            }
            values.add(element.getAsString());
        }
        return values;
    }

    private static JsonObject requiredObject(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonObject()) {
            throw new CatalogException("missing object: " + key);
        }
        return object.getAsJsonObject(key);
    }

    private static JsonArray requiredArray(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonArray()) {
            throw new CatalogException("missing array: " + key);
        }
        return object.getAsJsonArray(key);
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new CatalogException("missing string: " + key);
        }
        return object.get(key).getAsString();
    }

    private static String stringOrNull(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    private static int requiredInt(JsonObject object, String key) {
        if (!object.has(key) || !object.get(key).isJsonPrimitive()) {
            throw new CatalogException("missing integer: " + key);
        }
        return object.get(key).getAsInt();
    }

    public static final class CatalogException extends RuntimeException {
        public CatalogException(String message) {
            super(message);
        }

        public CatalogException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
