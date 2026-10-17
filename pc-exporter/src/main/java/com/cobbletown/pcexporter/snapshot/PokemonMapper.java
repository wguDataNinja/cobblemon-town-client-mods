package com.cobbletown.pcexporter.snapshot;

import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.BenchedMoves;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.abilities.Ability;
import com.cobblemon.mod.common.api.abilities.PotentialAbility;
import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.api.types.tera.TeraType;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.PokemonStats;
import com.cobblemon.mod.common.pokemon.abilities.HiddenAbilityType;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Read-only mapping from a client-synchronized Cobblemon {@link Pokemon} to pure
 * {@link PcEntry} data. Uses only public Cobblemon getters; never mutates the Pokemon.
 */
public final class PokemonMapper {
    private PokemonMapper() {
    }

    public static PcEntry map(int box, int slot, Pokemon pokemon) {
        PcEntry e = new PcEntry();
        e.box = box;
        e.slot = slot;

        e.uuid = pokemon.getUuid() != null ? pokemon.getUuid().toString() : null;
        if (pokemon.getSpecies() != null) {
            e.species = String.valueOf(pokemon.getSpecies().getResourceIdentifier());
            e.speciesName = safeText(pokemon.getSpecies().getTranslatedName());
            e.dexNumber = pokemon.getSpecies().getNationalPokedexNumber();
        }
        e.form = pokemon.getForm() != null ? pokemon.getForm().getName() : null;
        e.formId = pokemon.getForm() != null ? pokemon.getForm().formOnlyShowdownId() : null;
        e.aspects = new ArrayList<>(pokemon.getAspects());
        Text nickname = pokemon.getNickname();
        e.nickname = nickname == null ? null : nickname.getString();

        e.gender = pokemon.getGender() != null ? pokemon.getGender().getShowdownName() : null;
        e.level = pokemon.getLevel();
        e.experience = pokemon.getExperience();
        e.shiny = pokemon.getShiny();
        e.nature = pokemon.getNature() != null ? String.valueOf(pokemon.getNature().getName()) : null;
        e.mintedNature = pokemon.getMintedNature() != null
                ? String.valueOf(pokemon.getMintedNature().getName()) : null;
        e.ability = pokemon.getAbility() != null ? pokemon.getAbility().getName() : null;
        e.abilityHidden = abilityHidden(pokemon);
        e.abilitySlot = abilitySlot(pokemon);
        e.eggGroups = eggGroups(pokemon);

        e.ivs = statMap(pokemon.getIvs());
        e.evs = statMap(pokemon.getEvs());
        e.stats = new LinkedHashMap<>();
        for (Stat stat : Stats.Companion.getPERMANENT()) {
            e.stats.put(stat.getShowdownId(), pokemon.getStat(stat));
        }

        if (pokemon.getMoveSet() != null) {
            for (Move move : pokemon.getMoveSet().getMoves()) {
                if (move == null) {
                    continue;
                }
                MoveInfo mi = new MoveInfo();
                mi.name = move.getName();
                mi.type = move.getType() != null ? move.getType().getName() : null;
                mi.pp = move.getCurrentPp();
                mi.maxPp = move.getMaxPp();
                e.moves.add(mi);
            }
        }

        ItemStack held = pokemon.heldItem();
        if (held != null && !held.isEmpty()) {
            e.heldItem = Registries.ITEM.getId(held.getItem()).toString();
            e.heldItemCount = held.getCount();
        }

        ItemStack cosmetic = pokemon.getCosmeticItem();
        if (cosmetic != null && !cosmetic.isEmpty()) {
            e.cosmeticItem = Registries.ITEM.getId(cosmetic.getItem()).toString();
            e.cosmeticItemCount = cosmetic.getCount();
        }

        e.activeMark = markId(pokemon.getActiveMark());
        Set<Mark> marks = pokemon.getMarks();
        List<String> markIds = new ArrayList<>();
        if (marks != null) {
            for (Mark mark : marks) {
                String id = markId(mark);
                if (id != null) {
                    markIds.add(id);
                }
            }
        }
        markIds.sort(Comparator.naturalOrder());
        e.marks = markIds;

        List<SpeciesFeature> features = pokemon.getFeatures();
        if (features != null) {
            for (SpeciesFeature feature : features) {
                if (feature == null) {
                    continue;
                }
                FeatureInfo info = new FeatureInfo();
                info.name = feature.getName();
                JsonObject serialized = feature.saveToJSON(new JsonObject());
                if (serialized != null && serialized.size() == 1 && serialized.has(info.name)) {
                    info.value = serialized.get(info.name);
                } else {
                    info.value = serialized;
                }
                e.features.add(info);
            }
        }

        BenchedMoves benched = pokemon.getBenchedMoves();
        if (benched != null) {
            for (BenchedMove benchedMove : benched) {
                if (benchedMove == null || benchedMove.getMoveTemplate() == null) {
                    continue;
                }
                BenchedMoveInfo info = new BenchedMoveInfo();
                info.name = benchedMove.getMoveTemplate().getName();
                info.ppRaisedStages = benchedMove.getPpRaisedStages();
                e.benchedMoves.add(info);
            }
        }

        e.tradeable = pokemon.getTradeable();
        e.friendship = pokemon.getFriendship();
        e.caughtBall = pokemon.getCaughtBall() != null ? String.valueOf(pokemon.getCaughtBall().getName()) : null;

        e.otType = pokemon.getOriginalTrainerType() != null ? pokemon.getOriginalTrainerType().name() : null;
        e.otName = pokemon.getOriginalTrainerName();
        e.otId = pokemon.getOriginalTrainer();
        e.otFriendship = pokemon.getOriginalTrainerFriendship();

        TeraType tera = pokemon.getTeraType();
        e.teraType = tera != null ? tera.getName() : null;
        e.dmaxLevel = pokemon.getDmaxLevel();
        e.gmaxFactor = pokemon.getGmaxFactor();
        e.alpha = pokemon.isAlpha();

        e.currentHealth = pokemon.getCurrentHealth();
        e.currentFullness = pokemon.getCurrentFullness();
        e.maxFullness = pokemon.getMaxFullness();
        e.scaleModifier = pokemon.getScaleModifier();
        e.markings = new ArrayList<>(pokemon.getMarkings());
        return e;
    }

    private static Map<String, Integer> statMap(PokemonStats stats) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (stats == null) {
            return out;
        }
        for (Stat stat : Stats.Companion.getPERMANENT()) {
            out.put(stat.getShowdownId(), stats.getOrDefault(stat));
        }
        return out;
    }

    private static String safeText(Text text) {
        return text == null ? null : text.getString();
    }

    /** Stable mark/ribbon identity as a string, or null when absent. */
    private static String markId(Mark mark) {
        return mark == null || mark.getIdentifier() == null ? null : mark.getIdentifier().toString();
    }

    /**
     * Whether the Pokémon's current ability occupies the form's hidden ability slot.
     * Resolved from the form ability pool. Returns null when the ability cannot be located
     * (e.g. a forced ability).
     */
    private static Boolean abilityHidden(Pokemon pokemon) {
        Ability ability = pokemon.getAbility();
        FormData form = pokemon.getForm();
        if (ability == null || ability.getTemplate() == null || form == null || form.getAbilities() == null) {
            return null;
        }
        String name = ability.getName();
        for (PotentialAbility potential : form.getAbilities()) {
            if (potential.getTemplate() != null && name.equals(potential.getTemplate().getName())) {
                return potential.getType() == HiddenAbilityType.INSTANCE;
            }
        }
        return null;
    }

    /**
     * 0-based position of the current ability within the form ability pool (the species
     * ability list order), or null when no ability is present / it cannot be located.
     *
     * <p>This is a stable derived coordinate. Cobblemon's {@code Ability.getIndex()} is not
     * used because it is not reliably populated on the client (it reads 0 for many Pokémon,
     * including hidden abilities).
     */
    private static Integer abilitySlot(Pokemon pokemon) {
        Ability ability = pokemon.getAbility();
        FormData form = pokemon.getForm();
        if (ability == null || ability.getTemplate() == null || form == null || form.getAbilities() == null) {
            return null;
        }
        String name = ability.getName();
        int index = 0;
        for (PotentialAbility potential : form.getAbilities()) {
            if (potential.getTemplate() != null && name.equals(potential.getTemplate().getName())) {
                return index;
            }
            index++;
        }
        return null;
    }

    /** Canonical, sorted egg-group ids for the Pokémon's current form (fallback: species). */
    private static List<String> eggGroups(Pokemon pokemon) {
        FormData form = pokemon.getForm();
        Set<EggGroup> groups = form != null ? form.getEggGroups() : null;
        if (groups == null || groups.isEmpty()) {
            groups = pokemon.getSpecies() != null ? pokemon.getSpecies().getEggGroups() : null;
        }
        List<String> out = new ArrayList<>();
        if (groups != null) {
            for (EggGroup group : groups) {
                String id = eggGroupId(group);
                if (id != null) {
                    out.add(id);
                }
            }
        }
        out.sort(Comparator.naturalOrder());
        return out;
    }

    /**
     * Normalizes a Cobblemon {@link EggGroup} to the canonical data id form used in species
     * JSON (lowercase, spaces/dashes to underscores), e.g. {@code Water 2} to {@code water_2}.
     */
    private static String eggGroupId(EggGroup group) {
        if (group == null || group.getShowdownID() == null) {
            return null;
        }
        return group.getShowdownID().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
