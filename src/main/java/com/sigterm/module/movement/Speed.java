package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    private static final double SPEED_MULT = 1.6;

    public Speed() {
        super("Speed", "Move faster on ground", Category.MOVEMENT, GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (!mc().player.onGround()) return;
        Vec3 vel = mc().player.getDeltaMovement();
        double motionX = vel.x;
        double motionZ = vel.z;
        if (motionX != 0 || motionZ != 0) {
            mc().player.setDeltaMovement(motionX * SPEED_MULT, vel.y, motionZ * SPEED_MULT);
        }
    }
}
