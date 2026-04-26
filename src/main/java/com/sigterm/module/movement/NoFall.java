package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        // Simply zero out fall distance every tick
        // The server calculates fall damage from its own tracking,
        // but the client won't play damage animation or send hurt packets
        mc().player.fallDistance = 0;
        // Also set onGround to prevent the fall damage calculation
        // This is what most clients do — simple and effective
        mc().player.setOnGround(true);
    }

    @Override
    public void onDisable() {
        // Don't leave the player stuck in a fake onGround state
    }
}
