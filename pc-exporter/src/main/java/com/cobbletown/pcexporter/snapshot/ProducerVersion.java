package com.cobbletown.pcexporter.snapshot;

/**
 * The exporter's own version, stamped into every snapshot. Kept in sync with
 * {@code gradle.properties} {@code mod_version}.
 */
public final class ProducerVersion {
    public static final String VERSION = "0.3.0";

    private ProducerVersion() {
    }
}
