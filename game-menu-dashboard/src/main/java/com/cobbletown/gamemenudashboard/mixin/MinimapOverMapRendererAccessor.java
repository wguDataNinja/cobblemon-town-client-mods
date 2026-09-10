package com.cobbletown.gamemenudashboard.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Xaero 25.3.5 live minimap transform state; optional and read-only. */
@Pseudo
@Mixin(targets = "xaero.hud.minimap.element.render.over.MinimapElementOverMapRendererHandler", remap = false)
public interface MinimapOverMapRendererAccessor {
    @Accessor("ps") double gameMenuDashboard$getPs();
    @Accessor("pc") double gameMenuDashboard$getPc();
    @Accessor("zoom") double gameMenuDashboard$getZoom();
}
