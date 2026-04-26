package com.sigterm;

import com.sigterm.gui.ClickGui;
import com.sigterm.gui.HudOverlay;
import com.sigterm.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class SigTermClient implements ClientModInitializer {
    public static KeyMapping guiKey;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(
            new KeyMapping("key.sigterm.gui", GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC)
        );
        ModuleManager.INSTANCE.init();
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.consumeClick()) {
                if (mc.level != null) mc.setScreen(new ClickGui());
            }
            ModuleManager.INSTANCE.onTick();
        });
        HudRenderCallback.EVENT.register(HudOverlay::render);
        System.out.println("[SigTerm] Client loaded — RIGHT SHIFT to open GUI");
    }
}
