package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;
import java.util.Comparator;

public class Reach extends Module {
    private final Setting range;
    private final Setting onlyWhenAttacking;

    public Reach() {
        super("Reach", "Increase attack range beyond default 3 blocks", Category.COMBAT, 0);
        range = addSetting("Range", 4.5, 3.0, 6.0, 0.5, "m");
        onlyWhenAttacking = addSetting("OnlyAttack", 0, 0, 1, 1, "");
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null) return;

        double r = range.value;
        
        // Find closest entity in extended range
        Entity target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(r), e ->
                e instanceof LivingEntity le && le != mc().player 
                && le.isAlive() && le.distanceTo(mc().player) <= r
        ).stream().min(Comparator.comparingDouble(e -> e.distanceTo(mc().player))).orElse(null);

        if (target == null) return;

        // If onlyWhenAttacking, only extend reach when player is actively attacking
        if (onlyWhenAttacking.value >= 1) {
            float cooldown = mc().player.getAttackStrengthScale(0f);
            if (cooldown < 1.0f) return;
        }

        // Auto-attack entities in extended range (TriggerBot-style when not onlyWhenAttacking)
        if (!onlyWhenAttacking.value >= 1) {
            mc().player.swing(mc().player.getUsedItemHand());
            mc().gameMode.attack(mc().player, target);
            mc().player.resetAttackStrengthTicker();
        }
    }
}
