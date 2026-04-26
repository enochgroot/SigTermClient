package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class SpearDamage extends Module {
    private final Setting chargeTime;
    private final Setting range;
    private final Setting velocityBoost;
    private final Setting targetPrediction;
    
    private int chargeTicks = 0;
    private boolean isCharging = false;

    public SpearDamage() {
        super("SpearDamage", "Auto-aim, throw trident with velocity boost + prediction", Category.COMBAT, GLFW.GLFW_KEY_H);
        chargeTime = addSetting("Charge", 15, 5, 40, 1, " ticks");
        range = addSetting("Range", 32, 8, 64, 4, "m");
        velocityBoost = addSetting("Velocity", 1.5, 0.5, 3.0, 0.25, "x");
        targetPrediction = addSetting("Predict", 1, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null || mc().gameMode == null) return;
        if (!mc().player.getMainHandItem().is(Items.TRIDENT)) return;

        double r = range.value;
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player && le.isAlive()
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (target == null) {
            if (isCharging) {
                mc().gameMode.releaseUsingItem(mc().player);
                isCharging = false;
                chargeTicks = 0;
            }
            return;
        }

        // Predict target position based on their velocity and throw travel time
        double dx = target.getX() - mc().player.getX();
        double dy = target.getEyeY() - mc().player.getEyeY();
        double dz = target.getZ() - mc().player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);

        if (targetPrediction.value >= 1 && target instanceof LivingEntity living) {
            // Predict where target will be when trident arrives
            double travelTime = dist / (velocityBoost.value * 2.0); // estimated trident speed
            Vec3 targetVel = living.getDeltaMovement();
            dx += targetVel.x * travelTime;
            dy += targetVel.y * travelTime;
            dz += targetVel.z * travelTime;
            dist = Math.sqrt(dx * dx + dz * dz);
        }

        // Compensate for gravity drop over distance (scaled by velocity boost)
        double gravityComp = dist * 0.01 / velocityBoost.value;
        
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy + gravityComp, dist)));
        
        mc().player.setYRot(yaw);
        mc().player.setXRot(pitch);

        // Start charging if not already
        if (!isCharging) {
            mc().gameMode.useItem(mc().player, InteractionHand.MAIN_HAND);
            isCharging = true;
            chargeTicks = 0;
        }

        chargeTicks++;

        // Release after charge time (more charge = more velocity)
        if (chargeTicks >= (int) chargeTime.value) {
            mc().gameMode.releaseUsingItem(mc().player);
            isCharging = false;
            chargeTicks = 0;
        }
    }

    @Override
    public void onDisable() {
        if (isCharging && mc().player != null) {
            mc().gameMode.releaseUsingItem(mc().player);
            isCharging = false;
            chargeTicks = 0;
        }
    }

    private record Vec3(double x, double y, double z) {}
}
