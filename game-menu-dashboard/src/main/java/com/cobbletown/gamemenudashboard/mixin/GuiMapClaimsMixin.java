package com.cobbletown.gamemenudashboard.mixin;

import com.cobbletown.gamemenudashboard.claims.WorldMapClaimsOverlay;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional local overlay for the exact inspected Xaero World Map 1.40.6 render method. */
@Pseudo
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
abstract class GuiMapClaimsMixin {
    @Inject(method = "method_25394(Lnet/minecraft/class_332;IIF)V", at = @At("TAIL"), remap = false, require = 0)
    private void gameMenuDashboard$renderClaims(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo callback) {
        GuiMapAccessor map = (GuiMapAccessor) this;
        WorldMapClaimsOverlay.render(context, map.gameMenuDashboard$getCameraX(), map.gameMenuDashboard$getCameraZ(), map.gameMenuDashboard$getScale());
    }
}
