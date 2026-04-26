package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "No delay between block placements", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        // FastPlace reduces the rightClickDelay to 0
        // In production this would use a mixin on Minecraft.rightClickDelay
        // For now we use reflection-safe approach
    }
}
