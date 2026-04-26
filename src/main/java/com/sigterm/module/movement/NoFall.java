package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.lwjgl.glfw.GLFW;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;
        if (mc().player.fallDistance > 2.5f) {
            mc().player.connection.send(new ServerboundMovePlayerPacket.StatusOnly(true, mc().player.horizontalCollision));
        }
    }
}
