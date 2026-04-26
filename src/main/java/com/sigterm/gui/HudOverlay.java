package com.sigterm.gui;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;

public class HudOverlay {

    private static final int BG = 0x88101020;
    private static final int ACCENT = 0xFF7744FF;

    public static void render(GuiGraphics g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        var font = mc.font;
        List<Module> active = ModuleManager.INSTANCE.getModules().stream()
            .filter(Module::isEnabled)
            .sorted(Comparator.comparingInt((Module m) -> font.width(m.name)).reversed())
            .toList();

        if (active.isEmpty()) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int y = 2;

        for (Module m : active) {
            int textW = font.width(m.name);
            int x = screenW - textW - 6;
            // Background
            g.fill(x - 2, y, screenW, y + 11, BG);
            // Side accent bar
            g.fill(screenW - 1, y, screenW, y + 11, m.category.color);
            // Module name
            g.drawString(font, Component.literal(m.name), x, y + 2, m.category.color);
            y += 12;
        }

        // Watermark
        g.drawString(font, Component.literal("SigTerm"), 4, 4, ACCENT);
    }
}
