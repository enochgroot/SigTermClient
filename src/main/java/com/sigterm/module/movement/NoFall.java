package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage (works with Fly)", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        // Always reset fall distance client-side
        mc().player.fallDistance = 0;
        // Send onGround=true packet every tick to server
        // This prevents the server from calculating fall damage
        if (mc().player.tickCount % 4 == 0) {
            mc().player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(
                true, mc().player.horizontalCollision));
        }
    }
}
