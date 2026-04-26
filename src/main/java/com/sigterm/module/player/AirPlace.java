package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AirPlace extends Module {
    private final Setting reach;
    private int cooldown = 0;

    public AirPlace() {
        super("AirPlace", "Place blocks in mid-air", Category.PLAYER, 0);
        reach = addSetting("Reach", 4.5, 2.0, 6.0, 0.5, "m");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        // Only when holding a block and right-clicking
        if (!mc().options.keyUse.isDown()) return;
        if (!(mc().player.getMainHandItem().getItem() instanceof BlockItem)) return;

        // If we already have a valid block target, let vanilla handle it
        if (mc().hitResult instanceof BlockHitResult bhr && bhr.getType() != net.minecraft.world.phys.HitResult.Type.MISS) return;

        // Calculate position in front of the player at reach distance
        Vec3 eye = mc().player.getEyePosition(1.0f);
        Vec3 look = mc().player.getLookAngle();
        double r = reach.value;
        Vec3 target = eye.add(look.x * r, look.y * r, look.z * r);
        BlockPos pos = BlockPos.containing(target);

        // Create a fake block hit result pointing at the air position
        BlockHitResult fakeHit = new BlockHitResult(
            target, Direction.UP, pos, false);

        mc().gameMode.useItemOn(mc().player, InteractionHand.MAIN_HAND, fakeHit);
        cooldown = 4; // prevent spam
    }
}
