package dev.iievietskyi.coreclans.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import dev.iievietskyi.coreclans.CoreClansPlugin;

public class MenuListener implements Listener {
    private final CoreClansPlugin plugin;

    public MenuListener(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        plugin.getMenuService().handleClick(event);
    }
}