package com.cobbletown.gamemenudashboard.claims;

import com.cobbletown.gamemenudashboard.GameMenuDashboardClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * World Map claim overlay reconstructed at GuiMap's render tail. Xaero's canvas pose
 * has already been popped there, so world coordinates are converted exactly once from
 * the live GuiMap camera and framebuffer scale into DrawContext GUI coordinates.
 */
public final class WorldMapClaimsOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger("game-menu-dashboard/world-map-claims");
    private static long renderCalls;
    private WorldMapClaimsOverlay() { }

    public static void render(DrawContext context, double cameraX, double cameraZ, double framebufferScale) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;
        String world = client.world.getRegistryKey().getValue().toString();
        // GuiMap's scale is measured in framebuffer pixels. DrawContext is scaled-GUI
        // space, so divide once by the live window GUI scale; this is the missing unit
        // conversion behind the old pan-at-double-rate result.
        double guiScale = Math.max(1.0, client.getWindow().getScaleFactor());
        double scale = framebufferScale / guiScale;
        int centerX = context.getScaledWindowWidth() / 2, centerY = context.getScaledWindowHeight() / 2;
        int count = 0;
        for (ClaimEntry entry : GameMenuDashboardClient.CLAIMS.claims()) {
            ClaimGeometry geometry = entry.geometry();
            if (!entry.displayEnabled() || geometry == null || !ClaimGeometry.sameWorld(geometry.world(), world)) continue;
            drawRectangle(context, geometry, entry.geometryStale(), cameraX, cameraZ, scale, centerX, centerY);
            count++;
        }
        renderCalls++;
        if (renderCalls == 1 || renderCalls % 600 == 0) LOGGER.info("Xaero World Map Claims tail hook={}, rectangles={}, framebuffer-to-GUI conversion active", renderCalls, count);
    }
    private static void drawRectangle(DrawContext context, ClaimGeometry geometry, boolean stale, double cameraX, double cameraZ, double scale, int centerX, int centerY) {
        int minX = Math.min(geometry.lesserX(), geometry.greaterX()), maxX = Math.max(geometry.lesserX(), geometry.greaterX()) + 1;
        int minZ = Math.min(geometry.lesserZ(), geometry.greaterZ()), maxZ = Math.max(geometry.lesserZ(), geometry.greaterZ()) + 1;
        int x1 = (int) Math.round(centerX + (minX - cameraX) * scale);
        int x2 = (int) Math.round(centerX + (maxX - cameraX) * scale);
        int y1 = (int) Math.round(centerY + (minZ - cameraZ) * scale);
        int y2 = (int) Math.round(centerY + (maxZ - cameraZ) * scale);
        int outer = stale ? 0xFF5E3415 : 0xFF101218;
        int inner = stale ? 0xFFFF9D3D : 0xFFF5F7FF;
        stroke(context, x1, y1, x2, y2, outer, 3); stroke(context, x1, y1, x2, y2, inner, 1);
    }
    private static void stroke(DrawContext context, int x1, int y1, int x2, int y2, int color, int thickness) {
        context.fill(x1, y1, x2 + 1, y1 + thickness, color); context.fill(x1, y2 - thickness + 1, x2 + 1, y2 + 1, color);
        context.fill(x1, y1, x1 + thickness, y2 + 1, color); context.fill(x2 - thickness + 1, y1, x2 + 1, y2 + 1, color);
    }
}
