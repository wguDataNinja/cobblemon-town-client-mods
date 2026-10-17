package com.cobbletown.pcexporter.snapshot;

/**
 * Result of one export attempt. Kept UI-agnostic so feedback lives in the UI layer.
 */
public final class ExportResult {
    public enum Status { SUCCESS, BUSY, UNAVAILABLE, ERROR }

    public final Status status;
    public final int count;
    public final String path;
    public final String error;

    private ExportResult(Status status, int count, String path, String error) {
        this.status = status;
        this.count = count;
        this.path = path;
        this.error = error;
    }

    public static ExportResult success(int count, String path) {
        return new ExportResult(Status.SUCCESS, count, path, null);
    }

    public static ExportResult busy() {
        return new ExportResult(Status.BUSY, 0, null, "an export is already in progress");
    }

    public static ExportResult unavailable(String reason) {
        return new ExportResult(Status.UNAVAILABLE, 0, null, reason);
    }

    public static ExportResult error(String reason) {
        return new ExportResult(Status.ERROR, 0, null, reason);
    }

    public boolean ok() {
        return status == Status.SUCCESS;
    }
}
