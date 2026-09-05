package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EggClassifierTest {
    @Test void shippedDefaultsHaveOneGreenPerfectDotOnly() {
        BadgeRule[] rules = BadgeRule.defaults();
        assertFalse(rules[0].enabled);
        assertFalse(rules[1].enabled);
        assertTrue(rules[2].enabled);
        assertFalse(rules[3].enabled);
        assertEquals("●", rules[2].glyph);
        assertEquals(0xFF55DD77, rules[2].color);
        assertEquals(0xFF5599FF, rules[0].color, "M uses the requested blue palette");
        assertEquals(0xFFFF5555, rules[1].color, "F uses the requested red palette");
    }

    @Test void legacyDefaultGenderColorsMigrateWithoutChangingCustomColors() {
        BadgeRule[] legacy = BadgeRule.defaults();
        legacy[0].color = 0xFFFF5555;
        legacy[1].color = 0xFF5599FF;
        assertTrue(EggBadgesConfig.migrateLegacyGenderColors(legacy));
        assertEquals(0xFF5599FF, legacy[0].color);
        assertEquals(0xFFFF5555, legacy[1].color);
        legacy[0].color = 0xFF123456;
        assertFalse(EggBadgesConfig.migrateLegacyGenderColors(legacy));
        assertEquals(0xFF123456, legacy[0].color);
    }

    @Test void ruleFingerprintChangesWhenAnOptionChanges() {
        BadgeRule[] rules = BadgeRule.defaults();
        int before = EggBadgeRenderer.Cache.rulesHash(rules);
        rules[2].enabled = false;
        assertNotEquals(before, EggBadgeRenderer.Cache.rulesHash(rules));
    }

    @Test void formattingStripperIsDeterministicWithoutRegex() {
        assertEquals("HP: 31", EggClassifier.stripFormatting("\u00a7aHP: \u00a7f31"));
    }

    @Test void compactGenderValuesNormalizeToBadgeValues() {
        assertEquals("Male", EggClassifier.normalizeGender("M"));
        assertEquals("Female", EggClassifier.normalizeGender("F"));
    }

    private static EggAttributes attrs(int... ivs) { return new EggAttributes("Eevee", "", "Jolly", "Male", ivs); }

    @Test void perfectSixBy31Matches() {
        BadgeRule r = BadgeRule.defaults()[2];
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,30,31), r));
    }

    @Test void exactlyFiveMatchesButSixDoesNot() {
        BadgeRule r = BadgeRule.defaults()[3];
        r.enabled = true;
        assertTrue(EggClassifier.matches(attrs(31,31,31,31,31,30), r));
        assertFalse(EggClassifier.matches(attrs(31,31,31,31,31,31), r));
    }

    @Test void operatorsGenderSpeciesAndNatureFailClosed() {
        BadgeRule r = new BadgeRule(); r.enabled = true; r.gender = "Male"; r.species = "eevee"; r.nature = "jolly";
        r.values[5] = "30"; r.operators[5] = ">=";
        assertTrue(EggClassifier.matches(attrs(0,0,0,0,0,31), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Female",new int[]{0,0,0,0,0,31}), r));
        assertFalse(EggClassifier.matches(new EggAttributes("Eevee","","Jolly","Male",new int[]{0,0,0,0,0,-1}), r));
    }
}
