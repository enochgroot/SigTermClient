package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class AirJump extends Module {
    private boolean wasOnGround = false;

    public AirJump() {
        super("AirJump", "Auto-jump when leaving ground/ledge", Category.MOVEMENT, 0);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        boolean onGround = mc().player.onGround();
        
        if (wasOnGround && !onGround) {
            // Just left ground — jump immediately to maintain momentum
            mc().player.jumpFromGround();
        }

        wasOnGround = onGround;
    }

    @Override
    public void onDisable() {
        wasOnGround = false;
    }
}
