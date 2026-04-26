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
    private int chargeTicks = 0;
    private boolean isCharging = false;

    public SpearDamage() {
        super("SpearDamage", "Auto-aim and throw trident", Category.COMBAT, GLFW.GLFW_KEY_H);
        chargeTime = addSetting("Charge", 15, 5, 40, 1, " ticks");
        range = addSetting("Range", 32, 8, 64, 4, "m");
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

        // Aim at target with gravity compensation
        double dx = target.getX() - mc().player.getX();
        double dy = target.getEyeY() - mc().player.getEyeY();
        double dz = target.getZ() - mc().player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        // Compensate for gravity drop over distance
        double gravityComp = dist * 0.01;
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy + gravityComp, dist)));
        mc().player.setYRot(yaw);
        mc().player.setXRot(pitch);

        // Start charging
        if (!isCharging) {
            mc().gameMode.useItem(mc().player, InteractionHand.MAIN_HAND);
            isCharging = true;
            chargeTicks = 0;
        }

        chargeTicks++;

        // Release after charge time
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
}
