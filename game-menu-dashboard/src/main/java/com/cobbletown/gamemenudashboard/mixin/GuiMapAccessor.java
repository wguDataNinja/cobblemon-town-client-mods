package com.cobbletown.gamemenudashboard.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Optional, version-specific access for Xaero World Map 1.40.6's render camera. */
@Pseudo
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public interface GuiMapAccessor {
    @Accessor("cameraX") double gameMenuDashboard$getCameraX();
    @Accessor("cameraZ") double gameMenuDashboard$getCameraZ();
    @Accessor("scale") double gameMenuDashboard$getScale();
}
