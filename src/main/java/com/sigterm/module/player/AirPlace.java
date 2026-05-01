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
        super("AirPlace", "Place blocks in mid-air (raycast + face check)", Category.PLAYER, 0);
        reach = addSetting("Reach", 4.5, 2.0, 6.0, 0.5, "m");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (!mc().options.keyUse.isDown()) return;
        if (!(mc().player.getMainHandItem().getItem() instanceof BlockItem)) return;
        if (mc().hitResult instanceof BlockHitResult bhr && bhr.getType() != net.minecraft.world.phys.HitResult.Type.MISS) return;
        Vec3 eye = mc().player.getEyePosition(1.0f);
        Vec3 look = mc().player.getLookAngle();
        double r = reach.value;
        for (double d = 0.5; d < r; d += 0.1) {
            Vec3 check = eye.add(look.x * d, look.y * d, look.z * d);
            BlockPos pos = BlockPos.containing(check);
            if (!mc().level.getBlockState(pos).isAir()) {
                Direction face = getClosestFace(eye, pos);
                Vec3 target = Vec3.atCenterOf(pos).add(face.getUnitVector().scale(0.5));
                BlockHitResult fakeHit = new BlockHitResult(target, face, pos.relative(face), false);
                mc().gameMode.useItemOn(mc().player, InteractionHand.MAIN_HAND, fakeHit);
                cooldown = 4;
                return;
            }
        }
        Vec3 target = eye.add(look.x * r, look.y * r, look.z * r);
        BlockPos pos = BlockPos.containing(target);
        BlockHitResult fakeHit = new BlockHitResult(target, Direction.UP, pos, false);
        mc().gameMode.useItemOn(mc().player, InteractionHand.MAIN_HAND, fakeHit);
        cooldown = 4;
    }

    private Direction getClosestFace(Vec3 from, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double dx = from.x - center.x, dy = from.y - center.y, dz = from.z - center.z;
        if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > Math.abs(dz))
            return dx > 0 ? Direction.WEST : Direction.EAST;
        if (Math.abs(dy) > Math.abs(dz)) return dy > 0 ? Direction.DOWN : Direction.UP;
        return dz > 0 ? Direction.NORTH : Direction.SOUTH;
    }
}
