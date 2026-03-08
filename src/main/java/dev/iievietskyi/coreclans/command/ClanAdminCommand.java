package dev.iievietskyi.coreclans.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.WarMode;
import dev.iievietskyi.coreclans.model.WarRequest;
import dev.iievietskyi.coreclans.model.WarSession;

public class ClanAdminCommand implements CommandExecutor, TabCompleter {
    private final CoreClansPlugin plugin;

    public ClanAdminCommand(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("coreclans.admin")) {
            sender.sendMessage(plugin.msg("common.no-permission"));
            return true;
        }
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sender.sendMessage(plugin.prefixed("&e/clanadmin reload"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin menu [admin|main|war|objectives]"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin war stop <clan>"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin war start <attacker> <defender> <capture|beacon|crystal>"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin season reset [name]"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin points set <clan> <value>"));
            sender.sendMessage(plugin.prefixed("&e/clanadmin debug path <clan> <capture|beacon|crystal>"));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(sub)) {
            plugin.reloadAll();
            sender.sendMessage(plugin.msg("common.reloaded"));
            return true;
        }

        if ("menu".equals(sub)) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.msg("common.player-only"));
                return true;
            }
            Player player = (Player) sender;
            String menuId = args.length >= 2 ? args[1].toLowerCase(Locale.ROOT) : "admin";
            if (!plugin.getMenuService().openMenu(player, menuId)) {
                sender.sendMessage(plugin.prefixed("&cMenu not found: " + menuId));
            }
            return true;
        }
        if ("war".equals(sub)) {
            return handleWar(sender, args);
        }

        if ("season".equals(sub)) {
            if (args.length >= 2 && "reset".equalsIgnoreCase(args[1])) {
                String name = args.length >= 3 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : plugin.getCoreConfig().seasonName;
                plugin.getSeasonService().resetSeason(name);
                sender.sendMessage(plugin.msg("season.reset"));
                return true;
            }
            sender.sendMessage(plugin.prefixed("&eUsage: /clanadmin season reset [name]"));
            return true;
        }

        if ("points".equals(sub)) {
            if (args.length >= 4 && "set".equalsIgnoreCase(args[1])) {
                ClanData clan = plugin.getClanService().getClanByName(args[2]);
                if (clan == null) {
                    sender.sendMessage(plugin.prefixed("&cClan not found."));
                    return true;
                }
                int value;
                try {
                    value = Integer.parseInt(args[3]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage(plugin.prefixed("&cInvalid value."));
                    return true;
                }
                int current = plugin.getSeasonService().getPoints(clan.id);
                plugin.getSeasonService().addPoints(clan.id, value - current);
                sender.sendMessage(plugin.prefixed("&aPoints updated for " + clan.name + ": " + value));
                return true;
            }
            sender.sendMessage(plugin.prefixed("&eUsage: /clanadmin points set <clan> <value>"));
            return true;
        }

        if ("debug".equals(sub) && args.length >= 4 && "path".equalsIgnoreCase(args[1])) {
            ClanData clan = plugin.getClanService().getClanByName(args[2]);
            WarMode mode = WarMode.fromInput(args[3]);
            if (clan == null || mode == null) {
                sender.sendMessage(plugin.prefixed("&cInvalid clan or mode."));
                return true;
            }
            org.bukkit.Location objective = plugin.getClanService().getObjectiveLocation(clan, mode.requiredObjective());
            if (objective == null) {
                sender.sendMessage(plugin.prefixed("&cObjective not set."));
                return true;
            }
            String reason = plugin.getClanService().validateObjective(clan, objective);
            sender.sendMessage(plugin.prefixed("&7Path/objective validation: &f" + (reason == null ? "valid" : reason)));
            return true;
        }

        sender.sendMessage(plugin.prefixed("&cUnknown admin subcommand."));
        return true;
    }

    private boolean handleWar(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.prefixed("&eUsage: /clanadmin war <stop|start> ..."));
            return true;
        }
        String action = args[1].toLowerCase(Locale.ROOT);

        if ("stop".equals(action)) {
            ClanData clan = plugin.getClanService().getClanByName(args[2]);
            if (clan == null) {
                sender.sendMessage(plugin.prefixed("&cClan not found."));
                return true;
            }
            if (!plugin.getWarService().forfeit(clan.id)) {
                sender.sendMessage(plugin.prefixed("&cClan has no active war."));
                return true;
            }
            sender.sendMessage(plugin.prefixed("&aWar stopped via forfeit for clan " + clan.name));
            return true;
        }

        if ("start".equals(action)) {
            if (args.length < 5) {
                sender.sendMessage(plugin.prefixed("&eUsage: /clanadmin war start <attacker> <defender> <capture|beacon|crystal>"));
                return true;
            }
            ClanData attacker = plugin.getClanService().getClanByName(args[2]);
            ClanData defender = plugin.getClanService().getClanByName(args[3]);
            WarMode mode = WarMode.fromInput(args[4]);
            if (attacker == null || defender == null || mode == null) {
                sender.sendMessage(plugin.prefixed("&cInvalid attacker/defender/mode."));
                return true;
            }
            if (attacker.id.equals(defender.id)) {
                sender.sendMessage(plugin.msg("war.same-clan"));
                return true;
            }
            WarRequest request = plugin.getWarService().createChallenge(attacker, defender, mode, 0.0D);
            if (request == null) {
                sender.sendMessage(plugin.prefixed("&cCould not create war challenge (check windows/objectives/active wars)."));
                return true;
            }
            WarSession session = plugin.getWarService().acceptWarRequest(request.id);
            if (session == null) {
                sender.sendMessage(plugin.prefixed("&cCould not start war from request."));
                return true;
            }
            sender.sendMessage(plugin.prefixed("&aWar started: " + attacker.name + " vs " + defender.name + " (" + mode.name().toLowerCase(Locale.ROOT) + ")"));
            return true;
        }

        sender.sendMessage(plugin.prefixed("&eUsage: /clanadmin war <stop|start> ..."));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("help", "reload", "menu", "war", "season", "points", "debug"), args[0]);
        }
        if (args.length == 2 && "menu".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("admin", "main", "war", "objectives"), args[1]);
        }
        if (args.length == 2 && "war".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("stop", "start"), args[1]);
        }
        if (args.length == 3 && "war".equalsIgnoreCase(args[0]) && ("stop".equalsIgnoreCase(args[1]) || "start".equalsIgnoreCase(args[1]))) {
            return clanNames(args[2]);
        }
        if (args.length == 4 && "war".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1])) {
            return clanNames(args[3]);
        }
        if (args.length == 5 && "war".equalsIgnoreCase(args[0]) && "start".equalsIgnoreCase(args[1])) {
            return filter(Arrays.asList("capture", "beacon", "crystal"), args[4]);
        }
        if (args.length == 2 && "season".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("reset"), args[1]);
        }
        if (args.length == 2 && "points".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("set"), args[1]);
        }
        if (args.length == 3 && "points".equalsIgnoreCase(args[0]) && "set".equalsIgnoreCase(args[1])) {
            return clanNames(args[2]);
        }
        if (args.length == 2 && "debug".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("path"), args[1]);
        }
        if (args.length == 3 && "debug".equalsIgnoreCase(args[0]) && "path".equalsIgnoreCase(args[1])) {
            return clanNames(args[2]);
        }
        if (args.length == 4 && "debug".equalsIgnoreCase(args[0]) && "path".equalsIgnoreCase(args[1])) {
            return filter(Arrays.asList("capture", "beacon", "crystal"), args[3]);
        }
        return Collections.emptyList();
    }

    private List<String> clanNames(String input) {
        List<String> names = new ArrayList<String>();
        for (String id : plugin.getClanService().allClanIds()) {
            ClanData clan = plugin.getClanService().getClanById(id);
            if (clan != null) {
                names.add(clan.name);
            }
        }
        return filter(names, input);
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
