package com.cobbletown.collectionexporter.launcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Pure routing policy for the single Escape-menu entry.
 *
 * <p>One available mod opens directly. Two or more available mods open the shared selector.
 * Duplicate mod IDs are ignored, and mods that are not installed are never listed.</p>
 */
public final class SwampLauncherModel {
    public enum Mode {
        NONE,
        DIRECT,
        SELECTOR
    }

    private static final Comparator<SwampModDescriptor> ORDER =
            Comparator.comparingInt(SwampModDescriptor::order)
                    .thenComparing(SwampModDescriptor::displayName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(SwampModDescriptor::modId);

    private final List<SwampModDescriptor> entries;

    private SwampLauncherModel(List<SwampModDescriptor> entries) {
        this.entries = List.copyOf(entries);
    }

    public static SwampLauncherModel installed(Collection<SwampModDescriptor> candidates,
                                               Predicate<String> isModLoaded) {
        Map<String, SwampModDescriptor> unique = new LinkedHashMap<>();
        for (SwampModDescriptor candidate : candidates) {
            if (candidate == null || !isModLoaded.test(candidate.modId())) {
                continue;
            }
            unique.putIfAbsent(candidate.modId(), candidate);
        }
        List<SwampModDescriptor> sorted = new ArrayList<>(unique.values());
        sorted.sort(ORDER);
        return new SwampLauncherModel(sorted);
    }

    public Mode mode() {
        if (entries.isEmpty()) {
            return Mode.NONE;
        }
        return entries.size() == 1 ? Mode.DIRECT : Mode.SELECTOR;
    }

    public String buttonLabel() {
        return switch (mode()) {
            case NONE -> "";
            case DIRECT -> entries.get(0).displayName();
            case SELECTOR -> "Swamp Mods";
        };
    }

    public Optional<SwampModDescriptor> directEntry() {
        return mode() == Mode.DIRECT ? Optional.of(entries.get(0)) : Optional.empty();
    }

    public List<SwampModDescriptor> entries() {
        return entries;
    }
}
