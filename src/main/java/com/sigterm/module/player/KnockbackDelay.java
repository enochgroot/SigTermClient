package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class KnockbackDelay extends Module {
    private final Setting delayTicks;
    private Vec3 savedMotion = null;
    private int delayTimer = 0;

    public KnockbackDelay() {
        super("KnockbackDelay", "Delay or cancel knockback from hits", Category.PLAYER, 0);
        delayTicks = addSetting("Delay", 10, 0, 40, 5, " ticks");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        // Check if we were just hit (knockback applies velocity change)
        Vec3 currentMotion = mc().player.getDeltaMovement();
        
        // If delay timer is active, restore original motion
        if (delayTimer > 0) {
            delayTimer--;
            if (savedMotion != null && delayTimer == 0) {
                // After delay, apply the knockback (or don't if you want full cancel)
                savedMotion = null;
            } else if (savedMotion != null) {
                // Keep original motion during delay = no knockback
                mc().player.setDeltaMovement(savedMotion);
            }
            return;
        }

        // Detect knockback: sudden velocity change not from player input
        if (!mc().player.onGround() && mc().player.hurtTime > 0) {
            // Player was just hit — save current motion and delay knockback
            savedMotion = currentMotion;
            delayTimer = (int) delayTicks.value;
        }
    }

    @Override
    public void onDisable() {
        savedMotion = null;
        delayTimer = 0;
    }
}
