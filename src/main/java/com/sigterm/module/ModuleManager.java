package com.sigterm.module;

import com.sigterm.module.combat.*;
import com.sigterm.module.movement.*;
import com.sigterm.module.render.*;
import com.sigterm.module.player.*;
import com.sigterm.config.SigTermConfig;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import java.lang.reflect.Method;
import java.util.*;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();
    private final Set<String> favorites = new HashSet<>();
    private long cachedWindow = 0;
    private int guiKeyBind = GLFW.GLFW_KEY_RIGHT_SHIFT;

    public int getGuiKeyBind() { return guiKeyBind; }
    public void setGuiKeyBind(int k) { guiKeyBind = k; }

    public Set<String> getFavorites() { return favorites; }
    public void toggleFavorite(String name) {
        if (favorites.contains(name)) favorites.remove(name);
        else favorites.add(name);
    }

    public void init() {
        // Combat
        register(new KillAura());
        register(new AutoTotem());
        register(new MaceDamage());
        register(new SpearDamage());
        register(new AutoCrystal());
        register(new Reach());
        register(new AimAssist());
        register(new TriggerBot());
        register(new Clutch());
        
        // Movement
        register(new Speed());
        register(new Fly());
        register(new NoFall());
        register(new Sprint());
        register(new BHop());
        register(new AirJump());
        register(new BoatFly());
        register(new FakeLag());
        
        // Render
        register(new Fullbright());
        register(new Tracers());
        register(new FreeCam());
        register(new BlockESP());
        register(new MobESP());
        register(new Xray());
        
        // Player
        register(new AutoRespawn());
        register(new FastPlace());
        register(new NoSlow());
        register(new AutoRefill());
        register(new AirPlace());
        register(new KnockbackDelay());
        
        SigTermConfig.load();
    }

    private void register(Module m) { modules.add(m); }

    private long getWindowHandle() {
        if (cachedWindow != 0) return cachedWindow;
        try {
            var window = Minecraft.getInstance().getWindow();
            for (String name : new String[]{"getWindow", "getHandle", "getScreenId", "window"}) {
                try {
                    Method m = window.getClass().getMethod(name);
                    Object result = m.invoke(window);
                    if (result instanceof Long l && l != 0) { cachedWindow = l; return l; }
                } catch (NoSuchMethodException ignored) {}
            }
            for (var f : window.getClass().getDeclaredFields()) {
                if (f.getType() == long.class) {
                    f.setAccessible(true);
                    long val = f.getLong(window);
                    if (val != 0) { cachedWindow = val; return val; }
                }
            }
        } catch (Exception ignored) {}
        return 0;
    }

    private boolean wasGuiKeyDown = false;

    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        long window = getWindowHandle();
        if (window == 0) return;

        boolean guiDown = GLFW.glfwGetKey(window, guiKeyBind) == GLFW.GLFW_PRESS;
        if (guiDown && !wasGuiKeyDown && mc.screen == null)
            mc.setScreen(new com.sigterm.gui.ClickGui());
        wasGuiKeyDown = guiDown;

        if (mc.screen != null) return;
        for (Module m : modules) {
            if (m.getKeyBind() != 0) {
                boolean down = GLFW.glfwGetKey(window, m.getKeyBind()) == GLFW.GLFW_PRESS;
                if (down && !m.wasKeyDown) m.toggle();
                m.wasKeyDown = down;
            }
        }
        for (Module m : modules) {
            if (m.isEnabled()) { try { m.onTick(); } catch (Exception ignored) {} }
        }
    }

    public List<Module> getModules() { return Collections.unmodifiableList(modules); }
    
    public List<Module> getByCategory(Category c) {
        List<Module> r = new ArrayList<>();
        for (Module m : modules) {
            if (c == Category.FAVORITES) {
                if (favorites.contains(m.name)) r.add(m);
            } else if (m.category == c) {
                r.add(m);
            }
        }
        return r;
    }
}
