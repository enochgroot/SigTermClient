package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;
import java.util.List;

public class KillAura extends Module {
    private static final double RANGE = 4.5;
    private int tickDelay = 0;

    public KillAura() {
        super("KillAura", "Attacks nearby entities automatically", Category.COMBAT, GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;
        if (tickDelay > 0) { tickDelay--; return; }
        float cooldown = mc().player.getAttackStrengthScale(0f);
        if (cooldown < 0.9f) return;

        List<Entity> targets = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(RANGE), e -> {
                if (!(e instanceof LivingEntity le)) return false;
                if (le == mc().player) return false;
                if (!le.isAlive()) return false;
                if (le.distanceTo(mc().player) > RANGE) return false;
                return true;
            });

        Entity closest = targets.stream()
            .min(Comparator.comparingDouble(e -> e.distanceTo(mc().player)))
            .orElse(null);

        if (closest != null) {
            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, closest);
            mc().player.resetAttackStrengthTicker();
            tickDelay = 1;
        }
    }
}
