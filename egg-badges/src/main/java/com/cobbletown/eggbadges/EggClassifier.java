package com.cobbletown.eggbadges;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class EggClassifier {
    private static final String[] IV_NAMES={"HP","Attack","Defence","Sp.Attack","Sp.Defence","Speed"};
    private static final Identifier COBBLEMON_EGG=Identifier.of("cobblemon","pokemon_egg");
    private EggClassifier(){}
    public static EggAttributes read(ItemStack stack){
        return read(stack, BadgeRule.defaults()).attributes();
    }
    /** Bounded parse result used by the client-thread classifier and diagnostics. */
    public static ReadResult read(ItemStack stack,BadgeRule[] rules){
        if(!isEgg(stack))return new ReadResult(null,0,false);
        Needs needs=Needs.from(rules);
        String species="",form="",nature="",gender="";int[] ivs={-1,-1,-1,-1,-1,-1};
        var lore=stack.get(DataComponentTypes.LORE);
        int loreLines=0;
        if(lore!=null)for(Text line:lore.lines()){
            loreLines++;String s=stripFormatting(line.getString());int c=s.indexOf(':');if(c<=0)continue;
            String k=s.substring(0,c).trim(),v=s.substring(c+1).trim();
            if(needs.species&&"Species".equalsIgnoreCase(k))species=v;
            else if(needs.form&&"Form".equalsIgnoreCase(k))form=v;
            else if(needs.nature&&"Nature".equalsIgnoreCase(k))nature=v;
            else if(needs.gender&&("Gender".equalsIgnoreCase(k)||"Sex".equalsIgnoreCase(k)))gender=normalizeGender(v);
            else if(needs.ivs){int i=ivIndex(k);if(i>=0)ivs[i]=parse(v);}
        }
        return new ReadResult(new EggAttributes(species,form,nature,gender,ivs),loreLines,true);
    }
    static boolean isEgg(ItemStack stack){return stack!=null&&!stack.isEmpty()&&(COBBLEMON_EGG.equals(Registries.ITEM.getId(stack.getItem()))||"Pokémon Egg".equals(stack.getName().getString()));}
    static String normalizeGender(String value){
        if (value == null) return "";
        if ("M".equalsIgnoreCase(value) || "MALE".equalsIgnoreCase(value)) return "Male";
        if ("F".equalsIgnoreCase(value) || "FEMALE".equalsIgnoreCase(value)) return "Female";
        if ("GENDERLESS".equalsIgnoreCase(value)) return "Genderless";
        return value;
    }
    /** Minecraft formatting codes are two UTF-16 chars: section sign plus code. */
    static String stripFormatting(String s){int first=s.indexOf('\u00a7');if(first<0)return s.trim();StringBuilder out=new StringBuilder(s.length());out.append(s,0,first);for(int i=first;i<s.length();i++){if(s.charAt(i)=='\u00a7'&&i+1<s.length()){i++;continue;}out.append(s.charAt(i));}return out.toString().trim();}
    private static int ivIndex(String k){for(int i=0;i<IV_NAMES.length;i++)if(IV_NAMES[i].equalsIgnoreCase(k))return i;return -1;}
    private static int parse(String s){int value=0;boolean digit=false;for(int i=0;i<s.length();i++){char c=s.charAt(i);if(c>='0'&&c<='9'){value=value*10+(c-'0');digit=true;if(value>31)return -1;}else if(digit)break;}return digit?value:-1;}
    public static boolean matches(EggAttributes a,BadgeRule r){
        if(a==null||r==null||!r.enabled)return false;
        if(!eq(a.species(),r.species)||!eq(a.form(),r.form)||!eq(a.nature(),r.nature))return false;
        if(r.gender!=null&&!r.gender.isBlank()&&!"Any".equalsIgnoreCase(r.gender)&&!r.gender.equalsIgnoreCase(a.gender()))return false;
        if("male".equals(r.preset)&&!"male".equalsIgnoreCase(a.gender()))return false;
        if("female".equals(r.preset)&&!"female".equalsIgnoreCase(a.gender()))return false;
        if("perfect".equals(r.preset))for(int iv:a.ivs())if(iv!=31)return false;
        if("five".equals(r.preset)){int n=0;for(int iv:a.ivs())if(iv==31)n++;if(n!=5)return false;}
        if(r.values!=null)for(int i=0;i<Math.min(6,r.values.length);i++)if(r.values[i]!=null&&!r.values[i].isBlank()){
            int wanted;try{wanted=Integer.parseInt(r.values[i]);}catch(Exception e){return false;}int actual=a.ivs()[i];
            if(actual<0||!compare(actual,wanted,r.operators==null||i>=r.operators.length?"=":r.operators[i]))return false;
        }
        return true;
    }
    private static boolean eq(String a,String b){return b==null||b.isBlank()||(a!=null&&!a.isBlank()&&a.equalsIgnoreCase(b));}
    private static boolean compare(int a,int b,String op){return switch(op){case ">"->a>b;case "<"->a<b;case ">="->a>=b;case "<="->a<=b;default->a==b;};}
    public record ReadResult(EggAttributes attributes,int loreLines,boolean egg) {}
    private record Needs(boolean species,boolean form,boolean nature,boolean gender,boolean ivs){
        static Needs from(BadgeRule[] rules){boolean species=false,form=false,nature=false,gender=false,ivs=false;if(rules!=null)for(BadgeRule r:rules)if(r!=null&&r.enabled){species|=r.species!=null&&!r.species.isBlank();form|=r.form!=null&&!r.form.isBlank();nature|=r.nature!=null&&!r.nature.isBlank();gender|=(r.gender!=null&&!r.gender.isBlank()&&!"Any".equalsIgnoreCase(r.gender))||"male".equals(r.preset)||"female".equals(r.preset);ivs|="perfect".equals(r.preset)||"five".equals(r.preset)||(r.values!=null&&java.util.Arrays.stream(r.values).anyMatch(v->v!=null&&!v.isBlank()));}return new Needs(species,form,nature,gender,ivs);}
    }
}
