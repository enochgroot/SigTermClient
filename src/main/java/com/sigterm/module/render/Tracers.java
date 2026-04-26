package com.sigterm.module.render;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.Setting;
import org.lwjgl.glfw.GLFW;

public class Tracers extends Module {
    public final Setting range;
    public final Setting players;
    public final Setting mobs;
    public final Setting items;

    public Tracers() {
        super("Tracers", "Lines from crosshair to entities", Category.RENDER, 0);
        range = addSetting("Range", 64, 16, 256, 16, "m");
        players = addSetting("Players", 1, 0, 1, 1, "");
        mobs = addSetting("Mobs", 1, 0, 1, 1, "");
        items = addSetting("Items", 0, 0, 1, 1, "");
    }

    // Rendering happens in TracerRenderer via HudRenderCallback
}
