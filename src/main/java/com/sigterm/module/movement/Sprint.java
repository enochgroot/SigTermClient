package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Always sprint when moving (any direction)", Category.MOVEMENT, 0);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        boolean moving = mc().options.keyUp.isDown() || mc().options.keyDown.isDown()
            || mc().options.keyLeft.isDown() || mc().options.keyRight.isDown();
        if (moving && !mc().player.isSprinting() && !mc().player.isUsingItem()
            && mc().player.getFoodData().getFoodLevel() > 6) {
            mc().player.setSprinting(true);
        }
    }
}
