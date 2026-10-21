package com.cobbletown.collectionexporter.launcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Small in-process registry used by the hosted launcher.
 *
 * <p>Registration is keyed by Fabric mod ID, so a second registration for the same mod cannot
 * produce a duplicate row or button.</p>
 */
public final class SwampModRegistry {
    private static final Map<String, SwampModEntry> ENTRIES = new LinkedHashMap<>();

    private SwampModRegistry() {
    }

    public static synchronized boolean register(SwampModEntry entry) {
        String modId = entry.descriptor().modId();
        if (ENTRIES.containsKey(modId)) {
            return false;
        }
        ENTRIES.put(modId, entry);
        return true;
    }

    public static synchronized List<SwampModEntry> entries() {
        List<SwampModEntry> entries = new ArrayList<>(ENTRIES.values());
        entries.sort(Comparator
                .comparingInt((SwampModEntry entry) -> entry.descriptor().order())
                .thenComparing(entry -> entry.descriptor().displayName(), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.descriptor().modId()));
        return List.copyOf(entries);
    }

    public static synchronized List<SwampModDescriptor> descriptors() {
        return entries().stream().map(SwampModEntry::descriptor).toList();
    }

    public static synchronized SwampModEntry find(String modId) {
        return ENTRIES.get(modId);
    }

    public static synchronized boolean isRegistered(String modId) {
        return ENTRIES.containsKey(modId);
    }
}
