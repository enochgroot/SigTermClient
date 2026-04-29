package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public class FakeLag extends Module {
    private final Setting delayTicks;
    private Vec3 savedPos = null;
    private int tickCounter = 0;

    public FakeLag() {
        super("FakeLag", "Position desync — freeze locally then snap back", Category.MOVEMENT, 0);
        delayTicks = addSetting("Delay", 10, 2, 40, 2, " ticks");
    }

    @Override
    public void onEnable() {
        if (mc().player != null) savedPos = mc().player.position();
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        tickCounter++;
        int delay = (int) delayTicks.value;
        if (tickCounter <= delay) {
            if (savedPos != null) {
                mc().player.xo = savedPos.x; mc().player.yo = savedPos.y; mc().player.zo = savedPos.z;
                mc().player.setPos(savedPos.x, savedPos.y, savedPos.z);
            }
            mc().player.setDeltaMovement(0, 0, 0);
        } else {
            tickCounter = 0;
            if (mc().player != null) savedPos = mc().player.position();
        }
    }

    @Override
    public void onDisable() { savedPos = null; tickCounter = 0; }
}
