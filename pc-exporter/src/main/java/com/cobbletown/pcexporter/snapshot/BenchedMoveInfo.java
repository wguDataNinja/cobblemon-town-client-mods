package com.cobbletown.pcexporter.snapshot;

/**
 * One exported benched (previously known) move, preserving the underlying move identity and
 * its Cobblemon PP-raised stage rather than classifying it. Pure data.
 */
public final class BenchedMoveInfo {
    public String name;
    public int ppRaisedStages;

    public BenchedMoveInfo() {
    }
}
