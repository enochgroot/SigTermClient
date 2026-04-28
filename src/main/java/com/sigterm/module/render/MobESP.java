package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.gui.GuiGraphics;

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
        // TODO: Implement with GuiGraphics - needs 3D to 2D projection
    }
}
