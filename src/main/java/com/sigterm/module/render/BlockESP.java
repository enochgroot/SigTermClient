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

                    int r = 0, g = 0, b = 0;
                    boolean draw = false;

                    if (showDiamonds.value >= 1 && (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE))) {
                        draw = true; r = 0; g = 255; b = 255;
                    } else if (showGold.value >= 1 && (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE))) {
                        draw = true; r = 255; g = 214; b = 0;
                    } else if (showIron.value >= 1 && (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE))) {
                        draw = true; r = 198; g = 198; b = 198;
                    } else if (showChests.value >= 1 && (state.is(Blocks.CHEST) || state.is(Blocks.ENDER_CHEST))) {
                        draw = true; r = 138; g = 69; b = 17;
                    }

                    if (!draw) continue;

                    int c = (r << 24) | (g << 16) | (b << 8) | 0xFF;
                    drawBox(buffer, pos.getX(), pos.getY(), pos.getZ(), c);
                }
            }
        }

        BufferRenderer.drawWithShader(buffer.build());
    }

    private void drawBox(BufferBuilder buf, int x, int y, int z, int c) {
        buf.addVertex(x, y, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y, z, c, 0, 0, 0, 0, 0, 0, 0);

        buf.addVertex(x, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);

        buf.addVertex(x, y, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x+1, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y, z+1, c, 0, 0, 0, 0, 0, 0, 0);
        buf.addVertex(x, y+1, z+1, c, 0, 0, 0, 0, 0, 0, 0);
    }
}
