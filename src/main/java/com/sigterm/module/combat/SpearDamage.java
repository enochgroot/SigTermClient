package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class SpearDamage extends Module {
    private int chargeTicks = 0;
    private boolean charging = false;
    private static final int CHARGE_TIME = 15; // ticks to charge trident

    public SpearDamage() {
        super("SpearDamage", "Auto-aims and throws trident at targets", Category.COMBAT, GLFW.GLFW_KEY_H);
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null || mc().gameMode == null) return;
        if (!mc().player.getMainHandItem().is(Items.TRIDENT)) return;

        var target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(32.0), e ->
                e instanceof LivingEntity le && le != mc().player && le.isAlive()
        ).stream()
            .min(Comparator.comparingDouble(e -> e.distanceTo(mc().player)))
            .orElse(null);

        if (target == null) {
            if (charging) stopCharge();
            return;
        }

        // Auto-aim at target
        aimAt(target);

        // Start charging (hold right click)
        if (!charging) {
            mc().gameMode.useItem(mc().player, InteractionHand.MAIN_HAND);
            charging = true;
            chargeTicks = 0;
        }

        chargeTicks++;

        // Release after charge time
        if (chargeTicks >= CHARGE_TIME) {
            mc().gameMode.releaseUsingItem(mc().player);
            charging = false;
            chargeTicks = 0;
        }
    }

    private void aimAt(Entity target) {
        double dx = target.getX() - mc().player.getX();
        double dy = (target.getEyeY()) - mc().player.getEyeY();
        double dz = target.getZ() - mc().player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
        mc().player.setYRot(yaw);
        mc().player.setXRot(pitch);
    }

    private void stopCharge() {
        if (charging && mc().player != null) {
            mc().gameMode.releaseUsingItem(mc().player);
            charging = false;
            chargeTicks = 0;
        }
    }

    @Override
    public void onDisable() { stopCharge(); }
}
