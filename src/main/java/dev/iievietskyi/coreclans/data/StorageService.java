package dev.iievietskyi.coreclans.data;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.CoreDataStore;

public class StorageService {
    private final CoreClansPlugin plugin;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private File file;
    private CoreDataStore store;
    private BukkitTask autosaveTask;

    public StorageService(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        this.file = new File(plugin.getDataFolder(), plugin.getCoreConfig().storageFile);
        if (!file.exists()) {
            store = new CoreDataStore();
            saveNow();
        } else {
            try (FileReader reader = new FileReader(file)) {
                store = gson.fromJson(reader, CoreDataStore.class);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to read storage file, creating fresh store: " + ex.getMessage());
                store = new CoreDataStore();
            }
        }
        if (store == null) {
            store = new CoreDataStore();
        }
        startAutosave();
    }

    public CoreDataStore data() {
        return store;
    }

    public void saveNow() {
        if (file == null || store == null) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(store, writer);
        } catch (IOException ex) {
            plugin.getLogger().warning("Failed to save storage: " + ex.getMessage());
        }
    }

    public void shutdown() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
            autosaveTask = null;
        }
        saveNow();
    }

    private void startAutosave() {
        if (autosaveTask != null) {
            autosaveTask.cancel();
        }
        long periodTicks = plugin.getCoreConfig().autosaveSeconds * 20L;
        autosaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveNow, periodTicks, periodTicks);
    }
}
