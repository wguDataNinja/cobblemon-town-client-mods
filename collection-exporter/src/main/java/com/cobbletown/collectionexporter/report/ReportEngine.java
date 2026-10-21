package com.cobbletown.collectionexporter.report;

import com.cobbletown.collectionexporter.classify.ClassificationCatalog;
import com.cobbletown.collectionexporter.classify.ClassificationService;
import com.cobbletown.collectionexporter.classify.ClassifiedPokemon;
import com.cobbletown.collectionexporter.classify.PokemonCategory;
import com.cobbletown.collectionexporter.classify.SkinFamily;
import com.cobbletown.collectionexporter.classify.SkinResolutionStatus;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import com.cobbletown.collectionexporter.data.PokemonRecord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Deterministic, UI-independent report builder.
 *
 * <p>Filters decide inclusion with logical OR. Presentation priority is fixed:
 * skins, then Legend &amp; Mythic, then shiny, then other. A Pokémon appears exactly once.
 * Instances are never deduplicated; species/form display rows retain their counted instances.</p>
 */
public final class ReportEngine {
    private static final Comparator<ReportEntry> ENTRY_ORDER =
            Comparator.comparing(ReportEntry::displayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(ReportEntry::formId, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER))
                    .thenComparing(ReportEntry::speciesId, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(entry -> entry.instances().isEmpty() ? null : entry.instances().get(0).uuid(),
                            Comparator.nullsFirst(UUID::compareTo));

    private final ClassificationCatalog catalog;
    private final ClassificationService classifier;

    public ReportEngine(ClassificationCatalog catalog) {
        this.catalog = catalog;
        this.classifier = new ClassificationService(catalog);
    }

    public ReportResult build(PcInventorySnapshot inventory, ReportRequest request) {
        return buildModel(inventory, request).result();
    }

    /**
     * Builds the same report plus the classified instances used to produce it. The simulation
     * layouts reuse this model; they do not classify or report independently.
     */
    public ReportModel buildModel(PcInventorySnapshot inventory, ReportRequest request) {
        List<ClassifiedPokemon> all = new ArrayList<>();
        List<ClassifiedPokemon> included = new ArrayList<>();
        for (PokemonRecord record : inventory.entries()) {
            ClassifiedPokemon classified = classifier.classify(record);
            all.add(classified);
            if (selected(classified, request)) {
                included.add(classified);
            }
        }

        Map<SkinFamily, List<ClassifiedPokemon>> bySkin = new LinkedHashMap<>();
        List<ClassifiedPokemon> legendMyth = new ArrayList<>();
        List<ClassifiedPokemon> shiny = new ArrayList<>();
        List<ClassifiedPokemon> other = new ArrayList<>();

        for (ClassifiedPokemon classified : included) {
            if (classified.hasSkin()) {
                bySkin.computeIfAbsent(classified.skin().family(), ignored -> new ArrayList<>()).add(classified);
            } else if (classified.legendaryOrMythical()) {
                legendMyth.add(classified);
            } else if (classified.pokemon().shiny()) {
                shiny.add(classified);
            } else {
                other.add(classified);
            }
        }

        List<SkinGroup> skinGroups = new ArrayList<>();
        bySkin.keySet().stream()
                .sorted(Comparator.comparingInt(SkinFamily::reportOrder)
                        .thenComparing(SkinFamily::displayName, String.CASE_INSENSITIVE_ORDER))
                .forEach(family -> {
                    List<ClassifiedPokemon> familyEntries = bySkin.get(family);
                    List<ReportEntry> familyShiny = toEntries(familyEntries.stream()
                            .filter(entry -> entry.pokemon().shiny()).toList(), family);
                    List<ReportEntry> familyNonShiny = toEntries(familyEntries.stream()
                            .filter(entry -> !entry.pokemon().shiny()).toList(), family);
                    skinGroups.add(new SkinGroup(family, familyShiny, familyNonShiny));
                });

        List<ReportEntry> legendEntries = toEntries(legendMyth, null);
        List<ReportEntry> shinyEntries = toEntries(shiny, null);
        List<ReportEntry> otherEntries = toEntries(other, null);

        ReportSummary summary = summary(included, bySkin, other.size());
        List<String> diagnostics = diagnostics(inventory, included);
        ReportResult result = new ReportResult(
                inventory.capturedAt(),
                inventory.completeness(),
                skinGroups,
                legendEntries,
                shinyEntries,
                otherEntries,
                summary,
                diagnostics,
                counts(all),
                issues(included, SkinResolutionStatus.UNRESOLVED),
                issues(included, SkinResolutionStatus.CONFLICT));
        return new ReportModel(included, result);
    }

    private boolean selected(ClassifiedPokemon classified, ReportRequest request) {
        if (request.categories().isEmpty()) {
            return false;
        }
        if (request.selects(ReportCategory.ALL)) {
            return true;
        }
        if (request.selects(ReportCategory.SKINS) && classified.hasSkin()) {
            return true;
        }
        if (request.selects(ReportCategory.LEGEND_MYTH) && classified.legendaryOrMythical()) {
            return true;
        }
        if (request.selects(ReportCategory.SHINY) && classified.pokemon().shiny()) {
            return true;
        }
        if (request.selects(ReportCategory.PARADOX) && classified.hasCategory(PokemonCategory.PARADOX)) {
            return true;
        }
        return request.selects(ReportCategory.ULTRA_BEAST)
                && classified.hasCategory(PokemonCategory.ULTRA_BEAST);
    }

    public static List<ReportEntry> toEntries(List<ClassifiedPokemon> classified, SkinFamily family) {
        Map<String, List<ClassifiedPokemon>> groups = new LinkedHashMap<>();
        for (ClassifiedPokemon entry : classified) {
            PokemonRecord pokemon = entry.pokemon();
            String skinKey = entry.skin().resolved()
                    ? entry.skin().family().aspect()
                    : entry.skin().status().name();
            String key = pokemon.speciesId() + "\u0000" + nullToEmpty(pokemon.formId())
                    + "\u0000" + pokemon.shiny() + "\u0000" + skinKey;
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
        }
        List<ReportEntry> entries = new ArrayList<>();
        for (List<ClassifiedPokemon> group : groups.values()) {
            List<PokemonRecord> instances = group.stream()
                    .map(ClassifiedPokemon::pokemon)
                    .sorted(Comparator.comparingInt(PokemonRecord::box)
                            .thenComparingInt(PokemonRecord::slot)
                            .thenComparing(record -> record.uuid(), Comparator.nullsFirst(UUID::compareTo)))
                    .toList();
            PokemonRecord first = instances.get(0);
            Set<PokemonCategory> categories = EnumSet.noneOf(PokemonCategory.class);
            group.forEach(entry -> categories.addAll(entry.categories()));
            SkinFamily groupFamily = family;
            if (groupFamily == null) {
                Set<SkinFamily> families = new LinkedHashSet<>();
                group.stream().map(entry -> entry.skin().family()).filter(java.util.Objects::nonNull)
                        .forEach(families::add);
                if (families.size() == 1) {
                    groupFamily = families.iterator().next();
                }
            }
            entries.add(new ReportEntry(
                    first.speciesId(),
                    first.displaySpecies(),
                    first.formId(),
                    first.shiny(),
                    groupFamily,
                    categories,
                    instances));
        }
        entries.sort(ENTRY_ORDER);
        return entries;
    }

    private ReportSummary summary(List<ClassifiedPokemon> included,
                                  Map<SkinFamily, List<ClassifiedPokemon>> bySkin,
                                  int otherInstances) {
        int skin = 0;
        int shinySkin = 0;
        int nonShinySkin = 0;
        Set<String> uniqueSkinSpeciesForms = new LinkedHashSet<>();
        for (List<ClassifiedPokemon> group : bySkin.values()) {
            for (ClassifiedPokemon entry : group) {
                skin++;
                if (entry.pokemon().shiny()) {
                    shinySkin++;
                } else {
                    nonShinySkin++;
                }
                uniqueSkinSpeciesForms.add(entry.pokemon().speciesId() + "|" + nullToEmpty(entry.pokemon().formId()));
            }
        }
        int legendary = 0;
        int mythical = 0;
        int paradox = 0;
        int ultraBeast = 0;
        int shiny = 0;
        int conflicts = 0;
        int unresolved = 0;
        int custom = 0;
        for (ClassifiedPokemon entry : included) {
            if (entry.hasCategory(PokemonCategory.LEGENDARY)) legendary++;
            if (entry.hasCategory(PokemonCategory.MYTHICAL)) mythical++;
            if (entry.hasCategory(PokemonCategory.PARADOX)) paradox++;
            if (entry.hasCategory(PokemonCategory.ULTRA_BEAST)) ultraBeast++;
            if (entry.pokemon().shiny()) shiny++;
            if (entry.skin().status() == SkinResolutionStatus.CONFLICT) conflicts++;
            if (entry.skin().status() == SkinResolutionStatus.UNRESOLVED) unresolved++;
            if (entry.customSpecies()) custom++;
        }
        return new ReportSummary(
                included.size(),
                skin,
                shinySkin,
                nonShinySkin,
                uniqueSkinSpeciesForms.size(),
                legendary,
                mythical,
                paradox,
                ultraBeast,
                shiny,
                otherInstances,
                conflicts,
                unresolved,
                custom);
    }

    private ReportCounts counts(List<ClassifiedPokemon> all) {
        int customSkins = 0;
        int legendaryMythical = 0;
        int shiny = 0;
        for (ClassifiedPokemon entry : all) {
            if (entry.hasSkin()) {
                customSkins++;
            }
            if (entry.legendaryOrMythical()) {
                legendaryMythical++;
            }
            if (entry.pokemon().shiny()) {
                shiny++;
            }
        }
        return new ReportCounts(all.size(), customSkins, legendaryMythical, shiny);
    }

    private List<ReportIssue> issues(List<ClassifiedPokemon> included, SkinResolutionStatus status) {
        Map<IssueKey, List<ClassifiedPokemon>> groups = new LinkedHashMap<>();
        for (ClassifiedPokemon entry : included) {
            if (entry.skin().status() != status) {
                continue;
            }
            List<String> aspects = status == SkinResolutionStatus.UNRESOLVED
                    ? entry.unknownAspects()
                    : entry.skin().matchedAspects();
            if (aspects.isEmpty()) {
                continue;
            }
            IssueKey key = new IssueKey(
                    entry.pokemon().displaySpecies(),
                    reason(status),
                    List.copyOf(new LinkedHashSet<>(aspects)),
                    entry.customSpecies());
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(entry);
        }

        List<ReportIssue> issues = new ArrayList<>();
        groups.forEach((key, entries) -> issues.add(new ReportIssue(
                key.displayName(),
                key.reason(),
                key.aspects(),
                key.customSpecies(),
                entries.size())));
        issues.sort(Comparator.comparing(ReportIssue::displayName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(ReportIssue::reason, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(issue -> String.join(",", issue.aspects()), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(issues);
    }

    private static String reason(SkinResolutionStatus status) {
        return status == SkinResolutionStatus.CONFLICT
                ? "Conflicting skin aspects"
                : "Unrecognized aspect";
    }

    private record IssueKey(String displayName, String reason, List<String> aspects, boolean customSpecies) {
    }

    private List<String> diagnostics(PcInventorySnapshot inventory, List<ClassifiedPokemon> included) {
        Set<String> diagnostics = new LinkedHashSet<>();
        diagnostics.add("completeness: " + inventory.completeness().label());
        diagnostics.addAll(inventory.diagnostics());
        Set<String> customSpecies = new LinkedHashSet<>();
        for (ClassifiedPokemon entry : included) {
            PokemonRecord pokemon = entry.pokemon();
            if (entry.skin().status() == SkinResolutionStatus.CONFLICT) {
                diagnostics.add("skin conflict on " + pokemon.displaySpecies() + ": "
                        + String.join(", ", entry.skin().matchedAspects()));
            } else if (entry.skin().status() == SkinResolutionStatus.UNRESOLVED) {
                diagnostics.add("unresolved skin aspect(s) on " + pokemon.displaySpecies() + ": "
                        + String.join(", ", entry.skin().unknownAspects()));
            }
            if (!entry.unknownAspects().isEmpty()) {
                diagnostics.add("unresolved/unknown aspect(s) on " + pokemon.displaySpecies() + ": "
                        + String.join(", ", entry.unknownAspects()));
            }
            if (entry.customSpecies()) {
                customSpecies.add(pokemon.speciesId());
            }
        }
        if (!customSpecies.isEmpty()) {
            diagnostics.add("custom/unknown species instances: " + String.join(", ", customSpecies));
        }
        return List.copyOf(diagnostics);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
