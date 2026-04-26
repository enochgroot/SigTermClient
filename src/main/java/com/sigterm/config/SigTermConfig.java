package com.sigterm.config;

import com.sigterm.module.Module;
import com.sigterm.module.ModuleManager;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class SigTermConfig {
    private static final Path CONFIG_DIR = getConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("sigtermclient.properties");

    private static Path getConfigDir() {
        try {
            return Minecraft.getInstance().gameDirectory.toPath().resolve("config");
        } catch (Exception e) {
            return Path.of("config");
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);
            Properties props = new Properties();
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                props.setProperty("bind." + m.name, String.valueOf(m.getKeyBind()));
                props.setProperty("enabled." + m.name, String.valueOf(m.isEnabled()));
            }
            props.setProperty("gui.keybind", String.valueOf(ModuleManager.INSTANCE.getGuiKeyBind()));
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
            try (InputStream is = Files.newInputStream(CONFIG_FILE)) {
                props.load(is);
            }
            for (Module m : ModuleManager.INSTANCE.getModules()) {
                String bindKey = "bind." + m.name;
                if (props.containsKey(bindKey)) {
                    m.setKeyBind(Integer.parseInt(props.getProperty(bindKey)));
                }
                String enabledKey = "enabled." + m.name;
                if (props.containsKey(enabledKey)) {
                    boolean shouldBeEnabled = Boolean.parseBoolean(props.getProperty(enabledKey));
                    if (shouldBeEnabled && !m.isEnabled()) m.toggle();
                }
            }
            String guiKey = props.getProperty("gui.keybind");
            if (guiKey != null) {
                ModuleManager.INSTANCE.setGuiKeyBind(Integer.parseInt(guiKey));
            }
        } catch (Exception e) {
            System.err.println("[SigTerm] Config load failed: " + e);
        }
    }
}
