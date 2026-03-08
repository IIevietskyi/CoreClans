package dev.iievietskyi.coreclans.command;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.BountyContract;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.ClanRole;
import dev.iievietskyi.coreclans.model.ObjectiveType;
import dev.iievietskyi.coreclans.model.WarMode;
import dev.iievietskyi.coreclans.model.WarReplay;
import dev.iievietskyi.coreclans.model.WarRequest;
import dev.iievietskyi.coreclans.model.WarSession;

public class ClanCommand implements CommandExecutor, TabCompleter {
    private final CoreClansPlugin plugin;

    public ClanCommand(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.msg("common.player-only"));
            return true;
        }
        Player player = (Player) sender;

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "menu":
                return handleMenu(player, args);
            case "create":
                return handleCreate(player, args);
            case "disband":
                return handleDisband(player, args);
            case "info":
                return handleInfo(player, args);
            case "list":
                return handleList(player);
            case "top":
                return handleTop(player);
            case "online":
                return handleOnline(player);
            case "invite":
                return handleInvite(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "kick":
                return handleKick(player, args);
            case "promote":
                return handlePromote(player, args);
            case "demote":
                return handleDemote(player, args);
            case "owner":
                return handleOwner(player, args);
            case "chat":
                return handleChat(player, args);
            case "sethome":
                return handleSetHome(player);
            case "home":
                return handleHome(player);
            case "claim":
                return handleClaim(player, args);
            case "bank":
                return handleBank(player, args);
            case "ally":
            case "enemy":
            case "neutral":
                return handleDiplomacy(player, sub, args);
            case "objective":
                return handleObjective(player, args);
            case "war":
                return handleWar(player, args);
            case "siege":
                return handleSiege(player, args);
            case "bounty":
                return handleBounty(player, args);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleMenu(Player player, String[] args) {
        String menuId = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "main";
        if ("admin".equals(menuId) && !player.hasPermission("coreclans.admin")) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        if (!plugin.getMenuService().openMenu(player, menuId)) {
            player.sendMessage(plugin.prefixed("&cMenu not found: " + menuId));
        }
        return true;
    }
    private boolean handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan create <name>"));
            return true;
        }
        ClanData clan = plugin.getClanService().createClan(player, args[1]);
        if (clan == null) {
            player.sendMessage(plugin.prefixed("&cUnable to create clan. Check name, uniqueness, or your current clan status."));
            return true;
        }
        player.sendMessage(plugin.msg("clan.created", mapOf("clan", clan.name)));
        return true;
    }

    private boolean handleDisband(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        if (!player.getUniqueId().toString().equals(clan.ownerUuid)) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        if (args.length < 2 || !"confirm".equalsIgnoreCase(args[1])) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan disband confirm"));
            return true;
        }
        plugin.getClanService().disbandClan(clan);
        player.sendMessage(plugin.msg("clan.disbanded", mapOf("clan", clan.name)));
        return true;
    }

    private boolean handleInfo(Player player, String[] args) {
        ClanData clan = args.length >= 2 ? plugin.getClanService().getClanByName(args[1]) : plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }

        int points = plugin.getSeasonService().getPoints(clan.id);
        String division = plugin.getSeasonService().getDivision(points).name();
        int rank = plugin.getSeasonService().rankOf(clan.id);

        player.sendMessage(plugin.prefixed("&6Clan &f" + clan.name + " &7[" + clan.tag + "]"));
        player.sendMessage(plugin.prefixed("&7Members: &f" + clan.members.size() + " &8| &7Online: &f" + plugin.getClanService().getOnlineMemberCount(clan)));
        player.sendMessage(plugin.prefixed("&7Bank: &f" + fmt(clan.bank) + " &8| &7Points: &f" + points + " &8| &7Division: &f" + division));
        player.sendMessage(plugin.prefixed("&7Season Rank: &f" + (rank <= 0 ? "-" : "#" + rank)));
        player.sendMessage(plugin.prefixed("&7Objectives: &fCapture=" + (plugin.getClanService().getObjective(clan, ObjectiveType.CAPTURE) != null)
                + " &8| &fCrystal=" + (plugin.getClanService().getObjective(clan, ObjectiveType.CRYSTAL) != null)));
        return true;
    }

    private boolean handleList(Player player) {
        List<String> names = new ArrayList<String>();
        for (String id : plugin.getClanService().allClanIds()) {
            ClanData clan = plugin.getClanService().getClanById(id);
            if (clan != null) {
                names.add(clan.name + "(" + clan.members.size() + ")");
            }
        }
        Collections.sort(names);
        player.sendMessage(plugin.prefixed("&eClans (&f" + names.size() + "&e): &f" + (names.isEmpty() ? "none" : String.join(", ", names))));
        return true;
    }

    private boolean handleTop(Player player) {
        List<Map.Entry<String, Integer>> top = plugin.getSeasonService().top(10);
        player.sendMessage(plugin.prefixed("&6Top clans:"));
        int i = 1;
        for (Map.Entry<String, Integer> entry : top) {
            ClanData clan = plugin.getClanService().getClanById(entry.getKey());
            if (clan != null) {
                player.sendMessage(plugin.prefixed("&e#" + i + " &f" + clan.name + " &7- &f" + entry.getValue() + " pts"));
                i++;
            }
        }
        if (i == 1) {
            player.sendMessage(plugin.prefixed("&7No ranked clans yet."));
        }
        return true;
    }

    private boolean handleOnline(Player player) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        List<String> names = plugin.getClanService().getOnlineMemberNames(clan);
        player.sendMessage(plugin.msg("clan.online", mapOf(
                "online", String.valueOf(names.size()),
                "total", String.valueOf(clan.members.size()),
                "list", names.isEmpty() ? "none" : String.join(", ", names))));
        return true;
    }

    private boolean handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan invite <player>"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !target.isOnline()) {
            player.sendMessage(plugin.prefixed("&cPlayer not found."));
            return true;
        }
        if (!plugin.getClanService().invitePlayer(clan, player, target)) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        player.sendMessage(plugin.msg("clan.invited", mapOf("player", target.getName())));
        target.sendMessage(plugin.msg("clan.invite-received", mapOf("clan", clan.name)));
        return true;
    }

    private boolean handleJoin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan join <name>"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClanByName(args[1]);
        if (clan == null) {
            player.sendMessage(plugin.prefixed("&cClan not found."));
            return true;
        }
        if (!plugin.getClanService().joinClan(player, clan)) {
            player.sendMessage(plugin.prefixed("&cUnable to join clan. Need invite or slots."));
            return true;
        }
        player.sendMessage(plugin.msg("clan.joined", mapOf("clan", clan.name)));
        return true;
    }

    private boolean handleLeave(Player player) {
        if (!plugin.getClanService().leaveClan(player)) {
            player.sendMessage(plugin.prefixed("&cUnable to leave clan (leader transfer may be required)."));
            return true;
        }
        player.sendMessage(plugin.msg("clan.left"));
        return true;
    }

    private boolean handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan kick <player>"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(plugin.prefixed("&cPlayer not online."));
            return true;
        }
        if (!plugin.getClanService().kickMember(clan, player.getUniqueId(), target.getUniqueId())) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        player.sendMessage(plugin.msg("clan.kicked", mapOf("player", target.getName())));
        target.sendMessage(plugin.prefixed("&cYou were kicked from clan " + clan.name + "."));
        return true;
    }

    private boolean handlePromote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan promote <player>"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(plugin.prefixed("&cPlayer not online."));
            return true;
        }
        ClanRole role = plugin.getClanService().promote(clan, player.getUniqueId(), target.getUniqueId());
        if (role == null) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        player.sendMessage(plugin.msg("clan.promote", mapOf("player", target.getName(), "role", role.name())));
        return true;
    }

    private boolean handleDemote(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan demote <player>"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(plugin.prefixed("&cPlayer not online."));
            return true;
        }
        ClanRole role = plugin.getClanService().demote(clan, player.getUniqueId(), target.getUniqueId());
        if (role == null) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        player.sendMessage(plugin.msg("clan.demote", mapOf("player", target.getName(), "role", role.name())));
        return true;
    }

    private boolean handleOwner(Player player, String[] args) {
        if (args.length < 3 || !"confirm".equalsIgnoreCase(args[2])) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan owner <player> confirm"));
            return true;
        }
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null || !player.getUniqueId().toString().equals(clan.ownerUuid)) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !clan.members.containsKey(target.getUniqueId().toString())) {
            player.sendMessage(plugin.prefixed("&cPlayer must be your clan member and online."));
            return true;
        }
        plugin.getClanService().transferOwnership(clan, target.getUniqueId());
        player.sendMessage(plugin.prefixed("&aOwnership transferred to " + target.getName()));
        return true;
    }

    private boolean handleChat(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }

        if (args.length >= 2 && "toggle".equalsIgnoreCase(args[1])) {
            boolean next = !plugin.getClanService().isClanChatEnabled(player.getUniqueId());
            plugin.getClanService().setClanChat(player.getUniqueId(), next);
            player.sendMessage(plugin.prefixed("&eClan chat: " + (next ? "&aON" : "&cOFF")));
            return true;
        }

        if (args.length >= 2) {
            String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            for (String memberId : clan.members.keySet()) {
                Player target = Bukkit.getPlayer(UUID.fromString(memberId));
                if (target != null && target.isOnline()) {
                    target.sendMessage(plugin.prefixed("&8[Clan] &b" + player.getName() + "&7: &f" + msg));
                }
            }
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan chat <message> OR /clan chat toggle"));
        return true;
    }

    private boolean handleSetHome(Player player) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
        if (role == null || !role.canManageMembers()) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        plugin.getClanService().setHome(clan, player.getLocation());
        player.sendMessage(plugin.msg("clan.sethome"));
        return true;
    }

    private boolean handleHome(Player player) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        Location home = plugin.getClanService().getHome(clan);
        if (home == null) {
            player.sendMessage(plugin.msg("clan.home-missing"));
            return true;
        }
        player.teleport(home);
        player.sendMessage(plugin.prefixed("&aTeleported to clan home."));
        return true;
    }

    private boolean handleClaim(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
        if (role == null || !role.canManageMembers()) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }

        if (args.length >= 2 && "set".equalsIgnoreCase(args[1])) {
            int min = plugin.getCoreConfig().minClaimRadius;
            int max = plugin.getCoreConfig().maxClaimRadius;
            int radius = clan.claimRadius > 0 ? clan.claimRadius : plugin.getCoreConfig().defaultClaimRadius;

            if (args.length >= 3) {
                try {
                    radius = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    player.sendMessage(plugin.prefixed("&cInvalid radius. Use integer from " + min + " to " + max + "."));
                    return true;
                }
                if (radius < min || radius > max) {
                    player.sendMessage(plugin.prefixed("&cClaim radius must be between " + min + " and " + max + "."));
                    return true;
                }
            }

            clan.claimRadius = radius;
            plugin.getClanService().setClaimCenter(clan, player.getLocation());
            player.sendMessage(plugin.prefixed("&aClaim center updated. Radius: " + clan.claimRadius + " blocks."));
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan claim set [radius]"));
        return true;
    }

    private boolean handleBank(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }

        if (args.length == 1) {
            player.sendMessage(plugin.prefixed("&7Clan bank: &f" + fmt(clan.bank)));
            return true;
        }
        if (args.length < 3) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan bank <deposit|withdraw> <amount>"));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException ex) {
            player.sendMessage(plugin.msg("bank.invalid-amount"));
            return true;
        }
        if (!Double.isFinite(amount) || amount <= 0.0D) {
            player.sendMessage(plugin.msg("bank.invalid-amount"));
            return true;
        }

        dev.iievietskyi.coreclans.service.EconomyHook economy = plugin.getEconomyHook();
        if ("deposit".equalsIgnoreCase(args[1])) {
            if (economy == null || !economy.isAvailable()) {
                player.sendMessage(plugin.msg("bank.vault-required"));
                return true;
            }
            if (!economy.has(player, amount)) {
                player.sendMessage(plugin.msg("bank.player-not-enough"));
                return true;
            }
            if (!economy.withdraw(player, amount)) {
                player.sendMessage(plugin.msg("bank.tx-failed"));
                return true;
            }
            if (!plugin.getClanService().deposit(clan, amount)) {
                economy.deposit(player, amount);
                player.sendMessage(plugin.msg("bank.tx-failed"));
                return true;
            }
            player.sendMessage(plugin.msg("bank.deposit", mapOf("amount", fmt(amount), "bank", fmt(clan.bank))));
            return true;
        }

        if ("withdraw".equalsIgnoreCase(args[1])) {
            ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
            if (role == null || !role.canManageMembers()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (!plugin.getClanService().withdraw(clan, amount)) {
                player.sendMessage(plugin.msg("bank.not-enough"));
                return true;
            }
            if (economy == null || !economy.isAvailable()) {
                plugin.getClanService().deposit(clan, amount);
                player.sendMessage(plugin.msg("bank.vault-required"));
                return true;
            }
            if (!economy.deposit(player, amount)) {
                plugin.getClanService().deposit(clan, amount);
                player.sendMessage(plugin.msg("bank.tx-failed"));
                return true;
            }
            player.sendMessage(plugin.msg("bank.withdraw", mapOf("amount", fmt(amount), "bank", fmt(clan.bank))));
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan bank <deposit|withdraw> <amount>"));
        return true;
    }

    private boolean handleDiplomacy(Player player, String type, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
        if (role == null || !role.canManageMembers()) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan " + type + " <clan>"));
            return true;
        }
        ClanData target = plugin.getClanService().getClanByName(args[1]);
        if (target == null) {
            player.sendMessage(plugin.prefixed("&cClan not found."));
            return true;
        }

        String relation = "neutral";
        if ("ally".equals(type)) {
            relation = "ally";
        } else if ("enemy".equals(type)) {
            relation = "enemy";
        }
        plugin.getClanService().setRelation(clan, target, relation);
        player.sendMessage(plugin.prefixed("&aDiplomacy updated: " + clan.name + " -> " + target.name + " = " + relation));
        return true;
    }

    private boolean handleObjective(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan objective <set|remove|validate|preview|list> ..."));
            return true;
        }

        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
        if (role == null || !role.canManageWar()) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if ("set".equals(action)) {
            if (args.length < 3) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan objective set <capture|beacon|crystal>"));
                return true;
            }
            ObjectiveType type = ObjectiveType.fromInput(args[2]);
            if (type == null) {
                player.sendMessage(plugin.prefixed("&cAllowed objective types: capture, beacon, crystal"));
                return true;
            }
            String reason = plugin.getClanService().setObjective(clan, type, player.getLocation());
            if (reason != null) {
                player.sendMessage(plugin.msg("objective.invalid", mapOf("reason", reason)));
                return true;
            }
            player.sendMessage(plugin.msg("objective.set", mapOf("type", type.name().toLowerCase(Locale.ROOT))));
            return true;
        }

        if ("remove".equals(action)) {
            if (args.length < 3) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan objective remove <capture|beacon|crystal>"));
                return true;
            }
            ObjectiveType type = ObjectiveType.fromInput(args[2]);
            if (type == null) {
                player.sendMessage(plugin.prefixed("&cUnknown objective type."));
                return true;
            }
            if (plugin.getClanService().removeObjective(clan, type)) {
                player.sendMessage(plugin.msg("objective.removed", mapOf("type", type.name().toLowerCase(Locale.ROOT))));
            }
            return true;
        }

        if ("validate".equals(action)) {
            String reason = plugin.getClanService().validateObjective(clan, player.getLocation());
            if (reason == null) {
                player.sendMessage(plugin.msg("objective.valid"));
            } else {
                player.sendMessage(plugin.msg("objective.invalid", mapOf("reason", reason)));
            }
            return true;
        }

        if ("preview".equals(action)) {
            ObjectiveType type = args.length >= 3 ? ObjectiveType.fromInput(args[2]) : ObjectiveType.CAPTURE;
            if (type == null) {
                player.sendMessage(plugin.prefixed("&cAllowed objective types: capture, beacon, crystal"));
                return true;
            }
            Location objective = plugin.getClanService().getObjectiveLocation(clan, type);
            if (objective == null) {
                player.sendMessage(plugin.prefixed("&cObjective not set."));
                return true;
            }
            List<Location> entries = plugin.getClanService().getPreviewEntryPoints(clan, objective);
            if (entries.isEmpty()) {
                player.sendMessage(plugin.prefixed("&cNo reachable entry points found for this objective."));
                return true;
            }

            player.sendMessage(plugin.prefixed("&ePreviewing entries for " + type.name().toLowerCase(Locale.ROOT) + " (&f" + entries.size() + "&e)"));
            int shown = 0;
            for (Location entry : entries) {
                if (shown >= 12) {
                    break;
                }
                shown++;
                entry.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, entry.clone().add(0.5, 1.0, 0.5), 20, 0.2, 0.2, 0.2, 0.01);
                player.sendMessage(plugin.prefixed("&7Entry: &f" + entry.getBlockX() + " " + entry.getBlockY() + " " + entry.getBlockZ()));
            }
            return true;
        }

        if ("list".equals(action)) {
            player.sendMessage(plugin.prefixed("&eObjectives for " + clan.name + ":"));
            for (ObjectiveType type : new ObjectiveType[] {ObjectiveType.CAPTURE, ObjectiveType.BEACON, ObjectiveType.CRYSTAL}) {
                Location loc = plugin.getClanService().getObjectiveLocation(clan, type);
                player.sendMessage(plugin.prefixed("&7- " + type.name().toLowerCase(Locale.ROOT) + ": &f"
                        + (loc == null ? "not set" : (loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ()))));
            }
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan objective <set|remove|validate|preview|list>"));
        return true;
    }

    private boolean handleWar(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan war <challenge|accept|deny|info|forfeit|log> ..."));
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());

        if ("challenge".equals(action)) {
            if (role == null || !role.canManageWar()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (args.length < 4) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan war challenge <clan> <capture|beacon|crystal> [stake]"));
                return true;
            }

            ClanData target = plugin.getClanService().getClanByName(args[2]);
            WarMode mode = WarMode.fromInput(args[3]);
            if (target == null || mode == null) {
                player.sendMessage(plugin.prefixed("&cInvalid clan or mode."));
                return true;
            }
            if (clan.id.equals(target.id)) {
                player.sendMessage(plugin.msg("war.same-clan"));
                return true;
            }
            if (plugin.getWarService().isClanInWar(clan.id) || plugin.getWarService().isClanInWar(target.id)) {
                player.sendMessage(plugin.msg("war.already-active"));
                return true;
            }
            if (plugin.getClanService().getObjective(target, mode.requiredObjective()) == null) {
                player.sendMessage(plugin.msg("war.objective-missing", mapOf("mode", mode.name().toLowerCase(Locale.ROOT))));
                return true;
            }
            if (!plugin.getSiegeWindowService().isOpenNow(target)) {
                player.sendMessage(plugin.msg("war.outside-window"));
                return true;
            }

            double stake = 0.0D;
            if (args.length >= 5) {
                try {
                    stake = Double.parseDouble(args[4]);
                } catch (NumberFormatException ex) {
                    player.sendMessage(plugin.msg("war.invalid-stake"));
                    return true;
                }
            }
            if (!Double.isFinite(stake) || stake < 0.0D) {
                player.sendMessage(plugin.msg("war.invalid-stake"));
                return true;
            }
            if (stake > 0.0D && (clan.bank < stake || target.bank < stake)) {
                player.sendMessage(plugin.msg("war.stake-not-enough", mapOf("stake", fmt(stake))));
                return true;
            }

            WarRequest request = plugin.getWarService().createChallenge(clan, target, mode, stake);
            if (request == null) {
                player.sendMessage(plugin.prefixed("&cCould not create war request."));
                return true;
            }

            player.sendMessage(plugin.msg("war.challenge-created", mapOf("id", String.valueOf(request.id), "clan", target.name, "mode", mode.name().toLowerCase(Locale.ROOT))));
            for (String member : target.members.keySet()) {
                Player memberPlayer = Bukkit.getPlayer(UUID.fromString(member));
                if (memberPlayer != null && memberPlayer.isOnline()) {
                    memberPlayer.sendMessage(plugin.msg("war.challenge-received", mapOf("id", String.valueOf(request.id), "clan", clan.name)));
                }
            }
            return true;
        }

        if ("accept".equals(action)) {
            if (role == null || !role.canManageWar()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan war accept <id>"));
                return true;
            }
            int requestId;
            try {
                requestId = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                player.sendMessage(plugin.prefixed("&cInvalid request id."));
                return true;
            }
            WarRequest request = plugin.getWarService().getWarRequest(requestId);
            if (request == null || !clan.id.equals(request.defenderClanId)) {
                player.sendMessage(plugin.prefixed("&cWar request not found for your clan."));
                return true;
            }
            if (request.attackerClanId != null && request.attackerClanId.equals(request.defenderClanId)) {
                plugin.getWarService().denyWarRequest(requestId);
                player.sendMessage(plugin.msg("war.same-clan"));
                return true;
            }

            ClanData attackerClan = plugin.getClanService().getClanById(request.attackerClanId);
            if (attackerClan == null) {
                plugin.getWarService().denyWarRequest(requestId);
                player.sendMessage(plugin.msg("war.request-invalid"));
                return true;
            }
            if (request.stake > 0.0D && (attackerClan.bank < request.stake || clan.bank < request.stake)) {
                player.sendMessage(plugin.msg("war.stake-not-enough", mapOf("stake", fmt(request.stake))));
                return true;
            }

            WarSession session = plugin.getWarService().acceptWarRequest(requestId);
            if (session == null) {
                player.sendMessage(plugin.prefixed("&cCould not start war."));
                return true;
            }
            ClanData attacker = plugin.getClanService().getClanById(session.attackerClanId);
            player.sendMessage(plugin.msg("war.started", mapOf("attacker", attacker == null ? "?" : attacker.name, "defender", clan.name,
                    "mode", session.mode.name().toLowerCase(Locale.ROOT))));
            return true;
        }

        if ("deny".equals(action)) {
            if (role == null || !role.canManageWar()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (args.length < 3) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan war deny <id>"));
                return true;
            }
            int requestId;
            try {
                requestId = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                player.sendMessage(plugin.prefixed("&cInvalid request id."));
                return true;
            }
            WarRequest request = plugin.getWarService().getWarRequest(requestId);
            if (request == null || !clan.id.equals(request.defenderClanId)) {
                player.sendMessage(plugin.prefixed("&cWar request not found for your clan."));
                return true;
            }
            plugin.getWarService().denyWarRequest(requestId);
            player.sendMessage(plugin.msg("war.challenge-denied"));
            return true;
        }

        if ("info".equals(action)) {
            WarSession war = plugin.getWarService().getWarByClan(clan.id);
            if (war == null) {
                player.sendMessage(plugin.msg("war.no-active"));
                return true;
            }
            ClanData attacker = plugin.getClanService().getClanById(war.attackerClanId);
            ClanData defender = plugin.getClanService().getClanById(war.defenderClanId);
            player.sendMessage(plugin.prefixed("&cWar info: &f" + (attacker == null ? "?" : attacker.name) + " &7vs &f" + (defender == null ? "?" : defender.name)));
            player.sendMessage(plugin.prefixed("&7Mode: &f" + war.mode.name().toLowerCase(Locale.ROOT) + " &8| &7Status: &f" + war.status.name().toLowerCase(Locale.ROOT)));
            player.sendMessage(plugin.prefixed("&7Time left: &f" + Math.max(0, (war.endsAt - System.currentTimeMillis()) / 1000) + "s"));
            if (war.mode == WarMode.CAPTURE) {
                player.sendMessage(plugin.prefixed("&7Capture: &f" + war.captureProgressSeconds + " / " + war.captureTargetSeconds));
            } else {
                player.sendMessage(plugin.prefixed("&7Crystal HP: &f" + fmt(war.crystalHp) + " / " + fmt(war.crystalMaxHp)));
            }
            int position = plugin.getWarService().getQueuePosition(war, player.getUniqueId());
            if (position > 0) {
                player.sendMessage(plugin.msg("war.queue", mapOf("position", String.valueOf(position))));
            }
            return true;
        }

        if ("forfeit".equals(action)) {
            if (role == null || !role.canManageWar()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (!plugin.getWarService().forfeit(clan.id)) {
                player.sendMessage(plugin.msg("war.no-active"));
                return true;
            }
            player.sendMessage(plugin.msg("war.forfeit"));
            return true;
        }

        if ("log".equals(action)) {
            int shown = 0;
            for (String warId : plugin.getStorage().data().recentWarIds) {
                WarReplay replay = plugin.getStorage().data().replays.get(warId);
                if (replay == null) {
                    continue;
                }
                ClanData a = plugin.getClanService().getClanById(replay.attackerClanId);
                ClanData d = plugin.getClanService().getClanById(replay.defenderClanId);
                ClanData w = plugin.getClanService().getClanById(replay.winnerClanId);
                player.sendMessage(plugin.prefixed("&7#" + (++shown) + " &f" + (a == null ? "?" : a.name) + " vs " + (d == null ? "?" : d.name)
                        + " &8| &7winner: &f" + (w == null ? "?" : w.name) + " &8| &7" + replay.summary));
                if (shown >= 5) {
                    break;
                }
            }
            if (shown == 0) {
                player.sendMessage(plugin.prefixed("&7No war replays yet."));
            }
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan war <challenge|accept|deny|info|forfeit|log>"));
        return true;
    }

    private boolean handleSiege(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }
        ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
        if (role == null || !role.canManageWar()) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }

        if (args.length < 3 || !"window".equalsIgnoreCase(args[1])) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan siege window <set|view|clear> ..."));
            return true;
        }

        String action = args[2].toLowerCase(Locale.ROOT);
        if ("set".equals(action)) {
            if (args.length < 6) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan siege window set <day> <start> <end>"));
                return true;
            }
            DayOfWeek day;
            try {
                day = DayOfWeek.valueOf(args[3].toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                player.sendMessage(plugin.prefixed("&cInvalid day."));
                return true;
            }
            String range = args[4] + "-" + args[5];
            if (!plugin.getClanService().setSiegeWindow(clan, day, range)) {
                player.sendMessage(plugin.prefixed("&cCould not set window."));
                return true;
            }
            player.sendMessage(plugin.prefixed("&aSiege window set for " + day.name().toLowerCase(Locale.ROOT) + ": " + range));
            return true;
        }

        if ("view".equals(action)) {
            Map<String, List<String>> windows = plugin.getClanService().getSiegeWindows(clan);
            player.sendMessage(plugin.prefixed("&eSiege windows for " + clan.name + ":"));
            for (Map.Entry<String, List<String>> entry : windows.entrySet()) {
                player.sendMessage(plugin.prefixed("&7- " + entry.getKey() + ": &f" + String.join(", ", entry.getValue())));
            }
            player.sendMessage(plugin.prefixed("&7Currently open: &f" + plugin.getSiegeWindowService().isOpenNow(clan)));
            return true;
        }

        if ("clear".equals(action)) {
            if (args.length < 4) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan siege window clear <day>"));
                return true;
            }
            String key = args[3].toLowerCase(Locale.ROOT);
            clan.siegeWindows.remove(key);
            player.sendMessage(plugin.prefixed("&aCustom window for " + key + " removed."));
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan siege window <set|view|clear>"));
        return true;
    }

    private boolean handleBounty(Player player, String[] args) {
        ClanData clan = plugin.getClanService().getClan(player);
        if (clan == null) {
            player.sendMessage(plugin.msg("clan.not-in-clan"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.prefixed("&eUsage: /clan bounty <create|info> ..."));
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        if ("create".equals(action)) {
            ClanRole role = plugin.getClanService().getRole(clan, player.getUniqueId());
            if (role == null || !role.canManageWar()) {
                player.sendMessage(plugin.msg("common.no-permission"));
                return true;
            }
            if (args.length < 4) {
                player.sendMessage(plugin.prefixed("&eUsage: /clan bounty create <targetClan> <reward>"));
                return true;
            }
            ClanData target = plugin.getClanService().getClanByName(args[2]);
            double reward;
            try {
                reward = Double.parseDouble(args[3]);
            } catch (NumberFormatException ex) {
                player.sendMessage(plugin.prefixed("&cInvalid reward."));
                return true;
            }
            if (target == null) {
                player.sendMessage(plugin.prefixed("&cClan not found."));
                return true;
            }
            if (clan.bank < reward) {
                player.sendMessage(plugin.prefixed("&cNot enough clan bank for bounty."));
                return true;
            }
            BountyContract bounty = plugin.getBountyService().create(clan.id, target.id, reward);
            if (bounty == null) {
                player.sendMessage(plugin.prefixed("&cCould not create bounty contract."));
                return true;
            }
            clan.bank -= reward;
            player.sendMessage(plugin.msg("bounty.created", mapOf("clan", target.name, "reward", fmt(reward))));
            return true;
        }

        if ("info".equals(action)) {
            BountyContract bounty = plugin.getBountyService().firstActiveForTarget(clan.id);
            if (bounty == null) {
                player.sendMessage(plugin.msg("bounty.none"));
                return true;
            }
            ClanData issuer = plugin.getClanService().getClanById(bounty.issuerClanId);
            long minutes = Math.max(0L, (bounty.expiresAt - System.currentTimeMillis()) / 60000L);
            player.sendMessage(plugin.prefixed("&eActive bounty on your clan from &f" + (issuer == null ? "unknown" : issuer.name)));
            player.sendMessage(plugin.prefixed("&7Reward: &f" + fmt(bounty.reward) + " &8| &7Time left: &f" + minutes + "m"));
            return true;
        }

        player.sendMessage(plugin.prefixed("&eUsage: /clan bounty <create|info>"));
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(plugin.msg("help.header"));
        sender.sendMessage(plugin.msg("help.line-1"));
        sender.sendMessage(plugin.msg("help.line-2"));
        sender.sendMessage(plugin.msg("help.line-3"));
        sender.sendMessage(plugin.msg("help.line-4"));
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private Map<String, String> mapOf(String... values) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("help", "menu", "create", "disband", "info", "list", "top", "online", "invite", "join", "leave",
                    "kick", "promote", "demote", "owner", "chat", "sethome", "home", "claim", "bank", "ally", "enemy", "neutral",
                    "objective", "war", "siege", "bounty"), args[0]);
        }
        if (args.length == 2 && "menu".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("main", "war", "objectives", "admin"), args[1]);
        }
        if (args.length == 2 && "war".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("challenge", "accept", "deny", "info", "forfeit", "log"), args[1]);
        }
        if (args.length == 2 && "claim".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("set"), args[1]);
        }
        if (args.length == 3 && "claim".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1])) {
            return filter(Arrays.asList(String.valueOf(plugin.getCoreConfig().defaultClaimRadius), String.valueOf(plugin.getCoreConfig().minClaimRadius), String.valueOf(plugin.getCoreConfig().maxClaimRadius)), args[2]);
        }
        if (args.length == 2 && "objective".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("set", "remove", "validate", "preview", "list"), args[1]);
        }
        if (args.length == 3 && "objective".equalsIgnoreCase(args[0]) && ("set".equalsIgnoreCase(args[1]) || "remove".equalsIgnoreCase(args[1]) || "preview".equalsIgnoreCase(args[1]))) {
            return filter(Arrays.asList("capture", "beacon", "crystal"), args[2]);
        }
        if (args.length == 2 && "bank".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("deposit", "withdraw"), args[1]);
        }
        if (args.length == 3 && "war".equalsIgnoreCase(args[0]) && "challenge".equalsIgnoreCase(args[1])) {
            List<String> names = new ArrayList<String>();
            for (String id : plugin.getClanService().allClanIds()) {
                ClanData clan = plugin.getClanService().getClanById(id);
                if (clan != null) {
                    names.add(clan.name);
                }
            }
            return filter(names, args[2]);
        }
        if (args.length == 4 && "war".equalsIgnoreCase(args[0]) && "challenge".equalsIgnoreCase(args[1])) {
            return filter(Arrays.asList("capture", "beacon", "crystal"), args[3]);
        }
        if (args.length == 2 && ("invite".equalsIgnoreCase(args[0]) || "kick".equalsIgnoreCase(args[0]) || "promote".equalsIgnoreCase(args[0]) || "demote".equalsIgnoreCase(args[0]) || "owner".equalsIgnoreCase(args[0]))) {
            List<String> names = new ArrayList<String>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                names.add(online.getName());
            }
            return filter(names, args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> source, String input) {
        String lower = input == null ? "" : input.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<String>();
        for (String value : source) {
            if (value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(value);
            }
        }
        return out;
    }
}
