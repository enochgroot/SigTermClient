package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Fly extends Module {
    private final Setting speed;

    public Fly() {
        super("Fly", "Creative-style flight", Category.MOVEMENT, GLFW.GLFW_KEY_F);
        speed = addSetting("Speed", 2.0, 0.5, 10.0, 0.5, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        double spd = speed.value * 0.25;

        // Calculate movement from WASD input relative to look direction
        float yaw = (float) Math.toRadians(mc().player.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double strafeX = Math.cos(yaw);
        double strafeZ = Math.sin(yaw);

        double mx = 0, mz = 0;
        if (mc().options.keyUp.isDown())    { mx += forwardX; mz += forwardZ; }
        if (mc().options.keyDown.isDown())   { mx -= forwardX; mz -= forwardZ; }
        if (mc().options.keyLeft.isDown())   { mx += strafeX;  mz += strafeZ; }
        if (mc().options.keyRight.isDown())  { mx -= strafeX;  mz -= strafeZ; }

        // Normalize and apply speed
        double len = Math.sqrt(mx * mx + mz * mz);
        if (len > 0) { mx = mx / len * spd; mz = mz / len * spd; }

        double my = 0;
        if (mc().options.keyJump.isDown()) my = spd;
        else if (mc().options.keyShift.isDown()) my = -spd;

        mc().player.setDeltaMovement(mx, my, mz);
        mc().player.fallDistance = 0;
    }

    @Override
    public void onDisable() {
        if (mc().player != null)
            mc().player.setDeltaMovement(Vec3.ZERO);
    }
}
