package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.client.gui.screens.DeathScreen;
import org.lwjgl.glfw.GLFW;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Auto-respawn on death", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        // Check if death screen is showing
        if (mc().screen instanceof DeathScreen) {
            mc().player.respawn();
            mc().setScreen(null);
            return;
        }

        // Also check if player is dead (fallback)
        if (mc().player != null && mc().player.isDeadOrDying()) {
            mc().player.respawn();
            mc().setScreen(null);
        }
    }
}
