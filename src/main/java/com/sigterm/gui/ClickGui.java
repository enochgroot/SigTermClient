package com.sigterm.gui;

import com.sigterm.config.SigTermConfig;
import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public class ClickGui extends Screen {
    private static final int PW = 130, HH = 20, MH = 15;

    // Persistent panel positions
    private static final Map<Category, int[]> panelPos = new LinkedHashMap<>();
    private static final Map<Category, Boolean> collapsed = new LinkedHashMap<>();
    static {
        int x = 10;
        for (Category c : Category.values()) {
            panelPos.put(c, new int[]{x, 30});
            collapsed.put(c, false);
            x += PW + 5;
        }
    }

    private Category dragging = null;
    private double dragOffX, dragOffY;
    private String tooltip = null;
    private int tooltipX, tooltipY;

    // Keybind listening state
    private Module listeningModule = null;      // module waiting for keybind
    private boolean listeningForGuiKey = false;  // true if rebinding the GUI key

    public ClickGui() { super(Component.literal("SigTerm")); }

    private String getKeyName(int key) {
        if (key == 0) return "NONE";
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name != null) return name.toUpperCase();
        // Named keys without printable names
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_BACKSPACE -> "BKSP";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_F1 -> "F1"; case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3"; case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5"; case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7"; case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9"; case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11"; case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_KP_0 -> "NUM0"; case GLFW.GLFW_KEY_KP_1 -> "NUM1";
            case GLFW.GLFW_KEY_KP_2 -> "NUM2"; case GLFW.GLFW_KEY_KP_3 -> "NUM3";
            default -> "KEY" + key;
        };
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
        tooltip = null;

        // Title bar
        g.fill(0, 0, width, 18, 0xCC7744FF);
        g.fill(0, 17, width, 18, 0xFF5522CC);
        g.drawString(font, Component.literal("\u00a7lSigTerm"), 4, 5, 0xFFFFFFFF);

        // GUI keybind button in title bar
        String guiKeyLabel = "Menu: [" + getKeyName(ModuleManager.INSTANCE.getGuiKeyBind()) + "]";
        if (listeningForGuiKey) guiKeyLabel = "Menu: [PRESS A KEY...]";
        int guiBtnW = font.width(guiKeyLabel) + 8;
        int guiBtnX = width / 2 - guiBtnW / 2;
        boolean guiBtnHover = mx >= guiBtnX && mx < guiBtnX + guiBtnW && my >= 2 && my < 16;
        g.fill(guiBtnX, 2, guiBtnX + guiBtnW, 16, guiBtnHover ? 0xFF5533AA : 0xFF332277);
        g.drawString(font, Component.literal(guiKeyLabel), guiBtnX + 4, 5,
            listeningForGuiKey ? 0xFFFFFF44 : 0xFFFFFFFF);

        // Active count
        long active = ModuleManager.INSTANCE.getModules().stream().filter(Module::isEnabled).count();
        String info = active + " active";
        g.drawString(font, Component.literal(info), width - font.width(info) - 4, 5, 0xBBFFFFFF);

        // Panels
        for (Category cat : Category.values()) {
            int[] xy = panelPos.get(cat);
            boolean col = collapsed.getOrDefault(cat, false);
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            drawPanel(g, cat, xy, col, mods, mx, my);
        }

        // Listening overlay
        if (listeningModule != null) {
            String msg = "Press a key for " + listeningModule.name + "  (ESC = cancel, DEL = unbind)";
            int msgW = font.width(msg);
            int bx = width / 2 - msgW / 2 - 6;
            int by = height - 30;
            g.fill(bx, by, bx + msgW + 12, by + 16, 0xEE1A1A30);
            g.fill(bx, by, bx + msgW + 12, by + 2, 0xFFFFAA00);
            g.drawString(font, Component.literal(msg), bx + 6, by + 4, 0xFFFFFF44);
        }

        // Tooltip
        if (tooltip != null && listeningModule == null) {
            int tw = font.width(tooltip) + 8;
            int tx = Math.min(tooltipX + 10, width - tw - 4);
            int ty = tooltipY - 4;
            g.fill(tx - 2, ty - 2, tx + tw, ty + 12, 0xEE1A1A30);
            g.fill(tx - 2, ty - 2, tx + tw, ty - 1, 0xFF7744FF);
            g.drawString(font, Component.literal(tooltip), tx + 2, ty + 1, 0xFFCCCCDD);
        }

        super.render(g, mx, my, delta);
    }

    private void drawPanel(GuiGraphics g, Category cat, int[] xy, boolean col, List<Module> mods, int mx, int my) {
        int totalH = col ? HH : HH + mods.size() * MH + 3;
        int x = xy[0], y = xy[1];

        g.fill(x + 3, y + 3, x + PW + 3, y + totalH + 3, 0x33000000);

        boolean headerHover = mx >= x && mx < x + PW && my >= y && my < y + HH;
        g.fill(x, y, x + PW, y + HH, headerHover ? brighten(cat.color, 0.15f) : darken(cat.color, 0.4f));
        g.fill(x, y, x + PW, y + 2, cat.color);
        g.fill(x, y, x + 2, y + totalH, cat.color & 0x66FFFFFF);
        String catName = cat.displayName.toUpperCase();
        g.drawString(font, Component.literal(catName), x + (PW - font.width(catName)) / 2, y + 6, 0xFFFFFFFF);
        g.drawString(font, Component.literal(col ? "\u25b6" : "\u25bc"), x + PW - 12, y + 6, 0xAAFFFFFF);

        if (col) return;

        int ly = y + HH;
        g.fill(x, ly, x + PW, y + totalH, 0xDD0D0D1C);

        for (int i = 0; i < mods.size(); i++) {
            Module m = mods.get(i);
            int mmy = ly + 2 + i * MH;
            boolean hover = mx >= x + 2 && mx < x + PW - 2 && my >= mmy && my < mmy + MH - 1;
            boolean on = m.isEnabled();
            boolean binding = (listeningModule == m);

            int rowBg = binding ? 0xFF2A1A3A :
                        (on ? (hover ? 0xFF1A3A2A : 0xFF0F2A1A) :
                              (hover ? 0xFF1E1E3A : 0xFF141428));
            g.fill(x + 2, mmy, x + PW - 2, mmy + MH - 1, rowBg);

            if (on) g.fill(x + 2, mmy, x + 4, mmy + MH - 1, 0xFF00DD66);

            int textCol = binding ? 0xFFFFFF44 : (on ? 0xFF44FF88 : (hover ? 0xFFDDDDEE : 0xFF777799));
            g.drawString(font, Component.literal(m.name), x + 7, mmy + 3, textCol);

            // Keybind badge on right
            String keyLabel = binding ? "..." : getKeyName(m.getKeyBind());
            int badgeCol = binding ? 0xFF7744FF : 0x44FFFFFF;
            int badgeTextCol = binding ? 0xFFFFFF44 : 0xFF888899;
            int kw = font.width(keyLabel) + 6;
            g.fill(x + PW - kw - 4, mmy + 1, x + PW - 3, mmy + MH - 2, badgeCol);
            g.drawString(font, Component.literal(keyLabel), x + PW - kw - 1, mmy + 3, badgeTextCol);

            if (hover && !binding) {
                tooltip = m.description + "  |  Right-click to rebind";
                tooltipX = mx; tooltipY = my;
            }
        }
        g.fill(x, y + totalH - 1, x + PW, y + totalH, cat.color & 0x44FFFFFF);
    }

    private int darken(int c, float a) {
        return 0xFF000000 | (((int)(((c>>16)&0xFF)*(1-a)))<<16) | (((int)(((c>>8)&0xFF)*(1-a)))<<8) | ((int)((c&0xFF)*(1-a)));
    }
    private int brighten(int c, float a) {
        return 0xFF000000 | ((Math.min(255,(int)(((c>>16)&0xFF)*(1+a))))<<16) | ((Math.min(255,(int)(((c>>8)&0xFF)*(1+a))))<<8) | (Math.min(255,(int)((c&0xFF)*(1+a))));
    }

    // ── Mouse ──

    @Override
    public boolean mouseClicked(MouseButtonEvent ctx, boolean doubleClick) {
        double mx = ctx.x(), my = ctx.y();
        int btn = ctx.button();

        // If listening for keybind, ignore mouse clicks
        if (listeningModule != null || listeningForGuiKey) return true;

        // GUI keybind button in title bar
        String guiKeyLabel = "Menu: [" + getKeyName(ModuleManager.INSTANCE.getGuiKeyBind()) + "]";
        int guiBtnW = font.width(guiKeyLabel) + 8;
        int guiBtnX = width / 2 - guiBtnW / 2;
        if (mx >= guiBtnX && mx < guiBtnX + guiBtnW && my >= 2 && my < 16) {
            listeningForGuiKey = true;
            return true;
        }

        for (Category cat : Category.values()) {
            int[] xy = panelPos.get(cat);
            boolean col = collapsed.getOrDefault(cat, false);

            // Header
            if (mx >= xy[0] && mx < xy[0] + PW && my >= xy[1] && my < xy[1] + HH) {
                if (btn == 1) { collapsed.put(cat, !col); return true; }
                dragging = cat; dragOffX = mx - xy[0]; dragOffY = my - xy[1];
                return true;
            }

            // Modules
            if (!col) {
                List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
                int ly = xy[1] + HH + 2;
                for (int i = 0; i < mods.size(); i++) {
                    int mmy = ly + i * MH;
                    if (mx >= xy[0] + 2 && mx < xy[0] + PW - 2 && my >= mmy && my < mmy + MH) {
                        if (btn == 0) { // Left click = toggle
                            mods.get(i).toggle();
                            SigTermConfig.save();
                            return true;
                        } else if (btn == 1) { // Right click = rebind
                            listeningModule = mods.get(i);
                            return true;
                        }
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

    // ── Keyboard ──

    @Override
    public boolean keyPressed(KeyEvent ctx) {
        int key = ctx.key();

        // Rebinding GUI key
        if (listeningForGuiKey) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listeningForGuiKey = false;
                return true;
            }
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                ModuleManager.INSTANCE.setGuiKeyBind(0);
            } else {
                ModuleManager.INSTANCE.setGuiKeyBind(key);
            }
            listeningForGuiKey = false;
            SigTermConfig.save();
            return true;
        }

        // Rebinding module key
        if (listeningModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                listeningModule = null;
                return true;
            }
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                listeningModule.setKeyBind(0);
            } else {
                listeningModule.setKeyBind(key);
            }
            listeningModule = null;
            SigTermConfig.save();
            return true;
        }

        // Close GUI
        if (key == GLFW.GLFW_KEY_ESCAPE || key == ModuleManager.INSTANCE.getGuiKeyBind()) {
            SigTermConfig.save();
            onClose();
            return true;
        }
        return super.keyPressed(ctx);
    }

    @Override public boolean isPauseScreen() { return false; }
}
