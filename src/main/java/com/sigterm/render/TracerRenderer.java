package com.sigterm.render;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.render.Tracers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class TracerRenderer {

    public static void render(GuiGraphics graphics, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Tracers tracers = null;
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m instanceof Tracers t && t.isEnabled()) { tracers = t; break; }
        }
        if (tracers == null) return;

        double range = tracers.range.value;
        boolean showPlayers = tracers.players.value >= 1;
        boolean showMobs = tracers.mobs.value >= 1;
        boolean showItems = tracers.items.value >= 1;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        int cx = screenW / 2;
        int cy = screenH / 2;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (!entity.isAlive()) continue;
            double dist = entity.distanceTo(mc.player);
            if (dist > range) continue;

            boolean draw = false;
            int r = 255, g = 255, b = 255;

            if (entity instanceof Player && showPlayers) {
                draw = true; r = 255; g = 50; b = 50; // red for players
            } else if (entity instanceof LivingEntity && !(entity instanceof Player) && showMobs) {
                draw = true; r = 50; g = 255; b = 50; // green for mobs
            } else if (entity instanceof ItemEntity && showItems) {
                draw = true; r = 100; g = 100; b = 255; // blue for items
            }

            if (!draw) continue;

            // Project entity position to screen coordinates
            Vec3 entityPos = entity.getPosition(dt.getGameTimeDeltaPartialTick(true));
            Vec3 playerPos = mc.player.getEyePosition(dt.getGameTimeDeltaPartialTick(true));
            Vec3 diff = entityPos.add(0, entity.getBbHeight() / 2, 0).subtract(playerPos);

            // Use player look direction to compute screen-space position
            float yaw = (float) Math.toRadians(mc.player.getYRot());
            float pitch = (float) Math.toRadians(mc.player.getXRot());

            // Forward vector
            double fx = -Math.sin(yaw) * Math.cos(pitch);
            double fy = -Math.sin(pitch);
            double fz = Math.cos(yaw) * Math.cos(pitch);

            // Right vector
            double rx = Math.cos(yaw);
            double rz = Math.sin(yaw);

            // Up vector
            double ux = -Math.sin(yaw) * (-Math.sin(pitch));
            double uy = Math.cos(pitch);
            double uz = Math.cos(yaw) * (-Math.sin(pitch));

            // Project
            double dot = diff.x * fx + diff.y * fy + diff.z * fz;
            if (dot <= 0.1) continue; // Behind camera

            double sx = (diff.x * rx + diff.z * rz) / dot;
            double sy = -(diff.x * ux + diff.y * uy + diff.z * uz) / dot;

            double fov = mc.options.fov().get();
            double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            int ex = cx + (int)(sx * scale);
            int ey = cy + (int)(sy * scale);

            // Clamp to screen edges
            ex = Math.max(0, Math.min(screenW, ex));
            ey = Math.max(0, Math.min(screenH, ey));

            // Fade alpha by distance
            int alpha = (int)(255 * (1.0 - dist / range));
            alpha = Math.max(40, Math.min(255, alpha));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;

            // Draw line from center to entity screen pos
            drawLine(graphics, cx, cy, ex, ey, color);
        }
    }

    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // Bresenham line using fill rects (1px wide)
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1, sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int steps = 0;
        while (steps < 2000) {
            g.fill(x1, y1, x1 + 1, y1 + 1, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
            steps++;
        }
    }
}
