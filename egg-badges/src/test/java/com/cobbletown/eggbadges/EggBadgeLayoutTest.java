package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EggBadgeLayoutTest {
    @Test void largeAndSmallGuiLayoutsKeepBottomBarBelowEditor() {
        for (int[] size : new int[][]{{854,480},{1280,720},{2146,1744}}) {
            EggBadgeLayout l=EggBadgeLayout.calculate(size[0],size[1]);
            assertTrue(l.editorX()>l.selectorX()+l.selectorWidth());
            assertTrue(l.secondaryY()+20<l.bottomY());
            assertTrue(l.contentRight()<=size[0]-16);
        }
    }
    @Test void layoutUsesWideContentOnLargeGui() {
        EggBadgeLayout l=EggBadgeLayout.calculate(2146,1744);
        assertEquals(1100,l.contentRight()-l.contentLeft());
        assertTrue(l.editorWidth()>700);
    }
}
