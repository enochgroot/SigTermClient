package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class BHop extends Module {
    private final Setting hopHeight;
    private final Setting hopSpeed;

    public BHop() {
        super("BHop", "Bunny hop — auto-jump + speed boost", Category.MOVEMENT, 0);
        hopHeight = addSetting("Height", 0.42, 0.3, 1.0, 0.05, "");
        hopSpeed = addSetting("Speed", 1.5, 1.0, 5.0, 0.1, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        // Only bhop when moving forward
        boolean moving = mc().options.keyUp.isDown() || mc().options.keyDown.isDown()
            || mc().options.keyLeft.isDown() || mc().options.keyRight.isDown();
        if (!moving) return;

        if (mc().player.onGround()) {
            // Jump
            mc().player.setDeltaMovement(mc().player.getDeltaMovement().add(0, hopHeight.value, 0));
            // Boost horizontal speed
            Vec3 vel = mc().player.getDeltaMovement();
            mc().player.setDeltaMovement(vel.x * hopSpeed.value, vel.y, vel.z * hopSpeed.value);
        }
    }
}
