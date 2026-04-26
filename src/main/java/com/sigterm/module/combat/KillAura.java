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
    private final Setting onlyCrit;

    public KillAura() {
        super("KillAura", "Attacks at optimal crit timing", Category.COMBAT, GLFW.GLFW_KEY_R);
        range = addSetting("Range", 4.5, 2.0, 6.0, 0.5, "m");
        onlyCrit = addSetting("CritOnly", 1, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        // Wait for FULL cooldown (optimal damage)
        float cooldown = mc().player.getAttackStrengthScale(0f);
        if (cooldown < 1.0f) return;

        // Crit only: must be falling (deltaY < 0) and not on ground
        if (onlyCrit.value >= 1) {
            if (mc().player.onGround()) return;
            if (mc().player.getDeltaMovement().y >= 0) return;
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
        }
    }
}
