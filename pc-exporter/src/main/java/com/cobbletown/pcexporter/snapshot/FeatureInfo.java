package com.cobbletown.pcexporter.snapshot;

import com.google.gson.JsonElement;

/**
 * One exported Cobblemon {@code SpeciesFeature} (persistent per-Pokémon feature such as a
 * season, form trait, or custom/datapack cosmetic feature). Pure data.
 *
 * <p>{@code value} is Cobblemon's own generic serialization of the feature. For the built-in
 * feature types it is the single value under the feature name (e.g. a string, int, or
 * boolean); for a custom/multi-key feature it is the complete serialized object, so nothing
 * is discarded or interpreted.
 */
public final class FeatureInfo {
    public String name;
    public JsonElement value;

    public FeatureInfo() {
    }
}
