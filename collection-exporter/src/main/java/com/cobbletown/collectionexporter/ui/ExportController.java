package com.cobbletown.collectionexporter.ui;

import com.cobbletown.collectionexporter.clipboard.ClipboardResult;
import com.cobbletown.collectionexporter.clipboard.ClipboardService;
import com.cobbletown.collectionexporter.data.PcInventoryReader;
import com.cobbletown.collectionexporter.data.PcInventorySnapshot;
import com.cobbletown.collectionexporter.report.ReportEngine;
import com.cobbletown.collectionexporter.report.ReportRequest;
import com.cobbletown.collectionexporter.report.ReportResult;
import com.cobbletown.collectionexporter.report.ReportTextFormatter;

import java.util.Objects;

/**
 * Screen-independent export workflow: read, build, format, copy.
 *
 * <p>A future screen supplies the selected categories and calls these methods. This class does
 * not choose buttons, layout, default filters, or navigation.</p>
 */
public final class ExportController {
    private final PcInventoryReader reader;
    private final ReportEngine engine;
    private final ReportTextFormatter formatter;
    private final ClipboardService clipboard;

    private PcInventorySnapshot lastInventory;
    private ReportResult lastReport;
    private String lastText;

    public ExportController(PcInventoryReader reader,
                            ReportEngine engine,
                            ReportTextFormatter formatter,
                            ClipboardService clipboard) {
        this.reader = Objects.requireNonNull(reader, "reader");
        this.engine = Objects.requireNonNull(engine, "engine");
        this.formatter = Objects.requireNonNull(formatter, "formatter");
        this.clipboard = Objects.requireNonNull(clipboard, "clipboard");
    }

    /** Read the PC once and build the report. Use for screen open/explicit refresh. */
    public ReportResult refresh(ReportRequest request) {
        lastInventory = reader.read();
        return buildFrom(lastInventory, request);
    }

    /** Rebuild from the cached observation. Use for local filter changes. */
    public ReportResult rebuild(ReportRequest request) {
        if (lastInventory == null) {
            return refresh(request);
        }
        return buildFrom(lastInventory, request);
    }

    private ReportResult buildFrom(PcInventorySnapshot inventory, ReportRequest request) {
        lastReport = engine.build(inventory, request);
        lastText = formatter.format(lastReport, request);
        return lastReport;
    }

    public ClipboardResult copyLastReport() {
        if (lastText == null) {
            return ClipboardResult.failure("no report has been generated");
        }
        return clipboard.copy(lastText);
    }

    public PcInventorySnapshot lastInventory() {
        return lastInventory;
    }

    public ReportResult lastReport() {
        return lastReport;
    }

    public String lastText() {
        return lastText;
    }
}
