package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class MobESP extends Module {
    private final Setting range;
    private final Setting showPlayers;
    private final Setting showMobs;
    private final Setting showAnimals;

    public MobESP() {
        super("MobESP", "Show entities through walls (2D indicators)", Category.RENDER, 0);
        range = addSetting("Range", 64, 16, 256, 16, "m");
        showPlayers = addSetting("Players", true, false, true, "");
        showMobs = addSetting("Mobs", true, false, true, "");
        showAnimals = addSetting("Animals", false, false, true, "");
    }

    public void render(GuiGraphics graphics, float partialTick) {
        var mc = mc();
        if (mc.player == null || mc.level == null) return;
        double r = range.value;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int cx = screenW / 2, cy = mc.getWindow().getGuiScaledHeight() / 2;
        float yaw = (float) Math.toRadians(mc.player.getYRot());
        double cosY = Math.cos(yaw), sinY = Math.sin(yaw);
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player || !entity.isAlive()) continue;
            double dist = entity.distanceTo(mc.player);
            if (dist > r) continue;
            int color = 0; boolean draw = false;
            if (entity instanceof Player && showPlayers.value > 0.5) { color = 0xFFFF4444; draw = true; }
            else if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                if (showMobs.value > 0.5) { color = 0xFF44FF44; draw = true; }
                else if (showAnimals.value > 0.5) { color = 0xFF4488FF; draw = true; }
            }
            if (!draw) continue;
            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            double rx = dx * cosY + dz * sinY;
            double rz = -dx * sinY + dz * cosY;
            if (rz < 1.0) continue;
            int offsetX = (int)(rx / rz * 100);
            int alpha = Math.max(60, (int)(255 * (1.0 - dist / r)));
            int size = Math.max(2, (int)(8 * (1.0 - dist / r)));
            int finalColor = color | ((alpha & 0xFF) << 24);
            graphics.fill(cx + offsetX - size, cy - size, cx + offsetX + size, cy + size, finalColor);
        }
    }
}
