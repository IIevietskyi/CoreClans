package dev.iievietskyi.coreclans.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.ObjectiveType;
import dev.iievietskyi.coreclans.model.WarSession;
import dev.iievietskyi.coreclans.service.Texts;

public class MenuService {
    private final CoreClansPlugin plugin;
    private final Map<String, MenuDefinition> menus = new HashMap<String, MenuDefinition>();

    public MenuService(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        menus.clear();

        ensureDefaultMenu("menus/main.yml");
        ensureDefaultMenu("menus/war.yml");
        ensureDefaultMenu("menus/objectives.yml");
        ensureDefaultMenu("menus/admin.yml");

        File folder = new File(plugin.getDataFolder(), "menus");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }

        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File a, File b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        for (File file : files) {
            if (file == null || !file.isFile() || !file.getName().toLowerCase(Locale.ROOT).endsWith(".yml")) {
                continue;
            }
            loadMenu(file);
        }

        if (!menus.containsKey("main")) {
            plugin.getLogger().warning("No main menu found in /menus. Create menus/main.yml with id: main");
        }
        plugin.getLogger().info("Loaded " + menus.size() + " menu file(s).");
    }

    public boolean openMenu(Player player, String menuId) {
        if (player == null) {
            return false;
        }
        String key = menuId == null ? "main" : menuId.trim().toLowerCase(Locale.ROOT);
        if (key.isEmpty()) {
            key = "main";
        }

        MenuDefinition menu = menus.get(key);
        if (menu == null) {
            player.sendMessage(plugin.prefixed("&cMenu not found: " + key));
            return false;
        }

        Map<String, String> context = buildContext(player);
        String title = Texts.format(menu.title, context);
        Inventory inventory = Bukkit.createInventory(new CoreMenuHolder(menu.id), menu.size, title);

        for (MenuItemDefinition item : menu.items.values()) {
            inventory.setItem(item.slot, buildItem(item, context));
        }

        player.openInventory(inventory);
        return true;
    }

    public void handleClick(InventoryClickEvent event) {
        if (event == null || event.getWhoClicked() == null || !(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (event.getView() == null || event.getView().getTopInventory() == null) {
            return;
        }
        if (!(event.getView().getTopInventory().getHolder() instanceof CoreMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (event.getRawSlot() < 0 || event.getRawSlot() >= event.getView().getTopInventory().getSize()) {
            return;
        }

        CoreMenuHolder holder = (CoreMenuHolder) event.getView().getTopInventory().getHolder();
        MenuDefinition menu = menus.get(holder.getMenuId());
        if (menu == null) {
            return;
        }

        MenuItemDefinition item = menu.items.get(Integer.valueOf(event.getRawSlot()));
        if (item == null) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        if (item.permission != null && !item.permission.isEmpty() && !player.hasPermission(item.permission)) {
            player.sendMessage(plugin.msg("common.no-permission"));
            return;
        }

        Map<String, String> context = buildContext(player);
        String value = Texts.replace(item.actionValue, context);

        switch (item.action) {
            case COMMAND:
                if (value == null || value.trim().isEmpty()) {
                    return;
                }
                String command = value.trim();
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                if (!command.isEmpty()) {
                    player.closeInventory();
                    player.performCommand(command);
                }
                return;
            case OPEN_MENU:
                openMenu(player, value);
                return;
            case CLOSE:
                player.closeInventory();
                return;
            case NONE:
            default:
                return;
        }
    }

    private void ensureDefaultMenu(String resourcePath) {
        File out = new File(plugin.getDataFolder(), resourcePath);
        File parent = out.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!out.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private void loadMenu(File file) {
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String id = cfg.getString("id", stripExtension(file.getName())).trim().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            id = stripExtension(file.getName()).toLowerCase(Locale.ROOT);
        }

        MenuDefinition menu = new MenuDefinition();
        menu.id = id;
        menu.title = cfg.getString("title", "&8Menu");
        menu.size = normalizeSize(cfg.getInt("size", 27));

        ConfigurationSection items = cfg.getConfigurationSection("items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection section = items.getConfigurationSection(key);
                if (section == null) {
                    continue;
                }

                int slot = section.getInt("slot", -1);
                if (slot < 0 || slot >= menu.size) {
                    continue;
                }

                MenuItemDefinition item = new MenuItemDefinition();
                item.slot = slot;

                Material material = Material.matchMaterial(section.getString("material", "PAPER"));
                item.material = material == null ? Material.BARRIER : material;
                item.amount = Math.max(1, Math.min(64, section.getInt("amount", 1)));
                item.name = section.getString("name", "&7Item");
                item.lore = section.getStringList("lore");
                item.skullOwner = section.getString("skull-owner", "");
                item.permission = section.getString("permission", "");
                item.glow = section.getBoolean("glow", false);

                ConfigurationSection action = section.getConfigurationSection("action");
                if (action == null) {
                    item.action = parseAction(section.getString("action", "none"));
                    item.actionValue = section.getString("command", "");
                } else {
                    item.action = parseAction(action.getString("type", "none"));
                    item.actionValue = action.getString("value", "");
                }

                menu.items.put(Integer.valueOf(slot), item);
            }
        }

        menus.put(menu.id, menu);
    }

    private int normalizeSize(int size) {
        int out = Math.max(9, Math.min(54, size));
        if (out % 9 != 0) {
            out = (out / 9) * 9;
            if (out < 9) {
                out = 9;
            }
        }
        return out;
    }

    private MenuAction parseAction(String input) {
        if (input == null) {
            return MenuAction.NONE;
        }
        String value = input.trim().toLowerCase(Locale.ROOT);
        if ("command".equals(value) || "cmd".equals(value)) {
            return MenuAction.COMMAND;
        }
        if ("open_menu".equals(value) || "menu".equals(value) || "open".equals(value)) {
            return MenuAction.OPEN_MENU;
        }
        if ("close".equals(value)) {
            return MenuAction.CLOSE;
        }
        return MenuAction.NONE;
    }

    private ItemStack buildItem(MenuItemDefinition item, Map<String, String> context) {
        ItemStack stack = new ItemStack(item.material, item.amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        meta.setDisplayName(Texts.format(item.name, context));

        List<String> lore = item.lore == null ? Collections.<String>emptyList() : item.lore;
        if (!lore.isEmpty()) {
            List<String> out = new ArrayList<String>();
            for (String line : lore) {
                out.add(Texts.format(line, context));
            }
            meta.setLore(out);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        if (item.glow) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (meta instanceof SkullMeta && item.skullOwner != null && !item.skullOwner.isEmpty()) {
            SkullMeta skull = (SkullMeta) meta;
            skull.setOwner(Texts.replace(item.skullOwner, context));
            meta = skull;
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private Map<String, String> buildContext(Player player) {
        Map<String, String> map = new HashMap<String, String>();
        map.put("player", player.getName());

        ClanData clan = plugin.getClanService().getClan(player);
        map.put("clan_name", clan == null ? plugin.getCoreConfig().placeholderNoClan : clan.name);
        map.put("clan_tag", clan == null ? plugin.getCoreConfig().placeholderNoClan : clan.tag);
        map.put("clan_members", clan == null ? "0" : String.valueOf(clan.members.size()));
        map.put("clan_online", clan == null ? "0" : String.valueOf(plugin.getClanService().getOnlineMemberCount(clan)));
        map.put("clan_bank", clan == null ? "0.00" : fmt(clan.bank));
        map.put("clan_points", clan == null ? "0" : String.valueOf(plugin.getSeasonService().getPoints(clan.id)));
        int rank = clan == null ? -1 : plugin.getSeasonService().rankOf(clan.id);
        map.put("clan_rank", rank <= 0 ? "-" : String.valueOf(rank));

        map.put("objective_capture", clan != null && plugin.getClanService().getObjective(clan, ObjectiveType.CAPTURE) != null ? "set" : "missing");
        map.put("objective_beacon", clan != null && plugin.getClanService().getObjective(clan, ObjectiveType.BEACON) != null ? "set" : "missing");
        map.put("objective_crystal", clan != null && plugin.getClanService().getObjective(clan, ObjectiveType.CRYSTAL) != null ? "set" : "missing");

        WarSession war = clan == null ? null : plugin.getWarService().getWarByClan(clan.id);
        if (war == null) {
            map.put("war_active", "no");
            map.put("war_mode", plugin.getCoreConfig().placeholderNoWar);
            map.put("war_status", "none");
            map.put("war_time_left", "0");
            map.put("war_capture", "0/0");
        } else {
            map.put("war_active", "yes");
            map.put("war_mode", war.mode.name().toLowerCase(Locale.ROOT));
            map.put("war_status", war.status.name().toLowerCase(Locale.ROOT));
            map.put("war_time_left", String.valueOf(Math.max(0, (war.endsAt - System.currentTimeMillis()) / 1000L)));
            map.put("war_capture", war.captureProgressSeconds + "/" + war.captureTargetSeconds);
        }

        map.put("siege_open", clan != null && plugin.getSiegeWindowService().isOpenNow(clan) ? "yes" : "no");
        return map;
    }

    private String fmt(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String stripExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot <= 0 ? name : name.substring(0, dot);
    }

    private static class MenuDefinition {
        String id;
        String title;
        int size;
        Map<Integer, MenuItemDefinition> items = new HashMap<Integer, MenuItemDefinition>();
    }

    private static class MenuItemDefinition {
        int slot;
        Material material;
        int amount;
        String name;
        List<String> lore = Collections.emptyList();
        String skullOwner;
        String permission;
        boolean glow;
        MenuAction action = MenuAction.NONE;
        String actionValue = "";
    }

    private enum MenuAction {
        NONE,
        COMMAND,
        OPEN_MENU,
        CLOSE
    }
}