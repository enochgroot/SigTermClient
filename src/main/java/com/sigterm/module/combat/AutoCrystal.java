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

public class AutoCrystal extends Module {
    private final Setting breakDelay;
    private final Setting placeRange;
    private final Setting autoAim;
    private int tickTimer = 0;

    public AutoCrystal() {
        super("AutoCrystal", "Fast auto-place and break end crystals (Vape 4.0 style)", Category.COMBAT, GLFW.GLFW_KEY_J);
        breakDelay = addSetting("BreakDelay", 0, 0, 5, 1, " ticks");
        placeRange = addSetting("PlaceRange", 6, 3, 9, 1, "m");
        autoAim = addSetting("AutoAim", true, false, true, " rotate to target");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        // Find nearest player using getEntities (reliable in 1.21.11)
        Player target = findNearestPlayer(8.0);
        if (target == null) return;

        // First try to break existing crystals in range
        Entity crystal = findNearestCrystal(6.0);

        if (crystal != null) {
            if (autoAim.value) rotateToEntity(crystal);
            mc().gameMode.attack(mc().player, crystal);
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

                if (autoAim.value) rotateToBlock(foundPos);
                mc().gameMode.useItemOn(mc().player, hand, hit);
                tickTimer = (int) breakDelay.value;
                return;
            }
        }
    }

    private Player findNearestPlayer(double range) {
        Player closest = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(range))) {
            if (e instanceof Player p && p != mc().player && p.isAlive()) {
                double d = p.distanceTo(mc().player);
                if (d < bestDist) { bestDist = d; closest = p; }
            }
        }
        return closest;
    }

    private Entity findNearestCrystal(double range) {
        double bestDist = Double.MAX_VALUE;
        Entity closest = null;

        for (Entity e : mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(range))) {
            if (e instanceof EndCrystal && e.isAlive()) {
                double d = e.distanceTo(mc().player);
                if (d < bestDist) { bestDist = d; closest = e; }
            }
        }
        return closest;
    }

    private void rotateToEntity(Entity target) {
        Vec3 eyes = mc().player.getEyePosition();
        Vec3 targetPos = new Vec3(
            target.getX(), target.getY() + target.getEyeHeight() * 0.5, target.getZ());

        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        mc().player.setYRot(yaw);
        mc().player.setXRot(pitch);
    }

    private void rotateToBlock(BlockPos pos) {
        Vec3 eyes = mc().player.getEyePosition();
        Vec3 targetPos = Vec3.atCenterOf(pos);

        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        mc().player.setYRot(yaw);
        mc().player.setXRot(pitch);
    }

    private BlockPos findPlacePosition(BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                // Check perimeter + some interior for better coverage
                if (radius > 0 && Math.abs(x) < radius - 1 && Math.abs(z) < radius - 1) continue;

                for (int y = -2; y <= 3; y++) {
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
