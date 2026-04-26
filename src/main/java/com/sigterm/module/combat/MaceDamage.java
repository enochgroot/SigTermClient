package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

public class MaceDamage extends Module {
    private int jumpTicks = 0;

    public MaceDamage() {
        super("MaceDamage", "Auto-jumps + times mace attacks for max fall damage", Category.COMBAT, GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        if (mc().player == null || mc().level == null || mc().gameMode == null) return;
        if (!mc().player.getMainHandItem().is(Items.MACE)) return;

        // Find nearest target
        var target = mc().level.getEntities(mc().player,
            mc().player.getBoundingBox().inflate(5.0), e ->
                e instanceof net.minecraft.world.entity.LivingEntity le
                && le != mc().player && le.isAlive()
                && le.distanceTo(mc().player) <= 5.0
        ).stream().findFirst().orElse(null);

        if (target == null) { jumpTicks = 0; return; }

        // Phase 1: Jump to build fall distance
        if (mc().player.onGround()) {
            mc().player.jumpFromGround();
            jumpTicks = 0;
        }
        jumpTicks++;

        // Phase 2: Attack on the way down for max fall damage bonus
        // Mace does bonus damage based on fall distance — attack after peak
        if (jumpTicks > 6 && mc().player.getDeltaMovement().y < -0.1) {
            float cooldown = mc().player.getAttackStrengthScale(0f);
            if (cooldown >= 0.9f && mc().player.distanceTo(target) <= 5.0) {
                mc().player.swing(mc().player.getUsedItemHand());
                mc().gameMode.attack(mc().player, target);
                mc().player.resetAttackStrengthTicker();
                jumpTicks = 0;
            }
        }
    }
}
