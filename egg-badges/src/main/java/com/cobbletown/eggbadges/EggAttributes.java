package com.cobbletown.eggbadges;

public record EggAttributes(String species, String form, String nature, String gender, int[] ivs) {
    public static final int UNKNOWN = -1;
}
