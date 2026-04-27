package com.sigterm.module.render;

import com.mojang.blaze3d.vertex.*;
import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

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
        if (mc().player == null || mc().level == null) return;

        int range = (int) this.range.value;
        int playerX = mc().player.getBlockX();
        int playerY = mc().player.getBlockY();
        int playerZ = mc().player.getBlockZ();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buffer = tess.begin(VertexFormat.Mode.DEBUG_LINES, 
            DefaultVertexFormat.POSITION_COLOR);

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos pos = new BlockPos(playerX + x, playerY + y, playerZ + z);
                    var state = mc().level.getBlockState(pos);

                    float r = 0, g = 0, b = 0;
                    boolean draw = false;

                    if (showDiamonds.value >= 1 && (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE))) {
                        draw = true; r = 0; g = 1f; b = 1f;
                    } else if (showGold.value >= 1 && (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE))) {
                        draw = true; r = 1f; g = 0.84f; b = 0;
                    } else if (showIron.value >= 1 && (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE))) {
                        draw = true; r = 0.78f; g = 0.78f; b = 0.78f;
                    } else if (showChests.value >= 1 && (state.is(Blocks.CHEST) || state.is(Blocks.ENDER_CHEST))) {
                        draw = true; r = 0.54f; g = 0.27f; b = 0.07f;
                    }

                    if (!draw) continue;

                    drawBox(buffer, pos.getX(), pos.getY(), pos.getZ(), r, g, b);
                }
            }
        }

        BufferRenderer.drawWithShader(buffer.end());
    }

    private void drawBox(BufferBuilder buf, int x, int y, int z, float r, float g, float b) {
        buf.vertex(x, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y, z).color(r, g, b, 1f).next();

        buf.vertex(x, y+1, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z+1).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y+1, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y+1, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y+1, z).color(r, g, b, 1f).next();

        buf.vertex(x, y, z).color(r, g, b, 1f).next();
        buf.vertex(x, y+1, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z).color(r, g, b, 1f).next();
        buf.vertex(x+1, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x+1, y+1, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y, z+1).color(r, g, b, 1f).next();
        buf.vertex(x, y+1, z+1).color(r, g, b, 1f).next();
    }
}
