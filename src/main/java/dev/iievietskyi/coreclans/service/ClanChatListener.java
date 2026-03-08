package dev.iievietskyi.coreclans.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;

public class ClanChatListener implements Listener {
    private final CoreClansPlugin plugin;

    public ClanChatListener(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            return;
        }

        boolean forcedClan = plugin.getClanService().isClanChatEnabled(player.getUniqueId());
        String msg = event.getMessage();
        boolean explicit = msg.startsWith("!");

        if (!forcedClan && !explicit) {
            return;
        }

        event.setCancelled(true);
        if (explicit) {
            msg = msg.substring(1).trim();
        }
        final String message = Texts.color("&8[Clan] &b" + player.getName() + "&7: &f" + msg);

        List<Player> recipients = new ArrayList<Player>();
        for (String memberId : clan.members.keySet()) {
            Player target = Bukkit.getPlayer(UUID.fromString(memberId));
            if (target != null && target.isOnline()) {
                recipients.add(target);
            }
        }

        for (Player recipient : recipients) {
            recipient.sendMessage(message);
        }
    }
}
