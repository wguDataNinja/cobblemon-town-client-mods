package com.cobbletown.eggbadges;

/** Pure state machine: waits for container revision stability before client-thread work. */
final class BadgeClassificationScheduler {
    static final int STABLE_TICKS_REQUIRED=3;
    static final int DEFENSIVE_CHECK_INTERVAL_TICKS=20;
    enum RestartReason { SCREEN, HANDLER, REVISION, CONFIG, CONTENT }
    private Object screen,handler; private int revision=Integer.MIN_VALUE,configHash; private int stableTicks; private boolean pending; private boolean scanning; private boolean complete; private int defensiveTicks;

    RestartReason observe(Object newScreen,Object newHandler,int newRevision,int newConfigHash){
        RestartReason reason=null;
        if(screen!=newScreen){screen=newScreen;handler=newHandler;reason=RestartReason.SCREEN;}
        else if(handler!=newHandler){handler=newHandler;reason=RestartReason.HANDLER;}
        else if(configHash!=newConfigHash){reason=RestartReason.CONFIG;}
        else if(revision!=newRevision){reason=RestartReason.REVISION;}
        if(reason!=null){revision=newRevision;configHash=newConfigHash;stableTicks=0;pending=true;scanning=false;complete=false;defensiveTicks=0;}
        else if(pending)stableTicks++;
        return reason;
    }
    boolean beginWhenStable(){if(pending&&stableTicks>=STABLE_TICKS_REQUIRED){pending=false;scanning=true;return true;}return false;}
    boolean scanning(){return scanning;}
    void markComplete(){scanning=false;complete=true;defensiveTicks=0;}
    boolean defensiveCheckDue(){if(!complete)return false;return ++defensiveTicks>=DEFENSIVE_CHECK_INTERVAL_TICKS&&(defensiveTicks=0)==0;}
    void invalidateContent(){stableTicks=0;pending=true;scanning=false;complete=false;defensiveTicks=0;}
    boolean sameScreen(Object value){return screen==value;}
    boolean isComplete(){return complete;}
}
