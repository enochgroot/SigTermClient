package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class AimAssist extends Module {
    private final Setting range;
    private final Setting smoothness;
    private final Setting onlyWhenAttacking;

    public AimAssist() {
        super("AimAssist", "Smoothly aim toward nearest entity", Category.COMBAT, 0);
        range = addSetting("Range", 5.0, 2.0, 8.0, 0.5, "m");
        smoothness = addSetting("Smooth", 0.3, 0.05, 1.0, 0.05, "x");
        onlyWhenAttacking = addSetting("OnlyAttack", 0, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        // Only activate when attacking if toggle is on
        if (onlyWhenAttacking.value >= 1) {
            float cooldown = mc().player.getAttackStrengthScale(0f);
            if (cooldown < 1.0f) return;
        }

        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player 
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (target == null) return;

        // Calculate target angles
        double dx = target.getX() - mc().player.getX();
        double dy = target.getEyeY() - mc().player.getEyeY();
        double dz = target.getZ() - mc().player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float targetPitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));

        // Smooth interpolation toward target angles
        float smooth = (float) smoothness.value;
        float currentYaw = mc().player.getYRot();
        float currentPitch = mc().player.getXRot();

        // Shortest rotation path for yaw
        float yawDiff = targetYaw - currentYaw;
        while (yawDiff > 180) yawDiff -= 360;
        while (yawDiff < -180) yawDiff += 360;

        float newYaw = currentYaw + yawDiff * smooth;
        float newPitch = currentPitch + (targetPitch - currentPitch) * smooth;

        mc().player.setYRot(newYaw);
        mc().player.setXRot(Math.max(-90f, Math.min(90f, newPitch)));
    }
}
