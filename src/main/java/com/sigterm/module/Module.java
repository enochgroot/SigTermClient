package com.sigterm.module;

import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    private boolean enabled;
    private int keyBind;
    public boolean wasKeyDown;
    protected final List<Setting> settings = new ArrayList<>();

    public Module(String name, String desc, Category cat, int key) {
        this.name = name; this.description = desc;
        this.category = cat; this.keyBind = key;
    }

    public void toggle() { enabled = !enabled; if (enabled) onEnable(); else onDisable(); }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int k) { keyBind = k; }
    public List<Setting> getSettings() { return settings; }
    protected Minecraft mc() { return Minecraft.getInstance(); }

    protected Setting addSetting(String name, double value, double min, double max, double step, String suffix) {
        Setting s = new Setting(name, value, min, max, step, suffix);
        settings.add(s);
        return s;
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
