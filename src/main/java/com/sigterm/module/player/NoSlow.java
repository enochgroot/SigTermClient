package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoSlow extends Module {
    public NoSlow() {
        super("NoSlow", "No slowdown from using items", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        // NoSlow effect is handled by mixin injection in production
        // This tick handler is a placeholder — the actual implementation
        // would inject into LivingEntity.travel() to remove slowdown factor
    }
}
