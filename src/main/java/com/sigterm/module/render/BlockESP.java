package com.sigterm.module.render;

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
        super("BlockESP", "Highlight valuable blocks through walls (3D boxes)", Category.RENDER, 0);
        range = addSetting("Range", 32, 8, 64, 4, "m");
        showDiamonds = addSetting("Diamonds", true, false, true, "");
        showGold = addSetting("Gold", true, false, true, "");
        showIron = addSetting("Iron", false, false, true, "");
        showChests = addSetting("Chests", true, false, true, "");
    }

    public void render(GuiGraphics graphics, float partialTick) {
        var mc = mc();
        if (mc.player == null || mc.level == null) return;
        double r = range.value;
        int px = (int) mc.player.getX(), py = (int) mc.player.getY(), pz = (int) mc.player.getZ();
        for (int x = px - (int)r; x <= px + r; x++) {
            for (int y = py - (int)r; y <= py + r; y++) {
                for (int z = pz - (int)r; z <= pz + r; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = mc.level.getBlockState(pos);
                    int color = 0; boolean draw = false;
                    if (showDiamonds.value > 0.5 && (state.is(Blocks.DIAMOND_ORE) || state.is(Blocks.DEEPSLATE_DIAMOND_ORE))) { color = 0xFF44FFFF; draw = true; }
                    else if (showGold.value > 0.5 && (state.is(Blocks.GOLD_ORE) || state.is(Blocks.DEEPSLATE_GOLD_ORE))) { color = 0xFFFFAA00; draw = true; }
                    else if (showIron.value > 0.5 && (state.is(Blocks.IRON_ORE) || state.is(Blocks.DEEPSLATE_IRON_ORE))) { color = 0xFFAAAAAA; draw = true; }
                    else if (showChests.value > 0.5 && state.is(Blocks.CHEST)) { color = 0xFFFF8800; draw = true; }
                    if (!draw) continue;
                    double dist = mc.player.distanceToSqr(x + 0.5, y + 0.5, z + 0.5);
                    if (dist > r * r) continue;
                    int alpha = Math.max(80, (int)(255 * (1.0 - Math.sqrt(dist) / r)));
                    int finalColor = color | ((alpha & 0xFF) << 24);
                    double dx = x + 0.5 - mc.player.getX();
                    double dz = z + 0.5 - mc.player.getZ();
                    float yaw = (float) Math.toRadians(mc.player.getYRot());
                    double rx = dx * Math.cos(yaw) + dz * Math.sin(yaw);
                    double rz = -dx * Math.sin(yaw) + dz * Math.cos(yaw);
                    if (rz < 1.0) continue;
                    int offsetX = (int)(rx / rz * 100);
                    int size = Math.max(3, (int)(10 * (1.0 - Math.sqrt(dist) / r)));
                    int screenX = mc.getWindow().getGuiScaledWidth() / 2;
                    int screenY = mc.getWindow().getGuiScaledHeight() / 2;
                    graphics.fill(screenX + offsetX - size, screenY - 1, screenX + offsetX + size, screenY + 1, finalColor);
                    graphics.fill(screenX + offsetX - 1, screenY - size, screenX + offsetX + 1, screenY + size, finalColor);
                }
            }
        }
    }
}
