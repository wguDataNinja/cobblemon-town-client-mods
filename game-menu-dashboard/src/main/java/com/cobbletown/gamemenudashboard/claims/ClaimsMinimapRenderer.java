package com.cobbletown.gamemenudashboard.claims;

import com.cobbletown.gamemenudashboard.GameMenuDashboardClient;
import com.cobbletown.gamemenudashboard.mixin.MinimapOverMapRendererAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xaero.hud.minimap.element.render.MinimapElementReader;
import xaero.hud.minimap.element.render.MinimapElementRenderInfo;
import xaero.hud.minimap.element.render.MinimapElementRenderLocation;
import xaero.hud.minimap.element.render.MinimapElementRenderProvider;
import xaero.hud.minimap.element.render.MinimapElementRenderer;
import xaero.minimap.XaeroMinimap;
import net.minecraft.util.math.RotationAxis;

import java.util.List;

/**
 * Xaero 25.3.5 OVER_MINIMAP extension point for deliberately MAP-enabled own Claims.
 * This intentionally does not write waypoints or map data. GuiMap/world-map rendering
 * has no proven safe hook yet, so this class does not pretend to support it.
 */
public final class ClaimsMinimapRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger("game-menu-dashboard/claims-minimap");
    private static boolean installed;

    public void tick() {
        if (installed || !FabricLoader.getInstance().isModLoaded("xaerominimap")) return;
        try {
            Object handler = XaeroMinimap.instance.getMinimap().getOverMapRendererHandler();
            XaeroMinimap.instance.getMinimap().getOverMapRendererHandler().add(new Renderer((MinimapOverMapRendererAccessor) handler));
            installed = true;
            LOGGER.info("Installed Xaero OVER_MINIMAP owned-claims renderer (no waypoints or map-data writes)");
        } catch (RuntimeException exception) {
            LOGGER.error("Could not install Xaero owned-claims renderer", exception);
        }
    }

    private record Rect(ClaimGeometry geometry, boolean stale) { }
    private static List<Rect> visibleClaims() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return List.of();
        String world = client.world.getRegistryKey().getValue().toString();
        return GameMenuDashboardClient.CLAIMS.claims().stream()
            .filter(entry -> entry.displayEnabled() && entry.geometry() != null && ClaimGeometry.sameWorld(entry.geometry().world(), world))
            .map(entry -> new Rect(entry.geometry(), entry.geometryStale())).toList();
    }
    private static final class Renderer extends MinimapElementRenderer<Rect, Void> {
        private final MinimapOverMapRendererAccessor transform;
        private long renderCalls;
        Renderer(MinimapOverMapRendererAccessor transform) { super(new Reader(), new Provider(), null); this.transform = transform; }
        @Override public boolean renderElement(Rect rect, boolean hovered, boolean highlighted, double optionalScale, float partialTicks, double x, double y,
                                               MinimapElementRenderInfo info, DrawContext context, VertexConsumerProvider.Immediate buffers) {
            ClaimGeometry geometry = rect.geometry();
            int width = Math.max(1, Math.abs(geometry.greaterX() - geometry.lesserX()) + 1);
            int depth = Math.max(1, Math.abs(geometry.greaterZ() - geometry.lesserZ()) + 1);
            int thin = Math.max(1, (int) Math.round(1.0 / Math.max(.25, optionalScale)));
            int outer = rect.stale() ? 0xFF5E3415 : 0xFF101218;
            int inner = rect.stale() ? 0xFFFF9D3D : 0xFFF5F7FF;
            // Xaero has already placed this element at its world-footprint center.
            // Apply the same live rotation + zoom to local X/Z extents: raw blocks
            // must never be submitted as unscaled GUI pixels.
            double ps = transform.gameMenuDashboard$getPs(), pc = transform.gameMenuDashboard$getPc(), zoom = transform.gameMenuDashboard$getZoom();
            context.getMatrices().push();
            context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.atan2(pc, ps)));
            context.getMatrices().scale((float) zoom, (float) -zoom, 1.0F);
            int x1 = -width / 2, z1 = -depth / 2, x2 = x1 + width, z2 = z1 + depth;
            stroke(context, x1, z1, x2, z2, outer, thin + 1);
            stroke(context, x1, z1, x2, z2, inner, thin);
            context.getMatrices().pop();
            renderCalls++;
            if (renderCalls == 1 || renderCalls % 600 == 0) LOGGER.info("Xaero minimap rectangle submitted through live center/rotation/zoom transform; calls={}, enabled-claim geometry={}", renderCalls, width + "x" + depth);
            return true;
        }
        private static void stroke(DrawContext context, int x1, int y1, int x2, int y2, int color, int thickness) {
            context.fill(x1, y1, x2, y1 + thickness, color); context.fill(x1, y2 - thickness, x2, y2, color);
            context.fill(x1, y1, x1 + thickness, y2, color); context.fill(x2 - thickness, y1, x2, y2, color);
        }
        @Override public void preRender(MinimapElementRenderInfo info, VertexConsumerProvider.Immediate buffers, xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider multi) { }
        @Override public void postRender(MinimapElementRenderInfo info, VertexConsumerProvider.Immediate buffers, xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider multi) { }
        @Override public boolean shouldRender(MinimapElementRenderLocation location) { return location == MinimapElementRenderLocation.OVER_MINIMAP; }
    }
    private static final class Provider extends MinimapElementRenderProvider<Rect, Void> {
        private List<Rect> rectangles = List.of(); private int index;
        @Override public void begin(MinimapElementRenderLocation location, Void context) { rectangles = visibleClaims(); index = 0; }
        @Override public boolean hasNext(MinimapElementRenderLocation location, Void context) { return index < rectangles.size(); }
        @Override public Rect getNext(MinimapElementRenderLocation location, Void context) { return rectangles.get(index++); }
        @Override public void end(MinimapElementRenderLocation location, Void context) { }
    }
    private static final class Reader extends MinimapElementReader<Rect, Void> {
        @Override public boolean isHidden(Rect rect, Void context) { return false; }
        @Override public double getRenderX(Rect rect, Void context, float partialTicks) { return (Math.min(rect.geometry().lesserX(), rect.geometry().greaterX()) + Math.max(rect.geometry().lesserX(), rect.geometry().greaterX()) + 1) / 2.0; }
        @Override public double getRenderY(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public double getRenderZ(Rect rect, Void context, float partialTicks) { return (Math.min(rect.geometry().lesserZ(), rect.geometry().greaterZ()) + Math.max(rect.geometry().lesserZ(), rect.geometry().greaterZ()) + 1) / 2.0; }
        @Override public int getInteractionBoxLeft(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getInteractionBoxRight(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getInteractionBoxTop(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getInteractionBoxBottom(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getRenderBoxLeft(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getRenderBoxRight(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getRenderBoxTop(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getRenderBoxBottom(Rect rect, Void context, float partialTicks) { return 0; }
        @Override public int getLeftSideLength(Rect rect, MinecraftClient client) { return 0; }
        @Override public String getMenuName(Rect rect) { return "Own Claim"; }
        @Override public String getFilterName(Rect rect) { return "Own Claim"; }
        @Override public int getMenuTextFillLeftPadding(Rect rect) { return 0; }
        @Override public int getRightClickTitleBackgroundColor(Rect rect) { return 0; }
        @Override public boolean shouldScaleBoxWithOptionalScale() { return false; }
    }
}
