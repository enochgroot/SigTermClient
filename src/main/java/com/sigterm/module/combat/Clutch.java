package com.sigterm.module.combat;

import com.sigterm.module.Category;
import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.Setting;
import org.lwjgl.glfw.GLFW;

public class Clutch extends Module {
    private final Setting healthThreshold;
    private boolean wasActive = false;

    // Modules to disable when clutching (aggressive modules)
    private static final String[] DISABLE_ON_CLUTCH = {"Speed", "Fly", "KillAura"};
    // Modules to enable when clutching (defensive modules)  
    private static final String[] ENABLE_ON_CLUTCH = {"Sprint", "NoFall"};

    public Clutch() {
        super("Clutch", "Auto-enable defensive modules when low HP", Category.COMBAT, 0);
        healthThreshold = addSetting("Health", 10, 1, 20, 1, " hearts");
    }

    @Override
    public void onTick() {
        if (mc().player == null) return;

        int health = mc().player.getHealth();
        int threshold = (int) healthThreshold.value;
        boolean shouldClutch = health <= threshold;

        if (shouldClutch && !wasActive) {
            // Just entered clutch mode — disable aggressive, enable defensive
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                for (String name : DISABLE_ON_CLUTCH) {
                    if (m.name.equals(name) && m.isEnabled()) m.toggle();
                }
                for (String name : ENABLE_ON_CLUTCH) {
                    if (m.name.equals(name) && !m.isEnabled()) m.toggle();
                }
            }
            wasActive = true;
        } else if (!shouldClutch && wasActive) {
            // Left clutch mode — could restore previous state, but keeping it simple
            wasActive = false;
        }
    }

    @Override
    public void onDisable() {
        wasActive = false;
    }
}
