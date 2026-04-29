package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage (zero fallDistance every tick)", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        mc().player.fallDistance = 0;
    }
}
