package com.cobbletown.pcexporter.snapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One exported PC Pokemon. Pure data (no Minecraft/Cobblemon types) so the JSON shape is
 * unit-testable without a game runtime. Fields mirror the client S2C field set.
 */
public final class PcEntry {
    public int box;
    public int slot;

    public String uuid;
    public String species;
    public String speciesName;
    public int dexNumber;
    /** Display form name (non-canonical, may be localized/renamed). */
    public String form;
    /**
     * Stable, machine-readable form identity from Cobblemon
     * ({@code FormData.formOnlyShowdownId()}): lowercased, punctuation-stripped form name,
     * e.g. {@code normal}, {@code hisui}, {@code archelian}, {@code alolabias}. Null only
     * if the form is unavailable. Never synthesized from the display name.
     */
    public String formId;
    public List<String> aspects = new ArrayList<>();
    public String nickname;

    public String gender;
    public int level;
    /** Exact accumulated experience; {@link #level} alone does not retain progress. */
    public int experience;
    public boolean shiny;
    public String nature;
    /** Mint override nature, if any; the base {@link #nature} is kept separately. Null = none. */
    public String mintedNature;
    public String ability;
    /**
     * Whether {@link #ability} occupies the species/form's hidden ability slot. Resolved
     * from the form ability pool; null when the ability cannot be located there (e.g. a
     * forced/unknown ability).
     */
    public Boolean abilityHidden;
    /**
     * Cobblemon's ability index for {@link #ability} within the form ability pool
     * ({@code Ability.getIndex()}, synced from the server). Null when no ability is present.
     */
    public Integer abilitySlot;
    /**
     * Canonical Cobblemon egg-group ids for the Pokémon's current form, e.g.
     * {@code water_2}, {@code dragon}. Sorted for determinism. Empty when the form exposes
     * none.
     */
    public List<String> eggGroups = new ArrayList<>();

    public Map<String, Integer> ivs = new LinkedHashMap<>();
    public Map<String, Integer> evs = new LinkedHashMap<>();
    public Map<String, Integer> stats = new LinkedHashMap<>();

    public List<MoveInfo> moves = new ArrayList<>();

    public String heldItem;
    public int heldItemCount;
    /** Persistent cosmetic item override (stable item id), independent of {@link #heldItem}. */
    public String cosmeticItem;
    public int cosmeticItemCount;
    /** Active ribbon/mark identity (stable id), if any. Null = none. */
    public String activeMark;
    /** All owned ribbon/mark identities (stable ids), sorted; the underlying identities, not aspects. */
    public List<String> marks = new ArrayList<>();
    /** Persistent per-Pokémon Cobblemon species features (name + generic serialized value). */
    public List<FeatureInfo> features = new ArrayList<>();
    /** Previously known (benched) moves, preserving identity and PP-raised stage. */
    public List<BenchedMoveInfo> benchedMoves = new ArrayList<>();
    public boolean tradeable;
    public int friendship;
    public String caughtBall;

    public String otType;
    public String otName;
    public String otId;
    public Integer otFriendship;

    public String teraType;
    public int dmaxLevel;
    public boolean gmaxFactor;
    public boolean alpha;

    public int currentHealth;
    public int currentFullness;
    public int maxFullness;
    public float scaleModifier;
    public List<Integer> markings = new ArrayList<>();

    public PcEntry() {
    }
}
