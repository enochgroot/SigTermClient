package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
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
    private int breakDelay = 0;

    public AutoCrystal() {
        super("AutoCrystal", "Auto-places and breaks end crystals", Category.COMBAT, GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
        if (breakDelay > 0) { breakDelay--; return; }

        // Break nearby crystals
        Entity crystal = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(6.0), e ->
                e instanceof EndCrystal && e.isAlive() && e.distanceTo(mc().player) <= 6.0
        ).stream().findFirst().orElse(null);

        if (crystal != null) {
            mc().gameMode.attack(mc().player, crystal);
            mc().player.swing(InteractionHand.MAIN_HAND);
            breakDelay = 2;
            return;
        }

        // Place crystal if holding one
        if (!mc().player.getMainHandItem().is(Items.END_CRYSTAL)
            && !mc().player.getOffhandItem().is(Items.END_CRYSTAL)) return;

        InteractionHand hand = mc().player.getMainHandItem().is(Items.END_CRYSTAL)
            ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;

        // Find obsidian/bedrock nearby to place on
        BlockPos playerPos = mc().player.blockPosition();
        for (int x = -4; x <= 4; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    var state = mc().level.getBlockState(pos);
                    if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
                        BlockPos above = pos.above();
                        if (mc().level.getBlockState(above).isAir()
                            && mc().level.getBlockState(above.above()).isAir()) {
                            Vec3 hitVec = Vec3.atCenterOf(pos).add(0, 0.5, 0);
                            BlockHitResult hit = new BlockHitResult(hitVec,
                                net.minecraft.core.Direction.UP, pos, false);
                            mc().gameMode.useItemOn(mc().player, hand, hit);
                            return;
                        }
                    }
                }
            }
        }
    }
}
