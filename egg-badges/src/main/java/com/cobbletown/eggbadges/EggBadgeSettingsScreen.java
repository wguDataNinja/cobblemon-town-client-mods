package com.cobbletown.eggbadges;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

/** Responsive editor: selector and bottom navigation stay fixed while the
 * editor uses available width for two-column attributes and IV rows. */
public final class EggBadgeSettingsScreen extends Screen {
    private final Screen parent; private final BadgeRule[] rules=new BadgeRule[5]; private int selected;
    private final CheckboxWidget[] rowChecks=new CheckboxWidget[5]; private final ButtonWidget[] rowButtons=new ButtonWidget[5]; private CheckboxWidget enabled;
    private TextFieldWidget species,form,nature; private final TextFieldWidget[] iv=new TextFieldWidget[6];
    private final ButtonWidget[] operators=new ButtonWidget[6]; private ButtonWidget gender,glyph,color; private EggBadgeLayout layout;
    private static final String[] STATS={"HP","Attack","Defence","Sp.Atk","Sp.Def","Speed"};
    public EggBadgeSettingsScreen(Screen parent){super(Text.literal("Egg Badges"));this.parent=parent;for(int i=0;i<5;i++)rules[i]=EggBadgesConfig.INSTANCE.badges[i].copy();}
    @Override protected void init(){
        layout=EggBadgeLayout.calculate(width,height);var tr=MinecraftClient.getInstance().textRenderer;
        for(int i=0;i<5;i++){final int n=i;int y=layout.selectorTop()+i*layout.selectorRowHeight();rowButtons[i]=ButtonWidget.builder(Text.literal(label(n)),b->select(n)).dimensions(layout.selectorX(),y,layout.selectorWidth(),layout.selectorRowHeight()-2).build();addDrawableChild(rowButtons[i]);rowChecks[i]=CheckboxWidget.builder(Text.empty(),tr).pos(layout.selectorX()+3,y+2).checked(rules[i].enabled).callback((c,v)->rules[n].enabled=v).build();addDrawableChild(rowChecks[i]);}
        int x=layout.editorX(),w=layout.editorWidth();int third=Math.max(70,(w-16)/3);enabled=CheckboxWidget.builder(Text.literal("Enabled"),tr).pos(x,layout.headerY()).checked(rules[selected].enabled).callback((c,v)->rules[selected].enabled=v).build();addDrawableChild(enabled);
        glyph=ButtonWidget.builder(Text.literal("Glyph"),b->cycleGlyph()).dimensions(x+third+8,layout.headerY(),third,20).build();color=ButtonWidget.builder(Text.literal("Color"),b->cycleColor()).dimensions(x+2*(third+8),layout.headerY(),third,20).build();addDrawableChild(glyph);addDrawableChild(color);
        int half=Math.max(120,(w-12)/2);species=field(tr,x,layout.attributeY(),half,"Species");form=field(tr,x+half+12,layout.attributeY(),half,"Form");nature=field(tr,x,layout.attributeY()+layout.rowGap(),half,"Nature");addDrawableChild(species);addDrawableChild(form);addDrawableChild(nature);gender=ButtonWidget.builder(Text.literal("Gender"),b->cycleGender()).dimensions(x+half+12,layout.attributeY()+layout.rowGap(),half,20).build();addDrawableChild(gender);
        int colW=Math.max(130,(w-12)/2);for(int i=0;i<6;i++){final int n=i;int col=i/3,row=i%3,bx=x+col*(colW+12),y=layout.ivY()+row*layout.ivRowGap();operators[i]=ButtonWidget.builder(Text.literal(rules[selected].operators[i]),b->cycleOperator(n)).dimensions(bx+58,y,30,20).build();addDrawableChild(operators[i]);iv[i]=field(tr,bx+94,y,Math.max(38,colW-96),"IV");iv[i].setMaxLength(2);addDrawableChild(iv[i]);}
        int py=layout.presetY(),bw=Math.max(70,(w-24)/4);String[] names={"Perfect / 6×31","Exactly 5×31","One stat 31","Clear"},ids={"perfect","five","one","custom"};for(int i=0;i<4;i++){final String id=ids[i];addDrawableChild(ButtonWidget.builder(Text.literal(names[i]),b->preset(id)).dimensions(x+i*(bw+8),py,bw,20).build());}
        int sy=layout.secondaryY();addDrawableChild(ButtonWidget.builder(Text.literal("Reset badge"),b->resetBadge()).dimensions(x,sy,Math.max(100,(w-8)/2),20).build());addDrawableChild(ButtonWidget.builder(Text.literal("Reset all"),b->{EggBadgesConfig.INSTANCE.reset();client.setScreen(parent);}).dimensions(x+(w+8)/2,sy,Math.max(100,(w-8)/2),20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Cancel"),b->client.setScreen(parent)).dimensions(layout.leftBottomX(),layout.bottomY(),80,20).build());addDrawableChild(ButtonWidget.builder(Text.literal("Done"),b->{save();client.setScreen(parent);}).dimensions(layout.rightBottomX()-80,layout.bottomY(),80,20).build());select(0);
    }
    private TextFieldWidget field(net.minecraft.client.font.TextRenderer tr,int x,int y,int w,String hint){return new TextFieldWidget(tr,x,y,w,20,Text.literal(hint));}
    private String label(int i){return (i==selected?"▶ ":"")+rules[i].glyph+"  "+rules[i].label;}
    private void select(int n){saveSelected();selected=n;var r=rules[n];if(species==null)return;for(int i=0;i<5;i++)rowButtons[i].setMessage(Text.literal(label(i)));enabled.visible=false;enabled=CheckboxWidget.builder(Text.literal("Enabled"),MinecraftClient.getInstance().textRenderer).pos(layout.editorX(),layout.headerY()).checked(r.enabled).callback((c,v)->rules[selected].enabled=v).build();addDrawableChild(enabled);species.setText(empty(r.species));form.setText(empty(r.form));nature.setText(empty(r.nature));for(int i=0;i<6;i++){iv[i].setText(r.values!=null?empty(r.values[i]):"");operators[i].setMessage(Text.literal(r.operators[i]));}gender.setMessage(Text.literal("Gender: "+empty(r.gender,"Any")));glyph.setMessage(Text.literal("Glyph: "+r.glyph));color.setMessage(Text.literal("Color"));}
    private void saveSelected(){if(species==null)return;var r=rules[selected];r.species=species.getText();r.form=form.getText();r.nature=nature.getText();for(int i=0;i<6;i++)r.values[i]=iv[i].getText();}
    private void save(){saveSelected();EggBadgesConfig.INSTANCE.badges=rules;EggBadgesConfig.INSTANCE.save();}
    private void resetBadge(){rules[selected]=BadgeRule.defaults()[selected];select(selected);}
    private void cycleGender(){String g=rules[selected].gender;rules[selected].gender="Any".equalsIgnoreCase(g)?"Male":"Male".equalsIgnoreCase(g)?"Female":"Any";gender.setMessage(Text.literal("Gender: "+rules[selected].gender));}
    private void cycleOperator(int i){String[] ops={"=",">","<",">=","<="};int n=0;for(int j=0;j<ops.length;j++)if(ops[j].equals(rules[selected].operators[i]))n=j;rules[selected].operators[i]=ops[(n+1)%ops.length];operators[i].setMessage(Text.literal(rules[selected].operators[i]));}
    private void cycleGlyph(){String[] g={"VI","V","M","F","!","●","▲"};int n=0;for(int i=0;i<g.length;i++)if(g[i].equals(rules[selected].glyph))n=i;rules[selected].glyph=g[(n+1)%g.length];glyph.setMessage(Text.literal("Glyph: "+rules[selected].glyph));}
    private void cycleColor(){int[] c={0xFFFFD54A,0xFFE0E0E0,0xFFFF5555,0xFF5599FF,0xFF55DD77,0xFFFFAA55};int n=0;for(int i=0;i<c.length;i++)if(c[i]==rules[selected].color)n=i;rules[selected].color=c[(n+1)%c.length];}
    private void preset(String p){saveSelected();var r=rules[selected];r.preset=p;java.util.Arrays.fill(r.values,"");if("perfect".equals(p))java.util.Arrays.fill(r.values,"31");if("five".equals(p))for(int i=0;i<5;i++)r.values[i]="31";if("one".equals(p))r.values[0]="31";select(selected);}
    private static String empty(String s){return s==null?"":s;}private static String empty(String s,String d){return s==null||s.isBlank()?d:s;}
    @Override public void render(DrawContext c,int mx,int my,float d){renderBackground(c,mx,my,d);c.drawCenteredTextWithShadow(textRenderer,title,width/2,layout.titleY(),0xFFFFFFFF);c.drawTextWithShadow(textRenderer,"Select a badge and edit its matching fields.",layout.editorX(),layout.subtitleY(),0xFFB8C6D8);c.drawTextWithShadow(textRenderer,"Attributes",layout.editorX(),layout.attributeLabelY(),0xFFE0E0E0);c.drawTextWithShadow(textRenderer,"IV requirements",layout.editorX(),layout.ivLabelY(),0xFFE0E0E0);int colW=Math.max(130,(layout.editorWidth()-12)/2);for(int i=0;i<6;i++){int col=i/3,row=i%3;c.drawTextWithShadow(textRenderer,STATS[i],layout.editorX()+col*(colW+12),layout.ivY()+row*layout.ivRowGap()+6,0xFFE0E0E0);}super.render(c,mx,my,d);}
    @Override public boolean shouldPause(){return false;}
}
