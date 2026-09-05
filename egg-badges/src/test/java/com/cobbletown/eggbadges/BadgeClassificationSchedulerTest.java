package com.cobbletown.eggbadges;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BadgeClassificationSchedulerTest {
    @Test void stableScreenWaitsBeforeClassificationAndThenCompletesOnce() {
        var scheduler=new BadgeClassificationScheduler();Object screen=new Object(),handler=new Object();
        assertEquals(BadgeClassificationScheduler.RestartReason.SCREEN,scheduler.observe(screen,handler,1,7));
        assertFalse(scheduler.beginWhenStable());
        scheduler.observe(screen,handler,1,7);assertFalse(scheduler.beginWhenStable());
        scheduler.observe(screen,handler,1,7);assertFalse(scheduler.beginWhenStable());
        scheduler.observe(screen,handler,1,7);assertTrue(scheduler.beginWhenStable());
        assertTrue(scheduler.scanning());scheduler.markComplete();assertTrue(scheduler.isComplete());
        scheduler.observe(screen,handler,1,7);assertFalse(scheduler.beginWhenStable());
    }
    @Test void revisionStormWaitsInsteadOfRestartingClassificationEachFrame() {
        var scheduler=new BadgeClassificationScheduler();Object screen=new Object(),handler=new Object();
        scheduler.observe(screen,handler,1,7);
        for(int revision=2;revision<12;revision++){assertEquals(BadgeClassificationScheduler.RestartReason.REVISION,scheduler.observe(screen,handler,revision,7));assertFalse(scheduler.beginWhenStable());assertFalse(scheduler.scanning());}
        scheduler.observe(screen,handler,11,7);scheduler.observe(screen,handler,11,7);scheduler.observe(screen,handler,11,7);
        assertTrue(scheduler.beginWhenStable());
    }
    @Test void configAndContentChangesInvalidateOnlyAfterStability() {
        var scheduler=new BadgeClassificationScheduler();Object screen=new Object(),handler=new Object();
        scheduler.observe(screen,handler,1,7);scheduler.observe(screen,handler,1,7);scheduler.observe(screen,handler,1,7);scheduler.observe(screen,handler,1,7);assertTrue(scheduler.beginWhenStable());scheduler.markComplete();
        assertEquals(BadgeClassificationScheduler.RestartReason.CONFIG,scheduler.observe(screen,handler,1,8));
        scheduler.observe(screen,handler,1,8);scheduler.observe(screen,handler,1,8);scheduler.observe(screen,handler,1,8);assertTrue(scheduler.beginWhenStable());scheduler.markComplete();
        scheduler.invalidateContent();assertFalse(scheduler.scanning());scheduler.observe(screen,handler,1,8);scheduler.observe(screen,handler,1,8);scheduler.observe(screen,handler,1,8);assertTrue(scheduler.beginWhenStable());
    }
    @Test void defensiveValidationIsNotEveryTick() {
        var scheduler=new BadgeClassificationScheduler();Object screen=new Object(),handler=new Object();
        scheduler.observe(screen,handler,1,1);scheduler.observe(screen,handler,1,1);scheduler.observe(screen,handler,1,1);scheduler.observe(screen,handler,1,1);scheduler.beginWhenStable();scheduler.markComplete();
        for(int i=1;i<BadgeClassificationScheduler.DEFENSIVE_CHECK_INTERVAL_TICKS;i++)assertFalse(scheduler.defensiveCheckDue());
        assertTrue(scheduler.defensiveCheckDue());assertFalse(scheduler.defensiveCheckDue());
    }
}
