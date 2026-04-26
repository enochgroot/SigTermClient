package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class Speed extends Module {
    private final Setting multiplier;

    public Speed() {
        super("Speed", "Move faster on ground", Category.MOVEMENT, GLFW.GLFW_KEY_V);
        multiplier = addSetting("Speed", 3.0, 1.1, 15.0, 0.5, "x");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        // Don't apply when sneaking
        if (mc().player.isShiftKeyDown()) return;
        // Don't apply when not on ground
        if (!mc().player.onGround()) return;
        // Don't stack with BHop
        for (Module m : ModuleManager.INSTANCE.getModules()) {
            if (m instanceof BHop && m.isEnabled()) return;
        }
        Vec3 vel = mc().player.getDeltaMovement();
        if (vel.x != 0 || vel.z != 0) {
            mc().player.setDeltaMovement(vel.x * multiplier.value, vel.y, vel.z * multiplier.value);
        }
    }
}
