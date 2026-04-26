package com.sigterm.gui;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ClickGui extends Screen {
    private static final int PW = 120, HH = 20, MH = 15;

    // Persistent panel positions across opens
    private static final Map<Category, int[]> panelPos = new LinkedHashMap<>();
    private static final Map<Category, Boolean> collapsed = new LinkedHashMap<>();
    static {
        int x = 20;
        for (Category c : Category.values()) {
            panelPos.put(c, new int[]{x, 30});
            collapsed.put(c, false);
            x += PW + 6;
        }
    }

    private Category dragging = null;
    private double dragOffX, dragOffY;
    private String tooltip = null;
    private int tooltipX, tooltipY;

    public ClickGui() { super(Component.literal("SigTerm")); }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        // Translucent background
        g.fill(0, 0, width, height, 0x66000000);

        tooltip = null;

        // Title bar
        g.fill(0, 0, width, 18, 0xCC7744FF);
        g.fill(0, 17, width, 18, 0xFF5522CC);
        g.drawString(font, Component.literal("\u00a7l SigTerm Client"), 4, 5, 0xFFFFFFFF);
        long active = ModuleManager.INSTANCE.getModules().stream().filter(Module::isEnabled).count();
        String info = active + " active | RIGHT SHIFT to close";
        g.drawString(font, Component.literal(info), width - font.width(info) - 4, 5, 0xBBFFFFFF);

        // Draw panels back-to-front
        for (Category cat : Category.values()) {
            int[] xy = panelPos.get(cat);
            boolean col = collapsed.getOrDefault(cat, false);
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            drawPanel(g, cat, xy, col, mods, mx, my);
        }

        // Tooltip last (on top)
        if (tooltip != null) {
            int tw = font.width(tooltip) + 8;
            int tx = tooltipX + 10;
            int ty = tooltipY - 4;
            if (tx + tw > width) tx = mx - tw - 4;
            g.fill(tx - 2, ty - 2, tx + tw, ty + 12, 0xEE1A1A30);
            g.fill(tx - 2, ty - 2, tx + tw, ty - 1, 0xFF7744FF);
            g.drawString(font, Component.literal(tooltip), tx + 2, ty + 1, 0xFFCCCCDD);
        }

        super.render(g, mx, my, delta);
    }

    private void drawPanel(GuiGraphics g, Category cat, int[] xy, boolean col, List<Module> mods, int mx, int my) {
        int totalH = col ? HH : HH + mods.size() * MH + 3;
        int x = xy[0], y = xy[1];

        // Drop shadow
        g.fill(x + 3, y + 3, x + PW + 3, y + totalH + 3, 0x33000000);

        // Header gradient effect
        boolean headerHover = mx >= x && mx < x + PW && my >= y && my < y + HH;
        int headerBg = headerHover ? brighten(cat.color, 0.15f) : darken(cat.color, 0.4f);
        g.fill(x, y, x + PW, y + HH, headerBg);
        // Top accent line
        g.fill(x, y, x + PW, y + 2, cat.color);
        // Left accent
        g.fill(x, y, x + 2, y + totalH, cat.color & 0x66FFFFFF);
        // Category name centered
        String catName = cat.displayName.toUpperCase();
        int nameW = font.width(catName);
        g.drawString(font, Component.literal(catName), x + (PW - nameW) / 2, y + 6, 0xFFFFFFFF);
        // Collapse arrow
        g.drawString(font, Component.literal(col ? "\u25b6" : "\u25bc"), x + PW - 12, y + 6, 0xAAFFFFFF);

        if (col) return;

        // Module list
        int ly = y + HH;
        g.fill(x, ly, x + PW, y + totalH, 0xDD0D0D1C);

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int mmy = ly + 2 + i * MH;
            boolean hover = mx >= x + 2 && mx < x + PW - 2 && my >= mmy && my < mmy + MH - 1;
            boolean on = m.isEnabled();

            // Row background
            int rowBg = hover ? 0xFF1E1E3A : 0xFF141428;
            if (on) rowBg = hover ? 0xFF1A3A2A : 0xFF0F2A1A;
            g.fill(x + 2, mmy, x + PW - 2, mmy + MH - 1, rowBg);

            // Enabled bar on left
            if (on) g.fill(x + 2, mmy, x + 4, mmy + MH - 1, 0xFF00DD66);

            // Module name
            int textX = on ? x + 7 : x + 7;
            int textCol = on ? 0xFF44FF88 : (hover ? 0xFFDDDDEE : 0xFF777799);
            g.drawString(font, Component.literal(m.name), textX, mmy + 3, textCol);

            // Keybind tag on right
            if (m.getKeyBind() != 0) {
                String keyName = org.lwjgl.glfw.GLFW.glfwGetKeyName(m.getKeyBind(), 0);
                if (keyName == null) keyName = "?";
                String tag = keyName.toUpperCase();
                int tagW = font.width(tag);
                g.fill(x + PW - tagW - 10, mmy + 1, x + PW - 4, mmy + MH - 2, 0x44FFFFFF);
                g.drawString(font, Component.literal(tag), x + PW - tagW - 7, mmy + 3, 0xFF555577);
            }

            // Tooltip on hover
            if (hover && m.description != null && !m.description.isEmpty()) {
                tooltip = m.description;
                tooltipX = mx;
                tooltipY = my;
            }
        }

        // Bottom border line
        g.fill(x, y + totalH - 1, x + PW, y + totalH, cat.color & 0x44FFFFFF);
    }

    private int darken(int color, float amount) {
        int r = (int)(((color >> 16) & 0xFF) * (1 - amount));
        int gr = (int)(((color >> 8) & 0xFF) * (1 - amount));
        int b = (int)((color & 0xFF) * (1 - amount));
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    private int brighten(int color, float amount) {
        int r = Math.min(255, (int)(((color >> 16) & 0xFF) * (1 + amount)));
        int gr = Math.min(255, (int)(((color >> 8) & 0xFF) * (1 + amount)));
        int b = Math.min(255, (int)((color & 0xFF) * (1 + amount)));
        return 0xFF000000 | (r << 16) | (gr << 8) | b;
    }

    // ── Mouse handling (1.21.11 API: MouseButtonEvent) ──

    @Override
    public boolean mouseClicked(MouseButtonEvent ctx, boolean doubleClick) {
        double mx = ctx.x(), my = ctx.y();
        int btn = ctx.button();

        for (Category cat : Category.values()) {
            int[] xy = panelPos.get(cat);
            boolean col = collapsed.getOrDefault(cat, false);

            // Header click
            if (mx >= xy[0] && mx < xy[0] + PW && my >= xy[1] && my < xy[1] + HH) {
                if (btn == 1) { // right = collapse
                    collapsed.put(cat, !col);
                    return true;
                }
                // left = start dragging
                dragging = cat;
                dragOffX = mx - xy[0];
                dragOffY = my - xy[1];
                return true;
            }

            // Module click
            if (!col && btn == 0) {
                List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
                int ly = xy[1] + HH + 2;
                for (int i = 0; i < mods.size(); i++) {
                    int mmy = ly + i * MH;
                    if (mx >= xy[0] + 2 && mx < xy[0] + PW - 2 && my >= mmy && my < mmy + MH) {
                        mods.get(i).toggle();
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(ctx, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent ctx, double dx, double dy) {
        if (dragging != null) {
            int[] xy = panelPos.get(dragging);
            xy[0] = (int)(ctx.x() - dragOffX);
            xy[1] = (int)(ctx.y() - dragOffY);
            return true;
        }
        return super.mouseDragged(ctx, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent ctx) {
        dragging = null;
        return super.mouseReleased(ctx);
    }

    @Override
    public boolean keyPressed(KeyEvent ctx) {
        if (ctx.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
            || ctx.key() == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(ctx);
    }

    @Override public boolean isPauseScreen() { return false; }
}
