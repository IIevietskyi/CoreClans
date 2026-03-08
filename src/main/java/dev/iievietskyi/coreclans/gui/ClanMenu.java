package dev.iievietskyi.coreclans.gui;

import org.bukkit.entity.Player;

import dev.iievietskyi.coreclans.CoreClansPlugin;

public final class ClanMenu {
    private ClanMenu() {
    }

    public static void openMain(CoreClansPlugin plugin, Player player) {
        if (!plugin.getMenuService().openMenu(player, "main")) {
            player.sendMessage(plugin.prefixed("&cMain menu is not configured."));
        }
    }
}