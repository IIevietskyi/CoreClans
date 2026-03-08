package dev.iievietskyi.coreclans.service;

import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.WarSession;

public class WarCombatListener implements Listener {
    private final CoreClansPlugin plugin;

    public WarCombatListener(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCrystalDamage(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof EnderCrystal)) {
            return;
        }

        WarSession session = plugin.getWarService().getWarByCrystalEntity(entity);
        if (session == null) {
            return;
        }

        Player attacker = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        if (attacker == null) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        double done = plugin.getWarService().applyCrystalDamage(session, attacker, event.getDamage());
        attacker.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, net.md_5.bungee.api.chat.TextComponent.fromLegacyText(Texts.color("&dCrystal HP: &f" + String.format(java.util.Locale.US, "%.1f", session.crystalHp) + " / " + String.format(java.util.Locale.US, "%.1f", session.crystalMaxHp) + " &7(-" + String.format(java.util.Locale.US, "%.1f", done) + ")")));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }
        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        double adjusted = plugin.getWarService().applyUnderdogDamageModifier(attacker, victim, event.getDamage());
        event.setDamage(adjusted);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCrystalExplode(EntityExplodeEvent event) {
        if (event.getEntity() instanceof EnderCrystal) {
            WarSession session = plugin.getWarService().getWarByCrystalEntity(event.getEntity());
            if (session != null) {
                event.blockList().clear();
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) {
            return;
        }

        WarSession victimWar = plugin.getWarService().getWarByPlayer(victim);
        WarSession killerWar = plugin.getWarService().getWarByPlayer(killer);
        if (victimWar != null && victimWar == killerWar) {
            plugin.getWarService().recordKill(victimWar, killer);
        }
    }
}
