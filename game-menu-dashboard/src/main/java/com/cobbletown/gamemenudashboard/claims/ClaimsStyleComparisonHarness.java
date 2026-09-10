package com.cobbletown.gamemenudashboard.claims;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Private player-level footprint renderer prototype using the proven buffered Xaero-safe world path. */
public final class ClaimsStyleComparisonHarness {
    private static final Logger LOGGER = LoggerFactory.getLogger("game-menu-dashboard/boundary-prototype");
    private static final int[] LINE_COLORS = {0xFFFFD33D, 0xFF2979FF, 0xFF69F0AE, 0xFFF5F7FF};
    private static final int RADIUS = 16;
    private boolean enabled;
    private BlockPos anchor;
    private long renderCallbacks;

    public void register() {
        LOGGER.info("Boundary prototype registering proven AFTER_ENTITIES buffered-line renderer");
        WorldRenderEvents.AFTER_ENTITIES.register(this::render);
        HudRenderCallback.EVENT.register((context, tickCounter) -> renderHud(context));
    }

    public void toggle() {
        enabled = !enabled;
        renderCallbacks = 0;
        if (!enabled) {
            MinimapRectangleProof.disable();
            LOGGER.info("Boundary prototype disabled; synthetic geometry cleared");
        } else {
            anchor = null;
            LOGGER.info("Boundary prototype enabled; awaiting player position");
        }
    }

    public boolean isEnabled() { return enabled; }

    public void tick(MinecraftClient client) {
        if (!enabled || anchor != null || client.player == null) return;
        anchor = client.player.getBlockPos();
        LOGGER.info("Boundary prototype synthetic claim created at X/Z {}..{} / {}..{}; primary line follows player Y", anchor.getX() - RADIUS, anchor.getX() + RADIUS, anchor.getZ() - RADIUS, anchor.getZ() + RADIUS);
        MinimapRectangleProof.enable(anchor.getX() - RADIUS, anchor.getZ() - RADIUS, anchor.getX() + RADIUS, anchor.getZ() + RADIUS);
    }

    private void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!enabled || anchor == null || client.player == null || context.matrixStack() == null || context.consumers() == null) return;
        renderCallbacks++;
        if (renderCallbacks == 1 || renderCallbacks % 600 == 0) LOGGER.info("Boundary prototype AFTER_ENTITIES callbacks={}; playerY={}", renderCallbacks, client.player.getY());
        double y = client.player.getY() + 0.18; // player-level footprint, never terrain-surface forced
        Vec3dCamera camera = Vec3dCamera.of(context);
        MatrixStack matrices = context.matrixStack();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        renderFootprint(matrices, lines, anchor.getX() - RADIUS, anchor.getZ() - RADIUS, anchor.getX() + RADIUS, anchor.getZ() + RADIUS, y);
        matrices.pop();
    }

    private static void renderFootprint(MatrixStack matrices, VertexConsumer lines, int minX, int minZ, int maxX, int maxZ, double y) {
        // A compact layered perimeter: tiny vertical offsets prevent z-fighting while
        // retaining one coherent outline instead of four visually separate fences.
        for (int index = 0; index < LINE_COLORS.length; index++) {
            double lineY = y + (index - 1.5) * .035;
            int color = LINE_COLORS[index];
            line(matrices, lines, minX, lineY, minZ, maxX, lineY, minZ, color);
            line(matrices, lines, maxX, lineY, minZ, maxX, lineY, maxZ, color);
            line(matrices, lines, maxX, lineY, maxZ, minX, lineY, maxZ, color);
            line(matrices, lines, minX, lineY, maxZ, minX, lineY, minZ, color);
        }
        for (int x = minX; x <= maxX; x += 4) { pylon(matrices, lines, x, y, minZ); pylon(matrices, lines, x, y, maxZ); }
        for (int z = minZ + 4; z < maxZ; z += 4) { pylon(matrices, lines, minX, y, z); pylon(matrices, lines, maxX, y, z); }
    }

    private static void pylon(MatrixStack matrices, VertexConsumer lines, double x, double y, double z) {
        // Strong nested boxes make both corners and regular four-block pylons visible
        // against a wide variety of terrain without making a full-height wall.
        marker(matrices, lines, x, y, z, 0xFF101218, 3.8, .52);
        marker(matrices, lines, x, y, z, 0xFFF5F7FF, 3.7, .34);
        marker(matrices, lines, x, y, z, 0xFFFFD33D, 3.6, .18);
    }

    private static void marker(MatrixStack matrices, VertexConsumer lines, double x, double y, double z, int color, double height, double radius) {
        WorldRenderer.drawBox(matrices, lines, x - radius, y - 1.0, z - radius, x + radius, y + height, z + radius, red(color), green(color), blue(color), 1.0F);
    }
    private static void line(MatrixStack matrices, VertexConsumer lines, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        var matrix = matrices.peek().getPositionMatrix();
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        lines.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red(color), green(color), blue(color), 1.0F).normal(dx, dy, dz);
        lines.vertex(matrix, (float) x2, (float) y2, (float) z2).color(red(color), green(color), blue(color), 1.0F).normal(dx, dy, dz);
    }

    private void renderHud(DrawContext context) {
        if (!enabled || anchor == null) return;
        context.fill(6, 6, 296, 44, 0xC0101218);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "SYNTHETIC PLAYER-LEVEL CLAIM BOUNDARY ON", 10, 10, 0xFFFFD05B);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Layered line · 4-block pylons · corners guaranteed", 10, 24, 0xFFFFFFFF);
    }

    private static float red(int color) { return ((color >> 16) & 0xFF) / 255F; }
    private static float green(int color) { return ((color >> 8) & 0xFF) / 255F; }
    private static float blue(int color) { return (color & 0xFF) / 255F; }
    private record Vec3dCamera(double x, double y, double z) { static Vec3dCamera of(WorldRenderContext context) { var pos = context.camera().getPos(); return new Vec3dCamera(pos.x, pos.y, pos.z); } }
}
