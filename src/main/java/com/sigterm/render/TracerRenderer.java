package com.sigterm.render;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.render.Tracers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class TracerRenderer {

    public static void render(GuiGraphics graphics, float partialTick) {
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
        int cx = screenW / 2, cy = screenH / 2;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive()) continue;
            double dist = entity.distanceTo(mc.player);
            if (dist > range) continue;

            boolean draw = false;
            int r = 255, g = 255, b = 255;
            if (entity instanceof Player && showPlayers) { draw = true; r = 255; g = 50; b = 50; }
            else if (entity instanceof LivingEntity && !(entity instanceof Player) && showMobs) { draw = true; r = 50; g = 255; b = 50; }
            else if (entity instanceof ItemEntity && showItems) { draw = true; r = 100; g = 100; b = 255; }
            if (!draw) continue;

            Vec3 ePos = entity.getPosition(partialTick).add(0, entity.getBbHeight() / 2, 0);
            Vec3 pPos = mc.player.getEyePosition(partialTick);
            Vec3 diff = ePos.subtract(pPos);

            float yaw = (float) Math.toRadians(mc.player.getYRot());
            float pitch = (float) Math.toRadians(mc.player.getXRot());
            double fx = -Math.sin(yaw)*Math.cos(pitch), fy = -Math.sin(pitch), fz = Math.cos(yaw)*Math.cos(pitch);
            double rx = Math.cos(yaw), rz = Math.sin(yaw);
            double ux = -Math.sin(yaw)*(-Math.sin(pitch)), uy = Math.cos(pitch), uz = Math.cos(yaw)*(-Math.sin(pitch));

            double dot = diff.x*fx + diff.y*fy + diff.z*fz;
            if (dot <= 0.1) continue;

            double sx = (diff.x*rx + diff.z*rz) / dot;
            double sy = -(diff.x*ux + diff.y*uy + diff.z*uz) / dot;
            double fov = mc.options.fov().get();
            double scale = screenH / (2.0 * Math.tan(Math.toRadians(fov / 2.0)));

            int ex = Math.max(0, Math.min(screenW, cx + (int)(sx * scale)));
            int ey = Math.max(0, Math.min(screenH, cy + (int)(sy * scale)));

            int alpha = Math.max(40, Math.min(255, (int)(255 * (1.0 - dist / range))));
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            drawLine(graphics, cx, cy, ex, ey, color);
        }
    }

    private static void drawLine(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2-x1), dy = Math.abs(y2-y1);
        int sx = x1<x2?1:-1, sy = y1<y2?1:-1, err = dx-dy;
        for (int i = 0; i < 2000; i++) {
            g.fill(x1, y1, x1+1, y1+1, color);
            if (x1==x2 && y1==y2) break;
            int e2 = 2*err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
}
