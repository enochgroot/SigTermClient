package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", "No slowdown from using items (force sprint)", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (mc().player.isUsingItem()) {
            mc().player.setSprinting(true);
            boolean moving = mc().options.keyUp.isDown() || mc().options.keyDown.isDown()
                || mc().options.keyLeft.isDown() || mc().options.keyRight.isDown();
            if (moving) {
                float yaw = (float) Math.toRadians(mc().player.getYRot());
                double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
                double strafeX = Math.cos(yaw), strafeZ = Math.sin(yaw);
                double mx = 0, mz = 0;
                if (mc().options.keyUp.isDown())    { mx += forwardX; mz += forwardZ; }
                if (mc().options.keyDown.isDown())   { mx -= forwardX; mz -= forwardZ; }
                if (mc().options.keyLeft.isDown())   { mx += strafeX;  mz += strafeZ; }
                if (mc().options.keyRight.isDown())   { mx -= strafeX;  mz -= strafeZ; }
                double len = Math.sqrt(mx * mx + mz * mz);
                if (len > 0) { double spd = 0.12; mx = mx / len * spd; mz = mz / len * spd; }
                var vel = mc().player.getDeltaMovement();
                mc().player.setDeltaMovement(mx, vel.y, mz);
            }
        }
    }
}
