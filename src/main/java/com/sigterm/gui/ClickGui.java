package com.sigterm.gui;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ClickGui extends Screen {

    private static final int PANEL_WIDTH = 120;
    private static final int HEADER_HEIGHT = 22;
    private static final int MODULE_HEIGHT = 16;
    private static final int PANEL_GAP = 8;

    private static final int COL_BG          = 0xE0101020;
    private static final int COL_HEADER_BASE = 0xFF1A1A2E;
    private static final int COL_MODULE_BG   = 0xCC141428;
    private static final int COL_MODULE_HOVER = 0xCC1E1E3A;
    private static final int COL_ENABLED     = 0xFF00CC66;
    private static final int COL_DISABLED    = 0xFF666688;
    private static final int COL_TEXT_WHITE  = 0xFFFFFFFF;
    private static final int COL_TEXT_DIM    = 0xFF888899;
    private static final int COL_ACCENT      = 0xFF7744FF;

    private final Map<Category, PanelState> panels = new LinkedHashMap<>();

    private static class PanelState {
        int x, y;
        boolean collapsed;
        boolean dragging;
        double dragOffX, dragOffY;
    }

    public ClickGui() {
        super(Component.literal("SigTerm Client"));
    }

    @Override
    protected void init() {
        super.init();
        if (panels.isEmpty()) {
            int startX = 20;
            for (Category cat : Category.values()) {
                PanelState ps = new PanelState();
                ps.x = startX;
                ps.y = 30;
                panels.put(cat, ps);
                startX += PANEL_WIDTH + PANEL_GAP;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, float delta) {
        // Dim background
        g.fill(0, 0, width, height, 0x88000000);

        // Title bar
        g.fill(0, 0, width, 20, COL_ACCENT);
        g.drawString(font, Component.literal("SigTerm Client v1.0"), 6, 6, COL_TEXT_WHITE);
        String modCount = ModuleManager.INSTANCE.getModules().stream()
            .filter(Module::isEnabled).count() + " modules active";
        g.drawString(font, Component.literal(modCount), width - font.width(modCount) - 6, 6, COL_TEXT_DIM);

        // Draw each category panel
        for (var entry : panels.entrySet()) {
            Category cat = entry.getKey();
            PanelState ps = entry.getValue();
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            drawPanel(g, cat, ps, mods);
        }

        super.render(g, delta);
    }

    private void drawPanel(GuiGraphics g, Category cat, PanelState ps, List<Module> mods) {
        int pw = PANEL_WIDTH;
        int totalH = ps.collapsed ? HEADER_HEIGHT : HEADER_HEIGHT + mods.size() * MODULE_HEIGHT + 4;

        // Panel shadow
        g.fill(ps.x + 2, ps.y + 2, ps.x + pw + 2, ps.y + totalH + 2, 0x44000000);

        // Header
        int headerColor = blendColor(COL_HEADER_BASE, cat.color, 0.3f);
        g.fill(ps.x, ps.y, ps.x + pw, ps.y + HEADER_HEIGHT, headerColor);
        // Accent line top
        g.fill(ps.x, ps.y, ps.x + pw, ps.y + 2, cat.color);
        // Category name
        g.drawString(font, Component.literal(cat.displayName),
            ps.x + 6, ps.y + 7, COL_TEXT_WHITE);
        // Collapse indicator
        String arrow = ps.collapsed ? "+" : "-";
        g.drawString(font, Component.literal(arrow),
            ps.x + pw - 12, ps.y + 7, COL_TEXT_DIM);

        if (ps.collapsed) return;

        // Module list background
        int listY = ps.y + HEADER_HEIGHT;
        g.fill(ps.x, listY, ps.x + pw, ps.y + totalH, COL_BG);

        // Modules
        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int my = listY + 2 + i * MODULE_HEIGHT;
            int bgColor = COL_MODULE_BG;
            // Module row
            g.fill(ps.x + 2, my, ps.x + pw - 2, my + MODULE_HEIGHT - 1, bgColor);
            // Enabled indicator dot
            int dotColor = m.isEnabled() ? COL_ENABLED : COL_DISABLED;
            g.fill(ps.x + 5, my + 4, ps.x + 9, my + MODULE_HEIGHT - 5, dotColor);
            // Module name
            int textColor = m.isEnabled() ? COL_TEXT_WHITE : COL_TEXT_DIM;
            g.drawString(font, Component.literal(m.name), ps.x + 14, my + 4, textColor);
            // Keybind hint
            if (m.getKeyBind() != 0) {
                String key = org.lwjgl.glfw.GLFW.glfwGetKeyName(m.getKeyBind(), 0);
                if (key == null) key = "?";
                g.drawString(font, Component.literal("[" + key.toUpperCase() + "]"),
                    ps.x + pw - font.width("[" + key.toUpperCase() + "]") - 6, my + 4, 0xFF555577);
            }
        }

        // Bottom border
        g.fill(ps.x, ps.y + totalH - 1, ps.x + pw, ps.y + totalH, cat.color & 0x44FFFFFF);
    }

    private int blendColor(int base, int accent, float t) {
        int bR = (base >> 16) & 0xFF, bG = (base >> 8) & 0xFF, bB = base & 0xFF;
        int aR = (accent >> 16) & 0xFF, aG = (accent >> 8) & 0xFF, aB = accent & 0xFF;
        int r = (int)(bR + (aR - bR) * t);
        int g = (int)(bG + (aG - bG) * t);
        int b = (int)(bB + (aB - bB) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent ctx, boolean doubleClick) {
        double mx = ctx.x();
        double my = ctx.y();
        int btn = ctx.button();

        for (var entry : panels.entrySet()) {
            Category cat = entry.getKey();
            PanelState ps = entry.getValue();
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);

            // Header click — drag or collapse
            if (mx >= ps.x && mx < ps.x + PANEL_WIDTH && my >= ps.y && my < ps.y + HEADER_HEIGHT) {
                if (btn == 1) { // right click = collapse
                    ps.collapsed = !ps.collapsed;
                    return true;
                }
                // left click = start drag
                ps.dragging = true;
                ps.dragOffX = mx - ps.x;
                ps.dragOffY = my - ps.y;
                return true;
            }

            // Module click — toggle
            if (!ps.collapsed && btn == 0) {
                int listY = ps.y + HEADER_HEIGHT + 2;
                for (int i = 0; i < mods.size(); i++) {
                    int modY = listY + i * MODULE_HEIGHT;
                    if (mx >= ps.x && mx < ps.x + PANEL_WIDTH && my >= modY && my < modY + MODULE_HEIGHT) {
                        mods.get(i).toggle();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(ctx, doubleClick);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent ctx, double dx, double dy) {
        double mx = ctx.x();
        double my = ctx.y();
        for (PanelState ps : panels.values()) {
            if (ps.dragging) {
                ps.x = (int)(mx - ps.dragOffX);
                ps.y = (int)(my - ps.dragOffY);
                return true;
            }
        }
        return super.mouseDragged(ctx, dx, dy);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent ctx) {
        for (PanelState ps : panels.values()) ps.dragging = false;
        return super.mouseReleased(ctx);
    }

    @Override public boolean isPauseScreen() { return false; }
}
