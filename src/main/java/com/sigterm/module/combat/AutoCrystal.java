package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class AutoCrystal extends Module {
    private final Setting breakDelay;
    private final Setting placeRange;
    private int tickTimer = 0;

    public AutoCrystal() {
        super("AutoCrystal", "Fast auto-place and break end crystals (Vape 4.0 style)", Category.COMBAT, GLFW.GLFW_KEY_J);
        breakDelay = addSetting("BreakDelay", 0, 0, 5, 1, " ticks");
        placeRange = addSetting("PlaceRange", 6, 3, 9, 1, "m");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        // Find nearest player using entity iteration (players field is not public in 1.21.11)
        Player target = null;
        double closestDist = Double.MAX_VALUE;
        for (Entity e : mc().level.entitiesForRendering()) {
            if (e instanceof Player p && p != mc().player && p.isAlive()) {
                double d = p.distanceTo(mc().player);
                if (d < closestDist) { closestDist = d; target = p; }
            }
        }

        if (target == null) return;

        // First try to break existing crystals
        Entity crystal = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(6.0), e ->
                e instanceof EndCrystal && e.isAlive() && e.distanceTo(mc().player) <= 6.0
        ).stream().findFirst().orElse(null);

        if (crystal != null) {
            mc().gameMode.attack(mc().player, crystal);
            mc().player.swing(InteractionHand.MAIN_HAND);
            tickTimer = (int) breakDelay.value;
            return;
        }

        if (tickTimer > 0) { tickTimer--; return; }

        // Check if holding crystal
        if (!mc().player.getMainHandItem().is(Items.END_CRYSTAL)
            && !mc().player.getOffhandItem().is(Items.END_CRYSTAL)) return;

        InteractionHand hand = mc().player.getMainHandItem().is(Items.END_CRYSTAL)
            ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        double range = placeRange.value;
        
        BlockPos playerPos = mc().player.blockPosition();
        
        for (int radius = 0; radius <= range; radius++) {
            BlockPos foundPos = findPlacePosition(playerPos, (int)radius);
            if (foundPos != null) {
                Vec3 hitVec = Vec3.atCenterOf(foundPos).add(0, 0.5, 0);
                BlockHitResult hit = new BlockHitResult(hitVec,
                    net.minecraft.core.Direction.UP, foundPos, false);
                mc().gameMode.useItemOn(mc().player, hand, hit);
                tickTimer = (int) breakDelay.value;
                return;
            }
        }
    }

    private BlockPos findPlacePosition(BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) != radius && Math.abs(z) != radius) continue;
                
                for (int y = -2; y <= 2; y++) {
                    BlockPos pos = center.offset(x, y, z);
                    var state = mc().level.getBlockState(pos);
                    if ((state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))
                        && mc().level.getBlockState(pos.above()).isAir()
                        && mc().level.getBlockState(pos.above(2)).isAir()) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }
}
