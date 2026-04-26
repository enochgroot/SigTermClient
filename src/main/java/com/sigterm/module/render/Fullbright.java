package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import org.lwjgl.glfw.GLFW;

public class Fullbright extends Module {
    private double prevGamma;

    public Fullbright() {
        super("Fullbright", "Max gamma — see in the dark", Category.RENDER, GLFW.GLFW_KEY_B);
    }

    @Override
    public void onEnable() {
        prevGamma = mc().options.gamma().get();
        mc().options.gamma().set(16.0);
    }

    @Override
    public void onDisable() {
        mc().options.gamma().set(prevGamma);
    }
}
