package com.sigterm;

import com.sigterm.gui.HudOverlay;
import com.sigterm.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class SigTermClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModuleManager.INSTANCE.init();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> ModuleManager.INSTANCE.onTick());
        HudRenderCallback.EVENT.register(HudOverlay::render);
        System.out.println("[SigTerm] Client loaded — RIGHT SHIFT to open GUI (rebindable)");
    }
}
