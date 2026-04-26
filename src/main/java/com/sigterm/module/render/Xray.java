package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.client.renderer.BlockRenderManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Xray extends Module {
    private final Setting range;
    
    // Blocks to make transparent
    private static final Set<Block> HIDE_BLOCKS = new HashSet<>(Arrays.asList(
        Blocks.STONE, Blocks.DEEPSLATE, Blocks.ANDESITE, Blocks.GRANITE,
        Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.GRAVEL, Blocks.SAND,
        Blocks.RED_SAND, Blocks.NETHERRACK, Blocks.END_STONE,
        Blocks.TUFF, Blocks.DRIPSTONE_BLOCK, Blocks.CALCITE,
        Blocks.MUD, Blocks.CLAY, Blocks.GILDED_BLACKSTONE
    ));

    public Xray() {
        super("Xray", "Make surrounding blocks transparent, highlight ores", Category.RENDER, 0);
        range = addSetting("Range", 16, 4, 32, 4, "m");
    }

    @Override
    public void onEnable() {
        // Xray works by modifying block render layers at runtime
        // In MC 1.21.11 Fabric, we can use mixin or reflection to change block opacity
        // For simplicity, this module toggles a flag that the render pipeline checks
    }

    @Override
    public void onTick() {
        // Xray is primarily a render-time effect — no tick logic needed
        // The actual transparency is handled in the render callback
    }

    public boolean shouldHideBlock(Block block) {
        return HIDE_BLOCKS.contains(block);
    }

    public double getRange() {
        return range.value;
    }
}
