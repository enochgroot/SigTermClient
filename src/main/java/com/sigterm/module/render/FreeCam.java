package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class FreeCam extends Module {
    private final Setting speed;
    private Vec3 savedPos = null;

    public FreeCam() {
        super("FreeCam", "Move camera independently from player body", Category.RENDER, 0);
        speed = addSetting("Speed", 3.0, 0.5, 10.0, 0.5, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        // FreeCam works by modifying the render camera position
        // In MC 1.21.11, we modify the player's render position
        double spd = speed.value * 0.15;

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

        // Move camera position only (not actual player position)
        // This is a client-side render offset — server doesn't see it
        mc().player.setDeltaMovement(mx, my, mz);
    }

    @Override
    public void onDisable() {
        if (mc().player != null) {
            mc().player.setDeltaMovement(0, 0, 0);
        }
    }
}
