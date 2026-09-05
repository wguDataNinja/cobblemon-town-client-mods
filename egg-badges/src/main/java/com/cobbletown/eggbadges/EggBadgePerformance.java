package com.cobbletown.eggbadges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;

/** One private aggregate record per handled-screen session; never records item data. */
final class EggBadgePerformance {
    private static final Gson GSON=new GsonBuilder().disableHtmlEscaping().create();
    private final Sample refresh=new Sample(),changed=new Sample(),scan=new Sample(),draw=new Sample();
    private final Map<String,Integer> restarts=new LinkedHashMap<>(); private final int[] revisions=new int[32];
    private int restartCount,revisionCount,classifiedSlots,eggs,loreLines,frames,framesToComplete=-1,over2,over5,over16;
    void refresh(long ns){refresh.add(ns);}void changed(long ns){changed.add(ns);}void scan(long ns){scan.add(ns);}void draw(long ns){draw.add(ns);}
    void classified(boolean egg,int lines){classifiedSlots++;if(egg)eggs++;loreLines+=lines;}void restart(BadgeClassificationScheduler.RestartReason reason){restartCount++;restarts.merge(reason.name().toLowerCase(),1,Integer::sum);}
    void revision(int value){if(revisionCount<revisions.length)revisions[revisionCount++]=value;}void frame(long callbackNs){frames++;if(callbackNs>2_000_000L)over2++;if(callbackNs>5_000_000L)over5++;if(callbackNs>16_700_000L)over16++;}
    void complete(){if(framesToComplete<0)framesToComplete=frames;}
    void emit(){if(!EggBadgesConfig.INSTANCE.performance_diagnostics)return;try{Map<String,Object> record=new LinkedHashMap<>();record.put("schema",1);record.put("refresh",refresh.map());record.put("changed",changed.map());record.put("scan",scan.map());record.put("draw",draw.map());record.put("classified_slots",classifiedSlots);record.put("egg_count",eggs);record.put("lore_line_count",loreLines);record.put("cache_restart_count",restartCount);record.put("cache_restart_reasons",restarts);record.put("revisions",java.util.Arrays.copyOf(revisions,revisionCount));record.put("frames_until_scan_complete",framesToComplete);record.put("callback_frames",frames);record.put("callback_frames_over_2ms",over2);record.put("callback_frames_over_5ms",over5);record.put("callback_frames_over_16_7ms",over16);var p=FabricLoader.getInstance().getConfigDir().resolve("egg-badges/performance.jsonl");Files.createDirectories(p.getParent());Files.writeString(p,GSON.toJson(record)+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception ignored){}}
    private static final class Sample {long total,max,count;void add(long ns){total+=ns;count++;max=Math.max(max,ns);}Map<String,Long> map(){return Map.of("total_ns",total,"count",count,"max_ns",max);}}
}
