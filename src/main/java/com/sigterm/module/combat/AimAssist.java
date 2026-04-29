package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public class AimAssist extends Module {
    private final Setting range;
    private final Setting smoothness;
    private final Setting fovCircle;

    public AimAssist() {
        super("AimAssist", "Smoothly aim toward nearest entity (FOV check)", Category.COMBAT, 0);
        range = addSetting("Range", 5.0, 2.0, 8.0, 0.5, "m");
        smoothness = addSetting("Smooth", 0.15, 0.02, 0.5, 0.02, "x");
        fovCircle = addSetting("FOV", 90.0, 10.0, 180.0, 5.0, "deg");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);
        if (target == null) return;
        Vec3 eyes = mc().player.getEyePosition();
        Vec3 lookDir = mc().player.getLookAngle();
        Vec3 toTarget = new Vec3(target.getX() - eyes.x, target.getEyeY() - eyes.y, target.getZ() - eyes.z).normalize();
        double dot = lookDir.x * toTarget.x + lookDir.y * toTarget.y + lookDir.z * toTarget.z;
        double angle = Math.toDegrees(Math.acos(Math.min(1.0, dot)));
        if (angle > fovCircle.value) return;
        double dx = toTarget.x, dy = toTarget.y, dz = toTarget.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        float smooth = (float) smoothness.value;
        float currentYaw = mc().player.getYRot();
        float yawDiff = targetYaw - currentYaw;
        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;
        mc().player.setYRot(currentYaw + yawDiff * smooth);
        mc().player.setXRot(Math.max(-90f, Math.min(90f, mc().player.getXRot() + (targetPitch - mc().player.getXRot()) * smooth)));
    }
}
