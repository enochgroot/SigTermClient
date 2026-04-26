package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    private boolean wasAirborne = false;

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        boolean onGround = mc().player.onGround();
        
        if (!onGround) {
            wasAirborne = true;
            return;
        }

        // Only zero fall distance when landing (was airborne, now on ground)
        if (wasAirborne) {
            mc().player.fallDistance = 0;
            wasAirborne = false;
        }
    }

    @Override
    public void onDisable() {
        wasAirborne = false;
    }
}
