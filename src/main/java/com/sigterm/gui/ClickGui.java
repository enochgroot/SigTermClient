package com.sigterm.gui;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.*;

public class ClickGui extends Screen {
    private final Map<Category, int[]> pos = new LinkedHashMap<>();
    private final Map<Category, Boolean> collapsed = new LinkedHashMap<>();

    public ClickGui() { super(Component.literal("SigTerm Client")); }

    @Override
    protected void init() {
        super.init();
        if (pos.isEmpty()) {
            int x = 20;
            for (Category c : Category.values()) {
                pos.put(c, new int[]{x, 30});
                collapsed.put(c, false);
                x += 128;
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float delta) {
        g.fill(0, 0, width, height, 0x88000000);
        g.fill(0, 0, width, 20, 0xFF7744FF);
        g.drawString(font, Component.literal("SigTerm Client v1.0"), 6, 6, 0xFFFFFFFF);

        for (var e : pos.entrySet()) {
            Category cat = e.getKey();
            int[] xy = e.getValue();
            boolean col = collapsed.getOrDefault(cat, false);
            List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
            int pw = 120, hh = 22, mh = 16;
            int totalH = col ? hh : hh + mods.size() * mh + 4;

            g.fill(xy[0]+2, xy[1]+2, xy[0]+pw+2, xy[1]+totalH+2, 0x44000000);
            g.fill(xy[0], xy[1], xy[0]+pw, xy[1]+hh, blend(0xFF1A1A2E, cat.color, 0.3f));
            g.fill(xy[0], xy[1], xy[0]+pw, xy[1]+2, cat.color);
            g.drawString(font, Component.literal(cat.displayName), xy[0]+6, xy[1]+7, 0xFFFFFFFF);
            g.drawString(font, Component.literal(col ? "+" : "-"), xy[0]+pw-12, xy[1]+7, 0xFF888899);

            if (!col) {
                int ly = xy[1] + hh;
                g.fill(xy[0], ly, xy[0]+pw, xy[1]+totalH, 0xE0101020);
                for (int i = 0; i < mods.size(); i++) {
                    Module m = mods.get(i);
                    int mmy = ly + 2 + i * mh;
                    g.fill(xy[0]+2, mmy, xy[0]+pw-2, mmy+mh-1, 0xCC141428);
                    g.fill(xy[0]+5, mmy+4, xy[0]+9, mmy+mh-5, m.isEnabled() ? 0xFF00CC66 : 0xFF666688);
                    g.drawString(font, Component.literal(m.name), xy[0]+14, mmy+4,
                        m.isEnabled() ? 0xFFFFFFFF : 0xFF888899);
                }
            }
        }
        super.render(g, mx, my, delta);
    }

    private int blend(int base, int accent, float t) {
        int bR=(base>>16)&0xFF, bG=(base>>8)&0xFF, bB=base&0xFF;
        int aR=(accent>>16)&0xFF, aG=(accent>>8)&0xFF, aB=accent&0xFF;
        return 0xFF000000|((int)(bR+(aR-bR)*t)<<16)|((int)(bG+(aG-bG)*t)<<8)|(int)(bB+(aB-bB)*t);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        for (var e : pos.entrySet()) {
            Category cat = e.getKey();
            int[] xy = e.getValue();
            boolean col = collapsed.getOrDefault(cat, false);
            int pw = 120, hh = 22, mh = 16;
            if (mx>=xy[0] && mx<xy[0]+pw && my>=xy[1] && my<xy[1]+hh) {
                if (btn == 1) { collapsed.put(cat, !col); return true; }
                return true;
            }
            if (!col && btn == 0) {
                List<Module> mods = ModuleManager.INSTANCE.getByCategory(cat);
                int ly = xy[1] + hh + 2;
                for (int i = 0; i < mods.size(); i++) {
                    int modY = ly + i * mh;
                    if (mx>=xy[0] && mx<xy[0]+pw && my>=modY && my<modY+mh) {
                        mods.get(i).toggle(); return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override public boolean isPauseScreen() { return false; }
}
