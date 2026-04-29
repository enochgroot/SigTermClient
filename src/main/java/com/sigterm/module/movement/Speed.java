package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    private final Setting multiplier;

    public Speed() {
        super("Speed", "Move faster on ground (velocity boost)", Category.MOVEMENT, GLFW.GLFW_KEY_V);
        multiplier = addSetting("Speed", 3.0, 1.1, 8.0, 0.5, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (mc().player.isShiftKeyDown()) return;
        if (!mc().player.onGround()) return;
        boolean moving = mc().options.keyUp.isDown() || mc().options.keyDown.isDown()
            || mc().options.keyLeft.isDown() || mc().options.keyRight.isDown();
        if (!moving) return;
        float yaw = (float) Math.toRadians(mc().player.getYRot());
        double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
        double strafeX = Math.cos(yaw), strafeZ = Math.sin(yaw);
        double mx = 0, mz = 0;
        if (mc().options.keyUp.isDown())    { mx += forwardX; mz += forwardZ; }
        if (mc().options.keyDown.isDown())   { mx -= forwardX; mz -= forwardZ; }
        if (mc().options.keyLeft.isDown())   { mx += strafeX;  mz += strafeZ; }
        if (mc().options.keyRight.isDown())   { mx -= strafeX;  mz -= strafeZ; }
        double len = Math.sqrt(mx * mx + mz * mz);
        if (len > 0) { double spd = multiplier.value * 0.08; mx = mx / len * spd; mz = mz / len * spd; }
        var vel = mc().player.getDeltaMovement();
        mc().player.setDeltaMovement(mx, vel.y, mz);
    }
}
