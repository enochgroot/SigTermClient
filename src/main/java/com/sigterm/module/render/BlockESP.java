package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.gui.GuiGraphics;

public class BlockESP extends Module {
    private final Setting range;
    private final Setting showDiamonds;
    private final Setting showGold;
    private final Setting showIron;
    private final Setting showChests;

    public BlockESP() {
        super("BlockESP", "Highlight valuable blocks through walls", Category.RENDER, 0);
        range = addSetting("Range", 32, 8, 64, 4, "m");
        showDiamonds = addSetting("Diamonds", 1, 0, 1, 1, "");
        showGold = addSetting("Gold", 1, 0, 1, 1, "");
        showIron = addSetting("Iron", 0, 0, 1, 1, "");
        showChests = addSetting("Chests", 1, 0, 1, 1, "");
    }

    public void render(GuiGraphics graphics, float partialTick) {
        // TODO: Implement with GuiGraphics - needs 3D to 2D projection
    }
}
