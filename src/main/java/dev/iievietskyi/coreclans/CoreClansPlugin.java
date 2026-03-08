package dev.iievietskyi.coreclans;

import java.util.Collections;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import dev.iievietskyi.coreclans.command.ClanAdminCommand;
import dev.iievietskyi.coreclans.command.ClanCommand;
import dev.iievietskyi.coreclans.config.CoreConfig;
import dev.iievietskyi.coreclans.config.Messages;
import dev.iievietskyi.coreclans.data.StorageService;
import dev.iievietskyi.coreclans.gui.MenuListener;
import dev.iievietskyi.coreclans.gui.MenuService;
import dev.iievietskyi.coreclans.papi.CoreClansPlaceholderExpansion;
import dev.iievietskyi.coreclans.service.BountyService;
import dev.iievietskyi.coreclans.service.ClanChatListener;
import dev.iievietskyi.coreclans.service.ClanService;
import dev.iievietskyi.coreclans.service.EconomyHook;
import dev.iievietskyi.coreclans.service.SeasonService;
import dev.iievietskyi.coreclans.service.SiegeWindowService;
import dev.iievietskyi.coreclans.service.Texts;
import dev.iievietskyi.coreclans.service.WarCombatListener;
import dev.iievietskyi.coreclans.service.WarService;

public class CoreClansPlugin extends JavaPlugin {
    private CoreConfig coreConfig;
    private Messages messages;
    private StorageService storage;
    private ClanService clanService;
    private SiegeWindowService siegeWindowService;
    private SeasonService seasonService;
    private BountyService bountyService;
    private WarService warService;
    private EconomyHook economyHook;
    private MenuService menuService;

    @Override
    public void onEnable() {
        reloadAll();

        registerCommands();
        registerListener(new ClanChatListener(this));
        registerListener(new WarCombatListener(this));
        registerListener(new MenuListener(this));

        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            new CoreClansPlaceholderExpansion(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("CoreClans enabled.");
    }

    @Override
    public void onDisable() {
        if (warService != null) {
            warService.shutdown();
        }
        if (storage != null) {
            storage.shutdown();
        }
    }

    public void reloadAll() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();
        reloadConfig();

        FileConfiguration cfg = getConfig();
        this.coreConfig = CoreConfig.from(cfg);

        this.messages = new Messages(this);
        messages.load();

        this.menuService = new MenuService(this);
        menuService.load();

        if (storage != null) {
            storage.shutdown();
        }
        this.storage = new StorageService(this);
        storage.load();

        this.clanService = new ClanService(this);
        this.siegeWindowService = new SiegeWindowService(this);
        this.seasonService = new SeasonService(this);
        this.bountyService = new BountyService(this);

        if (warService != null) {
            warService.shutdown();
        }
        this.warService = new WarService(this);

        this.economyHook = new EconomyHook(this);
        boolean economyReady = this.economyHook.setup();
        if (economyReady) {
            getLogger().info("Vault economy hook enabled.");
        } else {
            getLogger().warning("Vault economy hook not found. Clan bank deposit/withdraw to player wallet is disabled.");
        }
    }

    private void registerCommands() {
        ClanCommand clanCommand = new ClanCommand(this);
        PluginCommand clan = getCommand("clan");
        if (clan != null) {
            clan.setExecutor(clanCommand);
            clan.setTabCompleter(clanCommand);
        }

        ClanAdminCommand adminCommand = new ClanAdminCommand(this);
        PluginCommand clanAdmin = getCommand("clanadmin");
        if (clanAdmin != null) {
            clanAdmin.setExecutor(adminCommand);
            clanAdmin.setTabCompleter(adminCommand);
        }
    }

    private void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this);
    }

    public String prefixed(String raw) {
        return Texts.color(coreConfig.prefix + raw);
    }

    public String msg(String key) {
        return prefixed(messages.get(key));
    }

    public String msg(String key, Map<String, String> replacements) {
        Map<String, String> safe = replacements == null ? Collections.<String, String>emptyMap() : replacements;
        return prefixed(messages.get(key, safe));
    }

    public CoreConfig getCoreConfig() {
        return coreConfig;
    }

    public Messages getMessages() {
        return messages;
    }

    public StorageService getStorage() {
        return storage;
    }

    public ClanService getClanService() {
        return clanService;
    }

    public SiegeWindowService getSiegeWindowService() {
        return siegeWindowService;
    }

    public SeasonService getSeasonService() {
        return seasonService;
    }

    public BountyService getBountyService() {
        return bountyService;
    }

    public WarService getWarService() {
        return warService;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public MenuService getMenuService() {
        return menuService;
    }
}