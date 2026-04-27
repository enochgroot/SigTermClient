package com.sigterm.module.render;

import com.mojang.blaze3d.vertex.*;
import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class MobESP extends Module {
    private final Setting range;
    private final Setting showPlayers;
    private final Setting showMobs;
    private final Setting showAnimals;

    public MobESP() {
        super("MobESP", "Show entities through walls with boxes", Category.RENDER, 0);
        range = addSetting("Range", 64, 16, 256, 16, "m");
        showPlayers = addSetting("Players", 1, 0, 1, 1, "");
        showMobs = addSetting("Mobs", 1, 0, 1, 1, "");
        showAnimals = addSetting("Animals", 0, 0, 1, 1, "");
    }

    public void render(GuiGraphics graphics, float partialTick) {
        if (mc().player == null || mc().level == null) return;

        double range = this.range.value;

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.DEBUG_LINES, 
            DefaultVertexFormat.POSITION_COLOR);

        for (Entity entity : mc().level.entitiesForRendering()) {
            if (entity == mc().player || !entity.isAlive()) continue;
            double dist = entity.distanceTo(mc().player);
            if (dist > range) continue;

            float r = 0, g = 0, b = 0;
            boolean draw = false;

            if (entity instanceof Player && showPlayers.value >= 1) {
                draw = true; r = 1f; g = 0.2f; b = 0.2f;
            } else if (entity instanceof Monster && showMobs.value >= 1) {
                draw = true; r = 0.2f; g = 1f; b = 0.2f;
            } else if (entity instanceof Animal && showAnimals.value >= 1) {
                draw = true; r = 0.2f; g = 0.8f; b = 1f;
            } else if (entity instanceof LivingEntity && showMobs.value >= 1) {
                draw = true; r = 0.8f; g = 0.4f; b = 1f;
            }

            if (!draw) continue;

            var bb = entity.getBoundingBox();
            drawBox(buffer, (float)bb.minX, (float)bb.minY, (float)bb.minZ, 
                     (float)(bb.maxX-bb.minX), (float)(bb.maxY-bb.minY), (float)(bb.maxZ-bb.minZ), r, g, b);
        }

        BufferRenderer.drawWithShader(buffer.end());
    }

    private void drawBox(BufferBuilder buf, float x, float y, float z, float w, float h, float d, 
                         float r, float g, float b) {
        buf.vertex(x, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y, z).color(r, g, b, 1f).next();

        buf.vertex(x, y+h, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z+d).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y+h, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y+h, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y+h, z).color(r, g, b, 1f).next();

        buf.vertex(x, y, z).color(r, g, b, 1f).next();
        buf.vertex(x, y+h, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z).color(r, g, b, 1f).next();
        buf.vertex(x+w, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x+w, y+h, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+d).color(r, g, b, 1f).next();
        buf.vertex(x, y+h, z+d).color(r, g, b, 1f).next();
    }
}
