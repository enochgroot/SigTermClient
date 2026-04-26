package com.sigterm.gui;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.render.BlockESP;
import com.sigterm.module.render.MobESP;
import com.sigterm.render.TracerRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.Comparator;
import java.util.List;

public class HudOverlay {
    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        float partialTick = dt.getGameTimeDeltaPartialTick(true);
        
        // Render tracers
        try { TracerRenderer.render(g, partialTick); } catch (Exception ignored) {}

        // Render BlockESP
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m instanceof BlockESP esp && esp.isEnabled()) {
                try { esp.render(g, partialTick); } catch (Exception ignored) {}
            }
        }

        // Render MobESP
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m instanceof MobESP esp && esp.isEnabled()) {
                try { esp.render(g, partialTick); } catch (Exception ignored) {}
            }
        }

        // Active modules list
        var font = mc.font;
        List<Module> active = ModuleManager.INSTANCE.getModules().stream()
            .filter(Module::isEnabled)
            .sorted(Comparator.comparingInt((Module m) -> font.width(m.name)).reversed())
            .toList();
        if (active.isEmpty()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int y = 2;
        for (Module m : active) {
            int tw = font.width(m.name);
            int x = screenW - tw - 6;
            g.fill(x - 2, y, screenW, y + 11, 0x88101020);
            g.fill(screenW - 1, y, screenW, y + 11, m.category.color);
            g.drawString(font, Component.literal(m.name), x, y + 2, m.category.color);
            y += 12;
        }
        g.drawString(font, Component.literal("SigTerm"), 4, 4, 0xFF7744FF);
    }
}
