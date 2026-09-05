package com.cobbletown.eggbadges;

/** Pure scaled-GUI layout calculations, kept testable without Minecraft. */
public record EggBadgeLayout(int width,int height,int contentLeft,int contentRight,int selectorWidth,int editorX,int headerY,int attributeY,int ivY,int presetY,int secondaryY,int bottomY,int rowGap,int ivRowGap){
    public static EggBadgeLayout calculate(int width,int height){int content=Math.min(Math.max(width-32,420),1100);int left=Math.max(16,(width-content)/2);int selector=width>=900?240:width>=650?200:Math.max(140,content/3);int editor=left+selector+16;int top=height<600?34:48;int iv=top+96;int preset=iv+3*(height<600?21:28)+22;int secondary=preset+26;return new EggBadgeLayout(width,height,left,left+content,selector,editor,top,top+30,iv,preset,secondary,height-30,height<600?20:28,height<600?21:28);}
    public int selectorX(){return contentLeft;}public int selectorTop(){return headerY;}public int selectorRowHeight(){return height<600?24:28;}public int editorWidth(){return contentRight-editorX;}public int titleY(){return 12;}public int subtitleY(){return headerY-15;}public int attributeLabelY(){return attributeY-13;}public int ivLabelY(){return ivY-15;}public int leftBottomX(){return contentLeft;}public int rightBottomX(){return contentRight;}
}
