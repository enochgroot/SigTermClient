package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Always sprint when moving", Category.MOVEMENT, 0);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        // Check if player is moving forward by checking the key state
        boolean movingForward = mc().options.keyUp.isDown();
        if (movingForward && !mc().player.isSprinting()
            && !mc().player.isUsingItem()
            && mc().player.getFoodData().getFoodLevel() > 6) {
            mc().player.setSprinting(true);
        }
    }
}
