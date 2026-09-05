package com.cobbletown.eggbadges;

import java.util.Arrays;

public final class BadgeRule {
    public String id="", label="", glyph="!"; public int color=0xFFFFFFFF; public boolean enabled;
    public String species="", form="", nature="", gender="Any";
    public String[] operators={"=","=","=","=","=","="}; public String[] values={"","","","","",""};
    public String preset="custom";
    public BadgeRule() {}
    private BadgeRule(String id,String label,String glyph,int color,boolean enabled,String preset){this.id=id;this.label=label;this.glyph=glyph;this.color=color;this.enabled=enabled;this.preset=preset;}
    public static BadgeRule[] defaults(){return new BadgeRule[]{
        new BadgeRule("male","Male","M",0xFF5599FF,false,"male"),
        new BadgeRule("female","Female","F",0xFFFF5555,false,"female"),
        new BadgeRule("perfect","Perfect","●",0xFF55DD77,true,"perfect"),
        new BadgeRule("five","Five","V",0xFF55AAFF,false,"five"),
        new BadgeRule("conditional","Conditional","●",0xFF55DD77,false,"custom")};}
    public BadgeRule copy(){BadgeRule o=new BadgeRule();o.id=id;o.label=label;o.glyph=glyph;o.color=color;o.enabled=enabled;o.species=species;o.form=form;o.nature=nature;o.gender=gender;o.preset=preset;o.operators=operators==null?new String[6]:Arrays.copyOf(operators,6);o.values=values==null?new String[6]:Arrays.copyOf(values,6);return o;}
}
