package com.sigterm.module;

import net.minecraft.client.Minecraft;

public abstract class Module {
    public final String name;
    public final String description;
    public final Category category;
    private boolean enabled;
    private int keyBind;
    public boolean wasKeyDown;

    public Module(String name, String desc, Category cat, int key) {
        this.name = name; this.description = desc;
        this.category = cat; this.keyBind = key;
    }

    public void toggle() { enabled = !enabled; if (enabled) onEnable(); else onDisable(); }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int k) { keyBind = k; }
    protected Minecraft mc() { return Minecraft.getInstance(); }
    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
