package com.cobbletown.eggbadges;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EggBadgesConfig {
    public static final EggBadgesConfig INSTANCE=new EggBadgesConfig();
    private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
    private static final int CURRENT_SCHEMA_VERSION=2;
    public int schema_version=CURRENT_SCHEMA_VERSION;
    /** Diagnostic-local aggregate timing spool; disabled in ordinary use. */
    public boolean performance_diagnostics=false;
    public BadgeRule[] badges=BadgeRule.defaults();
    private Path path(){return FabricLoader.getInstance().getConfigDir().resolve("egg-badges/config.json");}
    public void load(){try{if(Files.exists(path())){EggBadgesConfig x=GSON.fromJson(Files.readString(path()),EggBadgesConfig.class);if(x!=null&&x.schema_version>=CURRENT_SCHEMA_VERSION&&x.badges!=null&&x.badges.length==5){badges=x.badges;if(migrateLegacyGenderColors(badges))save();}else{schema_version=CURRENT_SCHEMA_VERSION;badges=BadgeRule.defaults();save();}}else save();}catch(Exception e){schema_version=CURRENT_SCHEMA_VERSION;badges=BadgeRule.defaults();}}
    public void save(){try{Files.createDirectories(path().getParent());Files.writeString(path(),GSON.toJson(this));}catch(Exception ignored){}}
    public void reset(){badges=BadgeRule.defaults();save();}
    /** Upgrade only the known 0.1.2 default palette; never overwrite a custom color. */
    static boolean migrateLegacyGenderColors(BadgeRule[] rules){
        if(rules==null||rules.length<2)return false;
        boolean changed=false;
        if("male".equals(rules[0].id)&&rules[0].color==0xFFFF5555){rules[0].color=0xFF5599FF;changed=true;}
        if("female".equals(rules[1].id)&&rules[1].color==0xFF5599FF){rules[1].color=0xFFFF5555;changed=true;}
        return changed;
    }
}
