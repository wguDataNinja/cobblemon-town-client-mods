package com.cobbletown.pcexporter.mixin;

import com.cobblemon.mod.common.client.gui.pc.PCGUI;
import com.cobblemon.pc_plus.client.filter.FilterController;
import com.cobbletown.pcexporter.PcExporterMod;
import com.cobbletown.pcexporter.bridge.PcPlusFilterRegistry;
import com.cobbletown.pcexporter.ui.ExportPcOverlay;
import net.minecraft.client.gui.DrawContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Read-only interop with PC Plus: captures the {@link FilterController} per PC screen, draws the
 * Export control inside PC Plus's overlay layer, and consumes clicks on that control before PC
 * Plus starts a panel drag. PC Plus is never modified; marked {@code @Pseudo} + {@code require = 0}
 * so an absent or changed PC Plus never breaks loading.
 */
@Pseudo
@Mixin(value = FilterController.class, remap = false)
public abstract class FilterControllerMixin {

    @Inject(method = "<init>(Lcom/cobblemon/mod/common/client/gui/pc/PCGUI;)V", at = @At("RETURN"), require = 0, remap = false)
    private void pcexporter$captureController(PCGUI pcGui, CallbackInfo ci) {
        try {
            PcPlusFilterRegistry.register(pcGui, (FilterController) (Object) this);
        } catch (Throwable ignored) {
            // Fail closed: no Export control is shown when PC Plus's controller cannot be read.
        }
    }

    /**
     * Draw pass: PC Plus's own titlebar-overlay renderer, just before it pops the +350 matrix. This
     * runs after the native controls are drawn, in PC Plus's internal coordinates, so the control
     * sits in the same layer as them.
     */
    @Inject(
            method = "renderOnTop",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/class_4587;method_22909()V",
                    shift = At.Shift.BEFORE
            ),
            require = 1,
            remap = false
    )
    private void pcexporter$drawOverlay(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        FilterController self = (FilterController) (Object) this;
        PCGUI pcGui = PcPlusFilterRegistry.get(self);
        if (pcGui != null) {
            ExportPcOverlay.render(self, pcGui, context, mouseX, mouseY, delta);
        }
    }

    /**
     * Input pass: PC Plus treats any in-panel click that is not over one of its own controls as a
     * drag handle, so a click on our control reaches {@code beginTerminalDrag} before
     * {@code terminalMouseClicked}. Consume it here — same internal coordinate space as the draw —
     * so the export fires and no drag begins.
     */
    @Inject(method = "beginTerminalDrag", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void pcexporter$consumeExportClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        FilterController self = (FilterController) (Object) this;
        PCGUI pcGui = PcPlusFilterRegistry.get(self);
        if (pcGui != null && ExportPcOverlay.click(self, pcGui, mouseX, mouseY, button)) {
            PcExporterMod.LOGGER.info("PC Exporter: Export control click consumed before panel drag");
            cir.setReturnValue(false);
        }
    }
}
