package com.cobbletown.gamemenudashboard.claims;

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

/** Private Xaero 25.3.5 proof using its element-renderer handler; never creates waypoints. */
final class MinimapRectangleProof {
    private static final Logger LOGGER = LoggerFactory.getLogger("game-menu-dashboard/minimap-proof");
    private static Rect rectangle;
    private static boolean installed;
    private static long renderCalls;

    static void enable(int minX, int minZ, int maxX, int maxZ) {
        if (!FabricLoader.getInstance().isModLoaded("xaerominimap")) {
            LOGGER.warn("Minimap proof skipped: Xaero's Minimap is not loaded");
            return;
        }
        rectangle = new Rect(minX, minZ, maxX, maxZ);
        LOGGER.info("Minimap proof rectangle enabled at world X/Z {}", rectangle);
        if (installed) return;
        try {
            XaeroMinimap.instance.getMinimap().getOverMapRendererHandler().add(new RectangleRenderer());
            installed = true;
            LOGGER.info("Minimap proof renderer added to Xaero OVER_MINIMAP handler");
        } catch (RuntimeException exception) {
            LOGGER.error("Minimap proof install failed", exception);
        }
    }

    static void disable() { rectangle = null; LOGGER.info("Minimap proof disabled; no minimap data was modified"); }

    private record Rect(int minX, int minZ, int maxX, int maxZ) { }

    private static final class RectangleRenderer extends MinimapElementRenderer<Rect, Void> {
        RectangleRenderer() { super(new RectangleReader(), new RectangleProvider(), null); }

        @Override public boolean renderElement(Rect rect, boolean hovered, boolean highlighted, double optionalScale, float partialTicks, double x, double y,
                                               MinimapElementRenderInfo info, DrawContext context, VertexConsumerProvider.Immediate buffers) {
            if (rectangle == null) return false;
            renderCalls++;
            if (renderCalls == 1 || renderCalls % 600 == 0) LOGGER.info("Minimap proof renderer entered count={} location={} optionalScale={} mapDimension={}", renderCalls, info.location, optionalScale, info.mapDimension);
            // Xaero has translated the matrix to this element's world-space center.
            // Draw local half-extents, not absolute X/Z a second time. That lets the
            // handler carry pan/rotation/zoom. optionalScale is logged to evaluate the
            // initial inverse-scale thin-line hypothesis during the live review.
            int halfW = (rect.maxX - rect.minX) / 2;
            int halfH = (rect.maxZ - rect.minZ) / 2;
            int thin = Math.max(1, (int) Math.round(1.0 / Math.max(0.25, optionalScale)));
            context.fill(-halfW, -halfH, halfW, -halfH + thin, 0xFF101218);
            context.fill(-halfW, halfH - thin, halfW, halfH, 0xFF101218);
            context.fill(-halfW, -halfH, -halfW + thin, halfH, 0xFF101218);
            context.fill(halfW - thin, -halfH, halfW, halfH, 0xFF101218);
            context.fill(-halfW + thin, -halfH + thin, halfW - thin, -halfH + thin * 2, 0xFFF5F7FF);
            context.fill(-halfW + thin, halfH - thin * 2, halfW - thin, halfH - thin, 0xFFF5F7FF);
            context.fill(-halfW + thin, -halfH + thin, -halfW + thin * 2, halfH - thin, 0xFFF5F7FF);
            context.fill(halfW - thin * 2, -halfH + thin, halfW - thin, halfH - thin, 0xFFF5F7FF);
            if (renderCalls == 1) LOGGER.info("Minimap proof geometry submitted: local halfExtents={}x{}, thin={}, world rectangle={}", halfW, halfH, thin, rect);
            return true;
        }
        @Override public void preRender(MinimapElementRenderInfo info, VertexConsumerProvider.Immediate buffers, xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider multi) { }
        @Override public void postRender(MinimapElementRenderInfo info, VertexConsumerProvider.Immediate buffers, xaero.common.graphics.renderer.multitexture.MultiTextureRenderTypeRendererProvider multi) { }
        @Override public boolean shouldRender(MinimapElementRenderLocation location) { return location == MinimapElementRenderLocation.OVER_MINIMAP; }
    }

    private static final class RectangleProvider extends MinimapElementRenderProvider<Rect, Void> {
        private boolean yielded;
        @Override public void begin(MinimapElementRenderLocation location, Void context) { yielded = false; if (rectangle != null) LOGGER.info("Minimap proof provider begin at {}", location); }
        @Override public boolean hasNext(MinimapElementRenderLocation location, Void context) { return rectangle != null && !yielded; }
        @Override public Rect getNext(MinimapElementRenderLocation location, Void context) { yielded = true; LOGGER.info("Minimap proof provider emitted world rectangle {}", rectangle); return rectangle; }
        @Override public void end(MinimapElementRenderLocation location, Void context) { }
    }

    private static final class RectangleReader extends MinimapElementReader<Rect, Void> {
        @Override public boolean isHidden(Rect r, Void c) { return false; }
        @Override public double getRenderX(Rect r, Void c, float p) { return (r.minX + r.maxX) / 2.0; }
        @Override public double getRenderY(Rect r, Void c, float p) { return 0; }
        @Override public double getRenderZ(Rect r, Void c, float p) { return (r.minZ + r.maxZ) / 2.0; }
        @Override public int getInteractionBoxLeft(Rect r, Void c, float p) { return 0; }
        @Override public int getInteractionBoxRight(Rect r, Void c, float p) { return 0; }
        @Override public int getInteractionBoxTop(Rect r, Void c, float p) { return 0; }
        @Override public int getInteractionBoxBottom(Rect r, Void c, float p) { return 0; }
        @Override public int getRenderBoxLeft(Rect r, Void c, float p) { return 0; }
        @Override public int getRenderBoxRight(Rect r, Void c, float p) { return 0; }
        @Override public int getRenderBoxTop(Rect r, Void c, float p) { return 0; }
        @Override public int getRenderBoxBottom(Rect r, Void c, float p) { return 0; }
        @Override public int getLeftSideLength(Rect r, MinecraftClient mc) { return 0; }
        @Override public String getMenuName(Rect r) { return "Synthetic claim"; }
        @Override public String getFilterName(Rect r) { return "Synthetic claim"; }
        @Override public int getMenuTextFillLeftPadding(Rect r) { return 0; }
        @Override public int getRightClickTitleBackgroundColor(Rect r) { return 0; }
        @Override public boolean shouldScaleBoxWithOptionalScale() { return false; }
    }
}
