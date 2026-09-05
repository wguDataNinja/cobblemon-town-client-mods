package com.cobbletown.eggbadges;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class EggBadgesClient implements ClientModInitializer {
    @Override public void onInitializeClient(){
        EggBadgesConfig.INSTANCE.load();
        ClientTickEvents.END_CLIENT_TICK.register(EggBadgeRenderer::tick);
        ScreenEvents.AFTER_INIT.register((client,screen,width,height)->{
            if(screen instanceof OptionsScreen){ButtonWidget done=null;for(var c:Screens.getButtons(screen))if(c instanceof ButtonWidget b&&"Done".equals(b.getMessage().getString())){done=b;break;}
                int w=done==null?200:done.getWidth(),x=done==null?width/2-w/2:done.getX(),y=done==null?height-58:done.getY()-24;
                Screens.getButtons(screen).add(ButtonWidget.builder(Text.literal("★ Egg Badges…"),b->client.setScreen(new EggBadgeSettingsScreen(screen))).dimensions(x,y,w,20).build());}
            ScreenEvents.afterRender(screen).register((current,context,mouseX,mouseY,delta)->EggBadgeRenderer.render(current,context,mouseX,mouseY));
        });
    }
}
