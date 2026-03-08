package dev.iievietskyi.coreclans.papi;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.Player;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.BountyContract;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.ClanRole;
import dev.iievietskyi.coreclans.model.WarSession;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class CoreClansPlaceholderExpansion extends PlaceholderExpansion {
    private final CoreClansPlugin plugin;

    public CoreClansPlaceholderExpansion(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "coreclans";
    }

    @Override
    public String getAuthor() {
        return "IIevietskyi";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        ClanData clan = plugin.getClanService().getClan(player);
        WarSession war = clan == null ? null : plugin.getWarService().getWarByClan(clan.id);

        String key = params.toLowerCase(Locale.ROOT);

        if ("has_clan".equals(key)) {
            return String.valueOf(clan != null);
        }
        if ("clan_name".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : clan.name;
        }
        if ("clan_tag".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : clan.tag;
        }
        if ("clan_tag_colored".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : dev.iievietskyi.coreclans.service.Texts.color("&b[" + clan.tag + "]");
        }
        if ("clan_role".equals(key)) {
            if (clan == null) {
                return plugin.getCoreConfig().placeholderNoClan;
            }
            ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
            return role == null ? plugin.getCoreConfig().placeholderNoClan : role.name().toLowerCase(Locale.ROOT);
        }
        if ("clan_members_online".equals(key)) {
            return clan == null ? "0" : String.valueOf(plugin.getClanService().getOnlineMemberCount(clan));
        }
        if ("clan_members_total".equals(key)) {
            return clan == null ? "0" : String.valueOf(clan.members.size());
        }
        if ("clan_bank".equals(key)) {
            return clan == null ? "0.00" : fmt(clan.bank);
        }
        if ("clan_points".equals(key)) {
            return clan == null ? "0" : String.valueOf(plugin.getSeasonService().getPoints(clan.id));
        }
        if ("clan_division".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : plugin.getSeasonService().getDivision(clan.id).name().toLowerCase(Locale.ROOT);
        }
        if ("clan_season_rank".equals(key)) {
            return clan == null ? "-" : String.valueOf(plugin.getSeasonService().rankOf(clan.id));
        }

        if ("tab_tag".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : "[" + clan.tag + "]";
        }
        if ("tab_name".equals(key)) {
            return clan == null ? player.getName() : "[" + clan.tag + "] " + player.getName();
        }
        if ("tab_division".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : plugin.getSeasonService().getDivision(clan.id).name().toLowerCase(Locale.ROOT);
        }
        if ("tab_war_status".equals(key)) {
            return war == null ? "peace" : war.status.name().toLowerCase(Locale.ROOT);
        }

        if ("war_active".equals(key)) {
            return String.valueOf(war != null);
        }
        if ("war_mode".equals(key)) {
            return war == null ? plugin.getCoreConfig().placeholderNoWar : war.mode.name().toLowerCase(Locale.ROOT);
        }
        if ("war_side".equals(key)) {
            if (war == null || clan == null) {
                return plugin.getCoreConfig().placeholderNoWar;
            }
            return clan.id.equals(war.attackerClanId) ? "attacker" : "defender";
        }
        if ("war_enemy".equals(key)) {
            if (war == null || clan == null) {
                return plugin.getCoreConfig().placeholderNoWar;
            }
            String enemyId = clan.id.equals(war.attackerClanId) ? war.defenderClanId : war.attackerClanId;
            ClanData enemy = plugin.getClanService().getClanById(enemyId);
            return enemy == null ? plugin.getCoreConfig().placeholderNoWar : enemy.name;
        }
        if ("war_time_left".equals(key)) {
            return war == null ? "0" : String.valueOf(Math.max(0L, (war.endsAt - System.currentTimeMillis()) / 1000L));
        }
        if ("war_queue_position".equals(key)) {
            return war == null ? "-1" : String.valueOf(plugin.getWarService().getQueuePosition(war, player.getUniqueId()));
        }
        if ("war_queue_eta".equals(key)) {
            if (war == null) {
                return "0";
            }
            int pos = plugin.getWarService().getQueuePosition(war, player.getUniqueId());
            return pos <= 0 ? "0" : String.valueOf(pos * 20);
        }
        if ("war_attackers_cap".equals(key)) {
            return war == null ? "0" : String.valueOf(war.attackersCap);
        }
        if ("war_attackers_active".equals(key)) {
            return war == null ? "0" : String.valueOf(war.activeAttackers.size());
        }
        if ("war_capture_progress".equals(key)) {
            if (war == null) {
                return "0";
            }
            if (war.captureTargetSeconds <= 0) {
                return "0";
            }
            return String.valueOf((int) Math.min(100, (war.captureProgressSeconds * 100.0D) / war.captureTargetSeconds));
        }
        if ("war_crystal_hp".equals(key)) {
            return war == null ? "0" : fmt(war.crystalHp);
        }
        if ("war_crystal_hp_percent".equals(key)) {
            if (war == null || war.crystalMaxHp <= 0.0D) {
                return "0";
            }
            return String.valueOf((int) Math.max(0, Math.min(100, (war.crystalHp * 100.0D) / war.crystalMaxHp)));
        }

        if ("bounty_active".equals(key)) {
            return String.valueOf(clan != null && plugin.getBountyService().firstActiveForTarget(clan.id) != null);
        }
        if ("bounty_target_clan".equals(key)) {
            if (clan == null) {
                return plugin.getCoreConfig().placeholderNoClan;
            }
            BountyContract bounty = plugin.getBountyService().firstActiveForTarget(clan.id);
            if (bounty == null) {
                return plugin.getCoreConfig().placeholderNoWar;
            }
            ClanData issuer = plugin.getClanService().getClanById(bounty.issuerClanId);
            return issuer == null ? plugin.getCoreConfig().placeholderNoWar : issuer.name;
        }
        if ("bounty_reward".equals(key)) {
            if (clan == null) {
                return "0.00";
            }
            BountyContract bounty = plugin.getBountyService().firstActiveForTarget(clan.id);
            return bounty == null ? "0.00" : fmt(bounty.reward);
        }
        if ("bounty_time_left".equals(key)) {
            if (clan == null) {
                return "0";
            }
            BountyContract bounty = plugin.getBountyService().firstActiveForTarget(clan.id);
            return bounty == null ? "0" : String.valueOf(Math.max(0L, (bounty.expiresAt - System.currentTimeMillis()) / 1000L));
        }
        if ("bounty_progress".equals(key)) {
            if (clan == null) {
                return "0/0";
            }
            BountyContract bounty = plugin.getBountyService().firstActiveForTarget(clan.id);
            return bounty == null ? "0/0" : (bounty.progress + "/" + bounty.target);
        }

        if ("season_name".equals(key)) {
            return plugin.getStorage().data().season.name == null ? plugin.getCoreConfig().seasonName : plugin.getStorage().data().season.name;
        }
        if ("season_days_left".equals(key)) {
            return String.valueOf(plugin.getSeasonService().daysLeft());
        }
        if ("season_points".equals(key)) {
            return clan == null ? "0" : String.valueOf(plugin.getSeasonService().getPoints(clan.id));
        }
        if ("season_division".equals(key)) {
            return clan == null ? plugin.getCoreConfig().placeholderNoClan : plugin.getSeasonService().getDivision(clan.id).name().toLowerCase(Locale.ROOT);
        }

        if (key.startsWith("top_")) {
            String[] split = key.split("_");
            if (split.length == 3) {
                int pos;
                try {
                    pos = Integer.parseInt(split[1]);
                } catch (NumberFormatException ex) {
                    return "";
                }
                List<Map.Entry<String, Integer>> top = plugin.getSeasonService().top(Math.max(3, pos));
                if (pos <= 0 || pos > top.size()) {
                    return "";
                }
                Map.Entry<String, Integer> entry = top.get(pos - 1);
                if ("name".equals(split[2])) {
                    ClanData topClan = plugin.getClanService().getClanById(entry.getKey());
                    return topClan == null ? "" : topClan.name;
                }
                if ("points".equals(split[2])) {
                    return String.valueOf(entry.getValue());
                }
            }
        }

        return null;
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.2f", value);
    }
}
