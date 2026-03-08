package dev.iievietskyi.coreclans.config;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import dev.iievietskyi.coreclans.service.Texts;

public class Messages {
    private final JavaPlugin plugin;
    private FileConfiguration data;

    public Messages(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        this.data = YamlConfiguration.loadConfiguration(file);
    }

    public String get(String path) {
        return Texts.color(data.getString(path, path));
    }

    public String get(String path, Map<String, String> replacements) {
        return Texts.format(data.getString(path, path), replacements == null ? Collections.<String, String>emptyMap() : replacements);
    }
}
