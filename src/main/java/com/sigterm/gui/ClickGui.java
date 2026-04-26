package com.sigterm.gui;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ClickGui extends Screen {
    private static final int PW = 120, HH = 22, MH = 16, GAP = 8;
    private final Map<Category, int[]> pos = new LinkedHashMap<>();
    private final Map<Category, Boolean> collapsed = new LinkedHashMap<>();
    private Category dragging = null;
    private double dragOx, dragOy;

    public ClickGui() { super(Component.literal("SigTerm Client")); }

    @Override
    protected void init() {
        super.init();
        if (pos.isEmpty()) {
            int x = 20;
            for (Category c : Category.values()) {
                pos.put(c, new int[]{x, 30});
                collapsed.put(c, false);
                x += PW + GAP;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, float delta) {
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(0, 0, width, 20, 0xFF7744FF);
        g.drawString(font, Component.literal("SigTerm Client v1.0"), 6, 6, 0xFFFFFFFF);
        long active = ModuleManager.INSTANCE.getModules().stream().filter(Module::isEnabled).count();
        String info = active + " active";
        g.drawString(font, Component.literal(info), width - font.width(info) - 6, 6, 0xFF888899);

        for (var e : pos.entrySet()) {
            Category cat = e.getKey();
            int[] xy = e.getValue();
            boolean col = collapsed.getOrDefault(cat, false);
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            int totalH = col ? HH : HH + mods.size() * MH + 4;

            g.fill(xy[0]+2, xy[1]+2, xy[0]+PW+2, xy[1]+totalH+2, 0x44000000);
            g.fill(xy[0], xy[1], xy[0]+PW, xy[1]+HH, blend(0xFF1A1A2E, cat.color, 0.3f));
            g.fill(xy[0], xy[1], xy[0]+PW, xy[1]+2, cat.color);
            g.drawString(font, Component.literal(cat.displayName), xy[0]+6, xy[1]+7, 0xFFFFFFFF);
            g.drawString(font, Component.literal(col?"+":"-"), xy[0]+PW-12, xy[1]+7, 0xFF888899);

            if (!col) {
                int ly = xy[1] + HH;
                g.fill(xy[0], ly, xy[0]+PW, xy[1]+totalH, 0xE0101020);
                for (int i = 0; i < mods.size(); i++) {
                    Module m = mods.get(i);
                    int my = ly + 2 + i * MH;
                    g.fill(xy[0]+2, my, xy[0]+PW-2, my+MH-1, 0xCC141428);
                    g.fill(xy[0]+5, my+4, xy[0]+9, my+MH-5, m.isEnabled() ? 0xFF00CC66 : 0xFF666688);
                    g.drawString(font, Component.literal(m.name), xy[0]+14, my+4,
                        m.isEnabled() ? 0xFFFFFFFF : 0xFF888899);
                }
            }
        }
        super.render(g, delta);
    }

    private int blend(int base, int accent, float t) {
        int bR=(base>>16)&0xFF, bG=(base>>8)&0xFF, bB=base&0xFF;
        int aR=(accent>>16)&0xFF, aG=(accent>>8)&0xFF, aB=accent&0xFF;
        return 0xFF000000|((int)(bR+(aR-bR)*t)<<16)|((int)(bG+(aG-bG)*t)<<8)|(int)(bB+(aB-bB)*t);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent ctx, boolean dbl) {
        double mx = ctx.x(), my = ctx.y();
        int btn = ctx.button();
        for (var e : pos.entrySet()) {
            Category cat = e.getKey();
            int[] xy = e.getValue();
            boolean col = collapsed.getOrDefault(cat, false);
            if (mx>=xy[0] && mx<xy[0]+PW && my>=xy[1] && my<xy[1]+HH) {
                if (btn == 1) { collapsed.put(cat, !col); return true; }
                dragging = cat; dragOx = mx - xy[0]; dragOy = my - xy[1];
                return true;
            }
            if (!col && btn == 0) {
                List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
                int ly = xy[1] + HH + 2;
                for (int i = 0; i < mods.size(); i++) {
                    int modY = ly + i * MH;
                    if (mx>=xy[0] && mx<xy[0]+PW && my>=modY && my<modY+MH) {
                        mods.get(i).toggle(); return true;
                    }
                }
            }
        }
        return super.mouseClicked(ctx, dbl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent ctx, double dx, double dy) {
        if (dragging != null) {
            int[] xy = pos.get(dragging);
            if (xy != null) { xy[0] = (int)(ctx.x() - dragOx); xy[1] = (int)(ctx.y() - dragOy); }
            return true;
        }
        return super.mouseDragged(ctx, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent ctx) {
        dragging = null;
        return super.mouseReleased(ctx);
    }

    @Override public boolean isPauseScreen() { return false; }
}
