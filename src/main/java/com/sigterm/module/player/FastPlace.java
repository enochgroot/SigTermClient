package com.sigterm.module.player;

import com.sigterm.module.Category;
import com.sigterm.module.Module;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", "No delay between block placements (rightClickDelayTick=0)", Category.PLAYER, 0);
    }

    @Override
    public void onTick() {
        try {
            var field = mc().getClass().getDeclaredField("rightClickDelayTick");
            field.setAccessible(true);
            field.setInt(mc(), 0);
        } catch (Exception ignored) {}
    }
}
