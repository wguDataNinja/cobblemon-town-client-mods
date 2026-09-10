package com.cobbletown.gamemenudashboard.claims;

import com.cobbletown.gamemenudashboard.GameMenuDashboardClient;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

/** Read-only rendering of deliberately loaded own-claim Details geometry. */
public final class ClaimsWorldRenderer {
    private static final int AUTHORITATIVE = 0xFF8FC7DA;
    private static final int AUTHORITATIVE_STALE = 0xFFC08C58;
    private static final int PROPOSED = 0xFFFFA33D;

    public void register() { WorldRenderEvents.AFTER_ENTITIES.register(this::render); }

    private void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!GameMenuDashboardClient.CLAIMS_DISPLAY.boundariesVisible() || client.player == null || client.world == null
            || context.matrixStack() == null || context.consumers() == null) return;
        String world = client.world.getRegistryKey().getValue().toString();
        double y = client.player.getY() + .18;
        var camera = context.camera().getPos();
        MatrixStack matrices = context.matrixStack();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        ClaimEntry selected = GameMenuDashboardClient.CLAIMS.selected();
        ClaimGeometry geometry = selected == null ? null : selected.geometry();
        if (geometry != null && ClaimGeometry.sameWorld(geometry.world(), world)) {
            renderClaim(matrices, lines, geometry, y, selected.geometryStale() ? AUTHORITATIVE_STALE : AUTHORITATIVE, 3.4, .28);
            ClaimGeometry proposal = ResizePreview.geometryFor(selected.key());
            if (proposal != null) renderClaim(matrices, lines, proposal, y + .10, PROPOSED, 3.8, .34);
        }
        matrices.pop();
    }

    private static void renderClaim(MatrixStack matrices, VertexConsumer lines, ClaimGeometry geometry, double y, int color, double postHeight, double postRadius) {
        int minX = Math.min(geometry.lesserX(), geometry.greaterX()), maxX = Math.max(geometry.lesserX(), geometry.greaterX());
        int minZ = Math.min(geometry.lesserZ(), geometry.greaterZ()), maxZ = Math.max(geometry.lesserZ(), geometry.greaterZ());
        line(matrices, lines, minX, y, minZ, maxX, y, minZ, color);
        line(matrices, lines, maxX, y, minZ, maxX, y, maxZ, color);
        line(matrices, lines, maxX, y, maxZ, minX, y, maxZ, color);
        line(matrices, lines, minX, y, maxZ, minX, y, minZ, color);
        pylon(matrices, lines, minX, y, minZ, color, postHeight, postRadius);
        pylon(matrices, lines, maxX, y, minZ, color, postHeight, postRadius);
        pylon(matrices, lines, minX, y, maxZ, color, postHeight, postRadius);
        pylon(matrices, lines, maxX, y, maxZ, color, postHeight, postRadius);
    }
    private static void pylon(MatrixStack matrices, VertexConsumer lines, double x, double y, double z, int color, double height, double radius) {
        marker(matrices, lines, x, y, z, 0xFF182025, height + .1, radius + .12);
        marker(matrices, lines, x, y, z, color, height, radius);
    }
    private static void marker(MatrixStack matrices, VertexConsumer lines, double x, double y, double z, int color, double height, double radius) {
        // A bounded player-relative post: one block below and several above, never an infinite wall.
        WorldRenderer.drawBox(matrices, lines, x - radius, y - 1.0, z - radius, x + radius, y + height, z + radius, red(color), green(color), blue(color), 1.0F);
    }
    private static void line(MatrixStack matrices, VertexConsumer lines, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        var matrix = matrices.peek().getPositionMatrix();
        float dx = (float) (x2 - x1), dy = (float) (y2 - y1), dz = (float) (z2 - z1);
        lines.vertex(matrix, (float) x1, (float) y1, (float) z1).color(red(color), green(color), blue(color), 1F).normal(dx, dy, dz);
        lines.vertex(matrix, (float) x2, (float) y2, (float) z2).color(red(color), green(color), blue(color), 1F).normal(dx, dy, dz);
    }
    private static float red(int color) { return ((color >> 16) & 0xFF) / 255F; }
    private static float green(int color) { return ((color >> 8) & 0xFF) / 255F; }
    private static float blue(int color) { return (color & 0xFF) / 255F; }
}
