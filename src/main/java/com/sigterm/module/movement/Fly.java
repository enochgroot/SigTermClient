package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Fly extends Module {
    private final Setting speed;

    public Fly() {
        super("Fly", "Creative-style flight (noGravity + setPos)", Category.MOVEMENT, GLFW.GLFW_KEY_F);
        speed = addSetting("Speed", 2.0, 0.5, 10.0, 0.5, "x");
    }

    @Override
    public void onEnable() {
        if (mc().player != null)
            mc().player.noGravity = true;
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        double spd = speed.value * 0.12;
        float yaw = (float) Math.toRadians(mc().player.getYRot());
        double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
        double strafeX = Math.cos(yaw), strafeZ = Math.sin(yaw);
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
        mc().player.xo = mc().player.getX();
        mc().player.yo = mc().player.getY();
        mc().player.zo = mc().player.getZ();
        Vec3 pos = mc().player.position();
        mc().player.setPos(pos.x + mx, pos.y + my, pos.z + mz);
        mc().player.setDeltaMovement(0, 0, 0);
        mc().player.fallDistance = 0;
    }

    @Override
    public void onDisable() {
        if (mc().player != null) {
            mc().player.noGravity = false;
            mc().player.setDeltaMovement(0, 0, 0);
        }
    }
}
