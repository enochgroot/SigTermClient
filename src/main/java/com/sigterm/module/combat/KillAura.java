package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class KillAura extends Module {
    private final Setting range;
    private final Setting weapon; // 0=auto, 1=sword, 2=axe, 3=mace, 4=fist
    private final Setting delayTicks;
    private final Setting onlyCrit;
    private final Setting hitFlick;
    private int tickTimer = 0;

    // Weapon attack speeds (ticks between hits for max damage):
    // Sword: 12 ticks (1.6 attacks/sec)
    // Axe: 20 ticks (1.0 attacks/sec)
    // Mace: 12 ticks (1.6 attacks/sec)
    // Fist: 5 ticks (4.0 attacks/sec)
    private static final int[] WEAPON_DELAYS = {0, 12, 20, 12, 5};

    // HitFlick storage
    private float savedYaw = 0f;
    private float savedPitch = 0f;
    private int flickBackTimer = 0;

    public KillAura() {
        super("KillAura", "Attacks at optimal weapon timing", Category.COMBAT, GLFW.GLFW_KEY_R);
        range = addSetting("Range", 4.5, 2.0, 6.0, 0.5, "m");
        weapon = addSetting("Weapon", 0, 0, 4, 1, ""); // 0=auto 1=sword 2=axe 3=mace 4=fist
        delayTicks = addSetting("Delay", 0, 0, 20, 1, " ticks");
        onlyCrit = addSetting("CritOnly", 0, 0, 1, 1, "");
        hitFlick = addSetting("HitFlick", 0, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        // HitFlick: flick back after attack
        if (flickBackTimer > 0) {
            flickBackTimer--;
            if (flickBackTimer == 0) {
                mc().player.setYRot(savedYaw);
                mc().player.setXRot(savedPitch);
            }
            return;
        }

        tickTimer++;

        int weaponType = (int) weapon.value;
        int extraDelay = (int) delayTicks.value;

        if (weaponType == 0) {
            // Auto mode: use MC's built-in attack cooldown
            float cooldown = mc().player.getAttackStrengthScale(0f);
            if (cooldown < 1.0f) return;
        } else {
            // Manual weapon timing
            int requiredTicks = WEAPON_DELAYS[weaponType] + extraDelay;
            if (tickTimer < requiredTicks) return;
        }

        // Crit check - only crit when falling (moving downward)
        if (onlyCrit.value >= 1) {
            double yVel = mc().player.getDeltaMovement().y;
            if (yVel >= 0) return;
        }

        double r = range.value;
        Entity closest = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (closest != null) {
            // HitFlick: save current look direction and aim at target
            if (hitFlick.value >= 1) {
                savedYaw = mc().player.getYRot();
                savedPitch = mc().player.getXRot();

                double dx = closest.getX() - mc().player.getX();
                double dy = closest.getEyeY() - mc().player.getEyeY();
                double dz = closest.getZ() - mc().player.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
                float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
                mc().player.setYRot(yaw);
                mc().player.setXRot(pitch);

                flickBackTimer = 2; // flick back after 2 ticks
            }

            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, closest);
            mc().player.resetAttackStrengthTicker();
            tickTimer = 0;
        }
    }

    @Override
    public void onEnable() { 
        tickTimer = 100; // ready to attack immediately
        flickBackTimer = 0;
    }

    @Override
    public void onDisable() {
        // Flick back if disabled mid-attack
        if (flickBackTimer > 0) {
            mc().player.setYRot(savedYaw);
            mc().player.setXRot(savedPitch);
            flickBackTimer = 0;
        }
    }
}
