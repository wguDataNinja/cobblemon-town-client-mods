package com.cobbletown.collectionexporter.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Offline adapter from PC Exporter snapshot schema v1 to the in-game domain model.
 *
 * <p>Read-only. The original snapshot is never modified. Invalid fields are preserved where
 * possible and reported in diagnostics; entries are never silently dropped.</p>
 */
public final class PcExporterSnapshotReader {
    public PcInventorySnapshot read(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return read(reader);
        } catch (IOException failure) {
            throw new SnapshotException("could not read snapshot " + path + ": " + failure.getMessage(), failure);
        }
    }

    public PcInventorySnapshot read(Reader reader) {
        JsonElement rootElement = JsonParser.parseReader(reader);
        if (!rootElement.isJsonObject()) {
            throw new SnapshotException("snapshot root must be a JSON object");
        }
        JsonObject root = rootElement.getAsJsonObject();
        int schemaVersion = intOr(root, "schemaVersion", -1);
        if (schemaVersion != 1) {
            throw new SnapshotException("unsupported snapshot schemaVersion: " + schemaVersion);
        }
        String producer = stringOrNull(root, "producer");
        if (!"pc-exporter".equals(producer)) {
            throw new SnapshotException("unexpected snapshot producer: " + producer);
        }
        String source = stringOrNull(root, "source");
        if (!"cobblemon-client-pc".equals(source)) {
            throw new SnapshotException("unexpected snapshot source: " + source);
        }

        List<String> diagnostics = new ArrayList<>();
        String generatedAt = stringOrNull(root, "generatedAt");
        if (generatedAt == null || generatedAt.isBlank()) {
            generatedAt = "unknown";
            diagnostics.add("snapshot generatedAt is missing");
        }
        InventoryCompleteness completeness = completeness(stringOrNull(root, "completeness"), diagnostics);
        JsonObject store = root.has("store") && root.get("store").isJsonObject()
                ? root.getAsJsonObject("store") : new JsonObject();
        String storeUuid = stringOrNull(store, "uuid");
        int boxCount = intOr(root, "boxCount", intOr(store, "boxCount", 0));
        if (boxCount <= 0) {
            diagnostics.add("snapshot boxCount is missing or invalid");
        }
        if (!root.has("entries") || !root.get("entries").isJsonArray()) {
            throw new SnapshotException("snapshot entries array is missing");
        }

        JsonArray entries = root.getAsJsonArray("entries");
        List<PokemonRecord> records = new ArrayList<>();
        Set<String> seenUuids = new HashSet<>();
        for (int index = 0; index < entries.size(); index++) {
            JsonElement element = entries.get(index);
            if (!element.isJsonObject()) {
                diagnostics.add("entry " + index + " is not an object; preserved as an unknown record");
                records.add(unknownRecord(index));
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            records.add(mapEntry(index, entry, diagnostics, seenUuids));
        }

        return new PcInventorySnapshot(
                generatedAt,
                "cobblemon-client-pc",
                storeUuid,
                completeness,
                boxCount,
                intOr(store, "observedBoxCount", 0),
                records,
                diagnostics);
    }

    private static PokemonRecord mapEntry(int index, JsonObject entry, List<String> diagnostics, Set<String> seenUuids) {
        String uuidText = stringOrNull(entry, "uuid");
        UUID uuid = null;
        if (uuidText == null || uuidText.isBlank()) {
            diagnostics.add("entry " + index + " has no uuid");
        } else {
            try {
                uuid = UUID.fromString(uuidText);
                if (!seenUuids.add(uuidText)) {
                    diagnostics.add("duplicate uuid " + uuidText + " at entry " + index);
                }
            } catch (IllegalArgumentException invalid) {
                diagnostics.add("entry " + index + " has invalid uuid " + uuidText);
            }
        }

        String species = stringOrNull(entry, "species");
        if (species == null || species.isBlank()) {
            species = "unknown";
            diagnostics.add("entry " + index + " has no species");
        }
        String speciesName = stringOrNull(entry, "speciesName");
        if (speciesName == null || speciesName.isBlank()) {
            speciesName = species;
            diagnostics.add("entry " + index + " has no speciesName; using species id");
        }
        String formId = stringOrNull(entry, "formId");
        if (formId == null || formId.isBlank()) {
            formId = "normal";
            diagnostics.add("entry " + index + " has no formId; assuming normal");
        }
        String formName = stringOrNull(entry, "form");
        boolean shiny;
        if (!entry.has("shiny") || entry.get("shiny").isJsonNull()) {
            shiny = false;
            diagnostics.add("entry " + index + " has no shiny field; assuming false");
        } else {
            shiny = entry.get("shiny").getAsBoolean();
        }
        int box = intOr(entry, "box", -1);
        int slot = intOr(entry, "slot", -1);
        if (box < 0 || slot < 0) {
            diagnostics.add("entry " + index + " has invalid box/slot");
        }
        int level = intOr(entry, "level", 0);
        if (!entry.has("level")) {
            diagnostics.add("entry " + index + " has no level; assuming 0");
        }
        int dexNumber = intOr(entry, "dexNumber", 0);
        List<String> aspects = new ArrayList<>();
        if (!entry.has("aspects") || !entry.get("aspects").isJsonArray()) {
            diagnostics.add("entry " + index + " has no aspects array; using empty aspects");
        } else {
            for (JsonElement aspect : entry.getAsJsonArray("aspects")) {
                if (aspect.isJsonPrimitive()) {
                    aspects.add(aspect.getAsString());
                } else {
                    diagnostics.add("entry " + index + " has a non-string aspect; ignored");
                }
            }
        }
        return new PokemonRecord(
                uuid,
                box,
                slot,
                species,
                speciesName,
                dexNumber,
                formId,
                formName,
                aspects,
                shiny,
                level,
                stringOrNull(entry, "gender"),
                stringOrNull(entry, "nickname"));
    }

    private static PokemonRecord unknownRecord(int index) {
        return new PokemonRecord(
                null,
                -1,
                -1,
                "unknown",
                "unknown",
                0,
                "normal",
                null,
                List.of(),
                false,
                0,
                null,
                null);
    }

    private static InventoryCompleteness completeness(String value, List<String> diagnostics) {
        if ("complete-synchronized-store".equals(value)) {
            return InventoryCompleteness.COMPLETE_SYNCHRONIZED_STORE;
        }
        if ("partial-observed".equals(value)) {
            return InventoryCompleteness.PARTIAL_INCOMPLETE_STORE;
        }
        diagnostics.add("snapshot completeness is missing or unknown: " + value);
        return InventoryCompleteness.PARTIAL_INCOMPLETE_STORE;
    }

    private static String stringOrNull(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }

    private static int intOr(JsonObject object, String key, int fallback) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return object.get(key).getAsInt();
        } catch (RuntimeException invalid) {
            return fallback;
        }
    }

    public static final class SnapshotException extends RuntimeException {
        public SnapshotException(String message) {
            super(message);
        }

        public SnapshotException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
