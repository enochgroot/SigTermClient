package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class AutoRespawn extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Auto-respawn on death", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (mc().player.isDeadOrDying()) {
            mc().player.respawn();
            mc().setScreen(null);
        }
    }
}
