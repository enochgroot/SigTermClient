package com.sigterm.module.movement;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import org.lwjgl.glfw.GLFW;

public class FakeLag extends Module {
    private final Setting delayMs;
    private final int PACKET_BUFFER_SIZE = 100;
    
    // Simple packet delay — hold movement packets for N ms
    private long lastSendTime = 0;

    public FakeLag() {
        super("FakeLag", "Delay packets to appear laggy (anti-cheat confusion)", Category.MOVEMENT, 0);
        delayMs = addSetting("Delay", 200, 50, 1000, 50, "ms");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        long now = System.currentTimeMillis();
        long delay = (long) delayMs.value;

        // FakeLag works by desyncing client movement from server
        // In MC 1.21.11, we simulate this by occasionally skipping position updates
        // This is a simplified version — full packet-level FakeLag requires network mixins
        
        if (now - lastSendTime < delay) {
            // Skip this tick's position update — server thinks we're lagging
            // Set velocity to zero so client doesn't move during delay
            mc().player.setDeltaMovement(0, mc().player.getDeltaMovement().y, 0);
            return;
        }

        lastSendTime = now;
    }

    @Override
    public void onDisable() {
        lastSendTime = 0;
    }
}
