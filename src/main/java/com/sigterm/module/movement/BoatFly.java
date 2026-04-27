package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.entity.Entity;
import org.lwjgl.glfw.GLFW;

public class BoatFly extends Module {
    private final Setting speed;

    public BoatFly() {
        super("BoatFly", "Fly while in a boat", Category.MOVEMENT, 0);
        speed = addSetting("Speed", 1.0, 0.2, 5.0, 0.2, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        // Check if player is in a boat (any vehicle with "boat" in name)
        Entity vehicle = mc().player.getVehicle();
        if (vehicle == null || !vehicle.getType().getDescription().getString().toLowerCase().contains("boat")) return;

        double spd = speed.value * 0.25;

        float yaw = (float) Math.toRadians(mc().player.getYRot());
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double strafeX = Math.cos(yaw);
        double strafeZ = Math.sin(yaw);

        double mx = 0, mz = 0;
        if (mc().options.keyUp.isDown())    { mx += forwardX; mz += forwardZ; }
        if (mc().options.keyDown.isDown())   { mx -= forwardX; mz -= forwardZ; }
        if (mc().options.keyLeft.isDown())   { mx += strafeX;  mz += strafeZ; }
        if (mc().options.keyRight.isDown())   { mx -= strafeX;  mz -= strafeZ; }

        double len = Math.sqrt(mx * mx + mz * mz);
        if (len > 0) { mx = mx / len * spd; mz = mz / len * spd; }

        double my = 0;
        if (mc().options.keyJump.isDown()) my = spd;
        else if (mc().options.keyShift.isDown()) my = -spd;

        mc().player.setDeltaMovement(mx, my, mz);
    }
}
