package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Xray extends Module {
    private final Setting range;
    public static final Set<Block> ORE_BLOCKS = new HashSet<>(Arrays.asList(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE,
        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
        Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
        Blocks.ANCIENT_DEBRIS, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE
    ));
    public static final Set<Block> HIDE_BLOCKS = new HashSet<>(Arrays.asList(
        Blocks.STONE, Blocks.DEEPSLATE, Blocks.ANDESITE, Blocks.GRANITE,
        Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.SAND,
        Blocks.RED_SAND, Blocks.NETHERRACK, Blocks.END_STONE,
        Blocks.TUFF, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE,
        Blocks.MUD, Blocks.CLAY, Blocks.GILDED_BLACKSTONE
    ));

    public Xray() {
        super("Xray", "Highlight ore blocks through walls (used by BlockESP)", Category.RENDER, 0);
        range = addSetting("Range", 16, 4, 32, 4, "m");
    }

    public Set<Block> getOreBlocks() { return ORE_BLOCKS; }
    public Set<Block> getHideBlocks() { return HIDE_BLOCKS; }
    public double getRange() { return range.value; }
}
