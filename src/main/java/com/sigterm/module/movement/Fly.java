package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Fly extends Module {
    private static final double FLY_SPEED = 0.5;

    public Fly() {
        super("Fly", "Creative-style flight in survival", Category.MOVEMENT, GLFW.GLFW_KEY_F);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        Vec3 vel = mc().player.getDeltaMovement();
        double y = 0;
        if (mc().options.keyJump.isDown()) y = FLY_SPEED;
        else if (mc().options.keyShift.isDown()) y = -FLY_SPEED;
        mc().player.setDeltaMovement(vel.x, y, vel.z);
        mc().player.fallDistance = 0;
    }

    @Override
    public void onDisable() {
        if (mc().player != null) mc().player.setDeltaMovement(mc().player.getDeltaMovement().multiply(1, 0, 1));
    }
}
