package com.sigterm.config;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import com.sigterm.module.Setting;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SigTermConfig {
    private static final Path CONFIG_DIR = getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("sigtermclient.properties");

    private static Path getConfigDir() {
        try { return Minecraft.getInstance().gameDirectory.toPath().resolve("config"); }
        catch (Exception e) { return Path.of("config"); }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                props.setProperty("bind." + m.name, String.valueOf(m.getKeyBind()));
                props.setProperty("enabled." + m.name, String.valueOf(m.isEnabled()));
                for (Setting s : m.getSettings()) {
                    props.setProperty("setting." + m.name + "." + s.name, String.valueOf(s.value));
                }
            }
            props.setProperty("gui.keybind", String.valueOf(ModuleManager.INSTANCE.getGuiKeyBind()));
            
            // Save favorites
            String favs = String.join(",", ModuleManager.INSTANCE.getFavorites());
            props.setProperty("favorites", favs);
            
            try (OutputStream os = Files.newOutputStream(CONFIG_FILE)) {
                props.store(os, "SigTerm Client Config");
            }
        } catch (Exception e) {
            System.err.println("[SigTerm] Config save failed: " + e);
        }
    }

    public static void load() {
        if (!Files.exists(CONFIG_FILE)) return;
        try {
            Properties props = new Properties();
            try (InputStream is = Files.newInputStream(CONFIG_FILE)) { props.load(is); }
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                String bk = "bind." + m.name;
                if (props.containsKey(bk)) m.setKeyBind(Integer.parseInt(props.getProperty(bk)));
                String ek = "enabled." + m.name;
                if (props.containsKey(ek) && Boolean.parseBoolean(props.getProperty(ek)) && !m.isEnabled())
                    m.toggle();
                for (Setting s : m.getSettings()) {
                    String sk = "setting." + m.name + "." + s.name;
                    if (props.containsKey(sk)) s.value = Double.parseDouble(props.getProperty(sk));
                }
            }
            String gk = props.getProperty("gui.keybind");
            if (gk != null) ModuleManager.INSTANCE.setGuiKeyBind(Integer.parseInt(gk));
            
            // Load favorites
            String favs = props.getProperty("favorites");
            if (favs != null && !favs.isEmpty()) {
                for (String name : favs.split(",")) {
                    ModuleManager.INSTANCE.getFavorites().add(name.trim());
                }
            }
        } catch (Exception e) {
            System.err.println("[SigTerm] Config load failed: " + e);
        }
    }
}
