package com.sigterm.gui;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ClickGui extends Screen {
    public ClickGui() { super(Component.literal("SigTerm Client")); }

    @Override
    protected void init() {
        super.init();
        int panelX = 10;
        for (Category cat : Category.values()) {
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            int y = 40;
            for (Module m : mods) {
                final Module mod = m;
                String label = (m.isEnabled() ? "\u00a7a" : "\u00a77") + m.name;
                addRenderableWidget(Button.builder(Component.literal(label), btn -> {
                    mod.toggle();
                    Minecraft.getInstance().setScreen(new ClickGui());
                }).pos(panelX, y).size(110, 16).build());
                y += 18;
            }
            panelX += 118;
        }
        addRenderableWidget(Button.builder(Component.literal("Close"), btn -> onClose())
            .pos(width / 2 - 40, height - 30).size(80, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(0, 0, width, 22, 0xFF7744FF);
        g.drawString(font, Component.literal("SigTerm Client v1.0"), 6, 7, 0xFFFFFFFF);
        long active = ModuleManager.INSTANCE.getModules().stream().filter(Module::isEnabled).count();
        String info = active + " active";
        g.drawString(font, Component.literal(info), width - font.width(info) - 6, 7, 0xFFCCCCDD);
        int panelX = 10;
        for (Category cat : Category.values()) {
            g.fill(panelX, 25, panelX + 110, 38, cat.color);
            g.drawString(font, Component.literal(cat.displayName), panelX + 4, 28, 0xFFFFFFFF);
            panelX += 118;
        }
        super.render(g, mx, my, delta);
    }

    @Override public boolean isPauseScreen() { return false; }
}
