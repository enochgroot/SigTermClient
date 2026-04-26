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
        Vec3 vel = mc().player.getDeltaMovement();
        double y = 0;
        if (mc().options.keyJump.isDown()) y = spd;
        else if (mc().options.keyShift.isDown()) y = -spd;

        // Horizontal speed boost
        double mx = vel.x, mz = vel.z;
        double hLen = Math.sqrt(mx * mx + mz * mz);
        if (hLen > 0.01) {
            double mult = spd / hLen;
            mx = mx * Math.min(mult, speed.value);
            mz = mz * Math.min(mult, speed.value);
        }

        mc().player.setDeltaMovement(mx, y, mz);
        mc().player.fallDistance = 0;
    }

    @Override
    public void onDisable() {
        if (mc().player != null)
            mc().player.setDeltaMovement(mc().player.getDeltaMovement().multiply(1, 0, 1));
    }
}
