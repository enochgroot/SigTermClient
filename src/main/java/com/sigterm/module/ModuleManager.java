package com.sigterm.module;

import com.sigterm.module.combat.*;
import com.sigterm.module.movement.*;
import com.sigterm.module.render.*;
import com.sigterm.module.player.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import java.util.*;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();

    public void init() {
        register(new KillAura());
        register(new AutoTotem());
        register(new MaceDamage());
        register(new SpearDamage());
        register(new AutoCrystal());
        register(new Speed());
        register(new Fly());
        register(new NoFall());
        register(new Sprint());
        register(new Fullbright());
        register(new AutoRespawn());
        register(new FastPlace());
        register(new NoSlow());
    }

    private void register(Module m) { modules.add(m); }

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return; // don't process keybinds while GUI open

        long window = 0;
        try { window = mc.getWindow().getScreenId(); } catch (Throwable t) {
            try { window = mc.getWindow().getHandle(); } catch (Throwable t2) {
                try {
                    // Fallback: use reflection
                    var m = mc.getWindow().getClass().getMethod("getWindow");
                    window = (long) m.invoke(mc.getWindow());
                } catch (Throwable ignored) { return; }
            }
        }
        if (window == 0) return;

        for (Module m : modules) {
            if (m.getKeyBind() != 0) {
                boolean down = GLFW.glfwGetKey(window, m.getKeyBind()) == GLFW.GLFW_PRESS;
                if (down && !m.wasKeyDown) m.toggle();
                m.wasKeyDown = down;
            }
        }
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onTick(); } catch (Exception ignored) {}
            }
        }
    }

    public List<Module> getModules() { return Collections.unmodifiableList(modules); }
    public List<Module> getByCategory(Category c) {
        List<Module> r = new ArrayList<>();
        for (Module m : modules) if (m.category == c) r.add(m);
        return r;
    }
}
