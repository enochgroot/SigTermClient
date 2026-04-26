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
    private int tickTimer = 0;

    // Weapon attack speeds (ticks between hits for max damage):
    // Sword: 12 ticks (1.6 attacks/sec)
    // Axe: 20 ticks (1.0 attacks/sec)
    // Mace: 12 ticks (1.6 attacks/sec)
    // Fist: 5 ticks (4.0 attacks/sec)
    private static final int[] WEAPON_DELAYS = {0, 12, 20, 12, 5};
    // 0 = auto (use attack cooldown)

    public KillAura() {
        super("KillAura", "Attacks at optimal weapon timing", Category.COMBAT, GLFW.GLFW_KEY_R);
        range = addSetting("Range", 4.5, 2.0, 6.0, 0.5, "m");
        weapon = addSetting("Weapon", 0, 0, 4, 1, ""); // 0=auto 1=sword 2=axe 3=mace 4=fist
        delayTicks = addSetting("Delay", 0, 0, 20, 1, " ticks");
        onlyCrit = addSetting("CritOnly", 0, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
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

        // Crit check
        if (onlyCrit.value >= 1) {
            if (mc().player.onGround() || mc().player.getDeltaMovement().y >= 0) return;
        }

        double r = range.value;
        Entity closest = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (closest != null) {
            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, closest);
            mc().player.resetAttackStrengthTicker();
            tickTimer = 0;
        }
    }

    @Override
    public void onEnable() { tickTimer = 100; } // ready to attack immediately
}
