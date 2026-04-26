package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;

public class BHop extends Module {
    private final Setting hopHeight;
    private final Setting hopSpeed;

    public BHop() {
        super("BHop", "Bunny hop — auto-jump + speed", Category.MOVEMENT, 0);
        hopHeight = addSetting("Height", 0.42, 0.3, 1.0, 0.05, "");
        hopSpeed = addSetting("Speed", 1.5, 1.0, 5.0, 0.1, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (mc().player.isShiftKeyDown()) return;
        boolean moving = mc().options.keyUp.isDown() || mc().options.keyDown.isDown()
            || mc().options.keyLeft.isDown() || mc().options.keyRight.isDown();
        if (!moving) return;

        if (mc().player.onGround()) {
            float yaw = (float) Math.toRadians(mc().player.getYRot());
            double fx = -Math.sin(yaw), fz = Math.cos(yaw);
            double sx = Math.cos(yaw), sz = Math.sin(yaw);
            double mx = 0, mz = 0;
            if (mc().options.keyUp.isDown())    { mx += fx; mz += fz; }
            if (mc().options.keyDown.isDown())   { mx -= fx; mz -= fz; }
            if (mc().options.keyLeft.isDown())   { mx += sx; mz += sz; }
            if (mc().options.keyRight.isDown())  { mx -= sx; mz -= sz; }
            double len = Math.sqrt(mx*mx + mz*mz);
            if (len > 0) {
                double spd = hopSpeed.value * 0.2;
                mx = mx/len*spd; mz = mz/len*spd;
            }
            mc().player.setDeltaMovement(mx, hopHeight.value, mz);
        }
    }
}
