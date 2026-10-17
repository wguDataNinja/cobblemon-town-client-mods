package com.cobbletown.pcexporter.snapshot;

/**
 * One exported move. Pure data (no Minecraft types) so it is unit-testable.
 */
public final class MoveInfo {
    public String name;
    public String type;
    public int pp;
    public int maxPp;

    public MoveInfo() {
    }
}
