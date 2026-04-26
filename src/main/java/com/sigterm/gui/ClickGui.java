package com.sigterm.gui;

import com.sigterm.config.SigTermConfig;
import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public class ClickGui extends Screen {
    private static final int PW = 140, HH = 20, MH = 14, SH = 12;
    private static final Map<Category, int[]> panelPos = new LinkedHashMap<>();
    private static final Map<Category, Boolean> collapsed = new LinkedHashMap<>();
    private static final Set<String> expanded = new HashSet<>();
    static {
        int x = 6;
        for (Category c : Category.values()) {
            panelPos.put(c, new int[]{x, 28});
            collapsed.put(c, false);
            x += PW + 4;
        }
    }

    private Category dragging = null;
    private double dragOffX, dragOffY;
    private String tooltip = null;
    private int tooltipX, tooltipY;
    private Module listeningModule = null;
    private boolean listeningForGuiKey = false;

    public ClickGui() { super(Component.literal("SigTerm")); }

    private static int getButton(MouseButtonEvent ctx) {
        try { return ctx.buttonInfo().button(); }
        catch (Throwable t) {
            try { var m = ctx.getClass().getMethod("button"); return (int) m.invoke(ctx); }
            catch (Throwable ignored) {} return 0;
        }
    }

    private String keyName(int key) {
        if (key == 0) return "NONE";
        String n = GLFW.glfwGetKeyName(key, 0);
        if (n != null) return n.toUpperCase();
        return switch (key) {
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT"; case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL"; case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT"; case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_TAB -> "TAB"; case GLFW.GLFW_KEY_SPACE -> "SPC";
            case GLFW.GLFW_KEY_ENTER -> "RET"; case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_F1->"F1";case GLFW.GLFW_KEY_F2->"F2";case GLFW.GLFW_KEY_F3->"F3";
            case GLFW.GLFW_KEY_F4->"F4";case GLFW.GLFW_KEY_F5->"F5";case GLFW.GLFW_KEY_F6->"F6";
            case GLFW.GLFW_KEY_F7->"F7";case GLFW.GLFW_KEY_F8->"F8";case GLFW.GLFW_KEY_F9->"F9";
            case GLFW.GLFW_KEY_F10->"F10";case GLFW.GLFW_KEY_F11->"F11";case GLFW.GLFW_KEY_F12->"F12";
            default -> "K" + key;
        };
    }

    private int panelHeight(Category cat) {
        if (collapsed.getOrDefault(cat, false)) return HH;
        int h = HH;
        for (Module m : ModuleManager.INSTANCE.getByCategory(cat)) {
            h += MH;
            if (expanded.contains(m.name) && !m.getSettings().isEmpty())
                h += m.getSettings().size() * SH;
        }
        return h + 3;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x66000000);
        tooltip = null;
        g.fill(0, 0, width, 18, 0xCC7744FF);
        g.fill(0, 17, width, 18, 0xFF5522CC);
        g.drawString(font, Component.literal("\u00a7lSigTerm"), 4, 5, 0xFFFFFFFF);
        String gkl = listeningForGuiKey ? "Menu: [PRESS KEY...]" : "Menu: [" + keyName(ModuleManager.INSTANCE.getGuiKeyBind()) + "]";
        int gbw = font.width(gkl)+8, gbx = width/2-gbw/2;
        g.fill(gbx, 2, gbx+gbw, 16, (mx>=gbx&&mx<gbx+gbw&&my>=2&&my<16) ? 0xFF5533AA : 0xFF332277);
        g.drawString(font, Component.literal(gkl), gbx+4, 5, listeningForGuiKey ? 0xFFFFFF44 : 0xFFFFFFFF);
        long act = ModuleManager.INSTANCE.getModules().stream().filter(Module::isEnabled).count();
        String info = act + " active";
        g.drawString(font, Component.literal(info), width-font.width(info)-4, 5, 0xBBFFFFFF);

        for (Category cat : Category.values()) drawPanel(g, cat, mx, my);

        if (listeningModule != null) {
            String msg = "Press key for " + listeningModule.name + "  (ESC = unbind)";
            int mw = font.width(msg);
            g.fill(width/2-mw/2-6, height-30, width/2+mw/2+6, height-14, 0xEE1A1A30);
            g.fill(width/2-mw/2-6, height-30, width/2+mw/2+6, height-28, 0xFFFFAA00);
            g.drawString(font, Component.literal(msg), width/2-mw/2, height-26, 0xFFFFFF44);
        }
        if (tooltip != null && listeningModule == null) {
            int tw = font.width(tooltip)+8, tx = Math.min(tooltipX+10, width-tw-4), ty = tooltipY-4;
            g.fill(tx-2, ty-2, tx+tw, ty+12, 0xEE1A1A30);
            g.fill(tx-2, ty-2, tx+tw, ty-1, 0xFF7744FF);
            g.drawString(font, Component.literal(tooltip), tx+2, ty+1, 0xFFCCCCDD);
        }
        super.render(g, mx, my, delta);
    }

    private void drawPanel(GuiGraphics g, Category cat, int mx, int my) {
        int[] xy = panelPos.get(cat);
        boolean col = collapsed.getOrDefault(cat, false);
        int totalH = panelHeight(cat);
        int x = xy[0], y = xy[1];
        g.fill(x+3, y+3, x+PW+3, y+totalH+3, 0x33000000);
        boolean hh = mx>=x && mx<x+PW && my>=y && my<y+HH;
        g.fill(x, y, x+PW, y+HH, hh ? brighten(cat.color, 0.15f) : darken(cat.color, 0.4f));
        g.fill(x, y, x+PW, y+2, cat.color);
        g.fill(x, y, x+2, y+totalH, cat.color & 0x66FFFFFF);
        String cn = cat.displayName.toUpperCase();
        g.drawString(font, Component.literal(cn), x+(PW-font.width(cn))/2, y+6, 0xFFFFFFFF);
        g.drawString(font, Component.literal(col?"\u25b6":"\u25bc"), x+PW-12, y+6, 0xAAFFFFFF);
        if (col) return;
        int cy = y + HH;
        g.fill(x, cy, x+PW, y+totalH, 0xDD0D0D1C);
        for (Module m : ModuleManager.INSTANCE.getByCategory(cat)) {
            boolean hover = mx>=x+2 && mx<x+PW-2 && my>=cy && my<cy+MH-1;
            boolean on = m.isEnabled(), binding = (listeningModule==m);
            boolean exp = expanded.contains(m.name) && !m.getSettings().isEmpty();
            g.fill(x+2, cy, x+PW-2, cy+MH-1, binding?0xFF2A1A3A:(on?(hover?0xFF1A3A2A:0xFF0F2A1A):(hover?0xFF1E1E3A:0xFF141428)));
            if (on) g.fill(x+2, cy, x+4, cy+MH-1, 0xFF00DD66);
            if (!m.getSettings().isEmpty()) g.drawString(font, Component.literal(exp?"\u25bc":"\u25b6"), x+6, cy+3, 0xFF555577);
            g.drawString(font, Component.literal(m.name), x+(m.getSettings().isEmpty()?7:16), cy+3,
                binding?0xFFFFFF44:(on?0xFF44FF88:(hover?0xFFDDDDEE:0xFF777799)));
            String kl = binding?"...":keyName(m.getKeyBind());
            int kw = font.width(kl)+6;
            g.fill(x+PW-kw-4, cy+1, x+PW-3, cy+MH-2, binding?0xFF7744FF:0x44FFFFFF);
            g.drawString(font, Component.literal(kl), x+PW-kw-1, cy+3, binding?0xFFFFFF44:0xFF888899);
            if (hover && !binding) { tooltip = m.description; tooltipX = mx; tooltipY = my; }
            cy += MH;
            if (exp) {
                for (Setting s : m.getSettings()) {
                    boolean sHover = mx>=x+4 && mx<x+PW-4 && my>=cy && my<cy+SH-1;
                    g.fill(x+4, cy, x+PW-4, cy+SH-1, sHover?0xFF1A1A3A:0xFF101020);
                    g.drawString(font, Component.literal("  "+s.name), x+8, cy+2, 0xFF8888AA);
                    String val = "< " + s.display() + " >";
                    int vw = font.width(val);
                    // Left half = decrement, right half = increment
                    int midX = x + PW/2;
                    boolean onLeft = mx < midX;
                    int valCol = sHover ? (onLeft ? 0xFFFF8888 : 0xFF88FF88) : 0xFFAAAACC;
                    g.drawString(font, Component.literal(val), x+PW-vw-8, cy+2, valCol);
                    cy += SH;
                }
            }
        }
        g.fill(x, y+totalH-1, x+PW, y+totalH, cat.color & 0x44FFFFFF);
    }

    private int darken(int c, float a) {
        return 0xFF000000|(((int)(((c>>16)&0xFF)*(1-a)))<<16)|(((int)(((c>>8)&0xFF)*(1-a)))<<8)|((int)((c&0xFF)*(1-a)));
    }
    private int brighten(int c, float a) {
        return 0xFF000000|((Math.min(255,(int)(((c>>16)&0xFF)*(1+a))))<<16)|((Math.min(255,(int)(((c>>8)&0xFF)*(1+a))))<<8)|(Math.min(255,(int)((c&0xFF)*(1+a))));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent ctx, boolean doubleClick) {
        double mx = ctx.x(), my = ctx.y();
        int btn = getButton(ctx);
        if (listeningModule != null || listeningForGuiKey) return true;

        String gkl = "Menu: [" + keyName(ModuleManager.INSTANCE.getGuiKeyBind()) + "]";
        int gbw = font.width(gkl)+8, gbx = width/2-gbw/2;
        if (mx>=gbx && mx<gbx+gbw && my>=2 && my<16) { listeningForGuiKey = true; return true; }

        for (Category cat : Category.values()) {
            int[] xy = panelPos.get(cat);
            boolean col = collapsed.getOrDefault(cat, false);
            if (mx>=xy[0] && mx<xy[0]+PW && my>=xy[1] && my<xy[1]+HH) {
                if (btn == 1) { collapsed.put(cat, !col); return true; }
                dragging = cat; dragOffX = mx-xy[0]; dragOffY = my-xy[1]; return true;
            }
            if (col) continue;

            int cy = xy[1] + HH;
            for (Module m : ModuleManager.INSTANCE.getByCategory(cat)) {
                boolean exp = expanded.contains(m.name) && !m.getSettings().isEmpty();

                // Check SETTINGS FIRST (they're below module row)
                if (exp) {
                    int settingsStart = cy + MH;
                    for (Setting s : m.getSettings()) {
                        if (mx>=xy[0]+4 && mx<xy[0]+PW-4 && my>=settingsStart && my<settingsStart+SH) {
                            // Left half of row = decrement, right half = increment
                            int midX = xy[0] + PW / 2;
                            if (mx < midX) s.decrement();
                            else s.increment();
                            SigTermConfig.save();
                            return true;
                        }
                        settingsStart += SH;
                    }
                }

                // Module row
                if (mx>=xy[0]+2 && mx<xy[0]+PW-2 && my>=cy && my<cy+MH) {
                    if (btn == 0) { m.toggle(); SigTermConfig.save(); return true; }
                    if (btn == 1) { listeningModule = m; return true; }
                    if (btn == 2) {
                        if (!m.getSettings().isEmpty()) {
                            if (expanded.contains(m.name)) expanded.remove(m.name);
                            else expanded.add(m.name);
                        }
                        return true;
                    }
                }

                cy += MH;
                if (exp) cy += m.getSettings().size() * SH;
            }
        }
        return super.mouseClicked(ctx, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent ctx, double dx, double dy) {
        if (dragging != null) {
            int[] xy = panelPos.get(dragging);
            xy[0] = (int)(ctx.x()-dragOffX); xy[1] = (int)(ctx.y()-dragOffY); return true;
        }
        return super.mouseDragged(ctx, dx, dy);
    }
    @Override
    public boolean mouseReleased(MouseButtonEvent ctx) { dragging = null; return super.mouseReleased(ctx); }

    @Override
    public boolean keyPressed(KeyEvent ctx) {
        int key = ctx.key();
        if (listeningForGuiKey) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { ModuleManager.INSTANCE.setGuiKeyBind(0); listeningForGuiKey = false; SigTermConfig.save(); return true; }
            ModuleManager.INSTANCE.setGuiKeyBind(key); listeningForGuiKey = false; SigTermConfig.save(); return true;
        }
        if (listeningModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { listeningModule.setKeyBind(0); listeningModule = null; SigTermConfig.save(); return true; }
            listeningModule.setKeyBind(key); listeningModule = null; SigTermConfig.save(); return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE || key == ModuleManager.INSTANCE.getGuiKeyBind()) {
            SigTermConfig.save(); onClose(); return true;
        }
        return super.keyPressed(ctx);
    }

    @Override public boolean isPauseScreen() { return false; }
}
