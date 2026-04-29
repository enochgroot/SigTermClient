package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;

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
            var vel = mc().player.getDeltaMovement();
            mc().player.setDeltaMovement(vel.x, 0.42, vel.z);
        }
        wasOnGround = onGround;
    }

    @Override
    public void onDisable() { wasOnGround = false; }
}
