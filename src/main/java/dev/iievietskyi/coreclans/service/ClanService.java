package dev.iievietskyi.coreclans.service;

import java.time.DayOfWeek;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.ClanObjective;
import dev.iievietskyi.coreclans.model.ClanRole;
import dev.iievietskyi.coreclans.model.LocationData;
import dev.iievietskyi.coreclans.model.ObjectiveType;
import dev.iievietskyi.coreclans.model.PlayerProfile;

public class ClanService {
    private final CoreClansPlugin plugin;

    private final Map<String, ClanData> clanByNameLower = new ConcurrentHashMap<String, ClanData>();

    public ClanService(CoreClansPlugin plugin) {
        this.plugin = plugin;
        reindex();
    }

    public void reindex() {
        clanByNameLower.clear();
        for (ClanData clan : plugin.getStorage().data().clans.values()) {
            if (clan != null && clan.name != null) {
                clanByNameLower.put(clan.name.toLowerCase(Locale.ROOT), clan);
            }
        }
    }

    public PlayerProfile getOrCreateProfile(UUID uuid) {
        String key = uuid.toString();
        PlayerProfile profile = plugin.getStorage().data().players.get(key);
        if (profile == null) {
            profile = new PlayerProfile();
            plugin.getStorage().data().players.put(key, profile);
        }
        return profile;
    }

    public ClanData getClanById(String id) {
        if (id == null) {
            return null;
        }
        return plugin.getStorage().data().clans.get(id);
    }

    public ClanData getClanByName(String name) {
        if (name == null) {
            return null;
        }
        return clanByNameLower.get(name.toLowerCase(Locale.ROOT));
    }

    public ClanData getClan(Player player) {
        PlayerProfile profile = getOrCreateProfile(player.getUniqueId());
        return profile.clanId == null ? null : getClanById(profile.clanId);
    }

    public ClanRole getRole(ClanData clan, UUID uuid) {
        if (clan == null || uuid == null) {
            return null;
        }
        return clan.members.get(uuid.toString());
    }

    public boolean isInClan(UUID uuid) {
        PlayerProfile profile = getOrCreateProfile(uuid);
        return profile.clanId != null && getClanById(profile.clanId) != null;
    }

    public ClanData createClan(Player owner, String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.length() < plugin.getCoreConfig().minNameLength || trimmed.length() > plugin.getCoreConfig().maxNameLength) {
            return null;
        }
        if (!trimmed.matches("[A-Za-z0-9_]+")) {
            return null;
        }
        if (getClan(owner) != null || getClanByName(trimmed) != null) {
            return null;
        }

        ClanData clan = new ClanData();
        clan.id = UUID.randomUUID().toString();
        clan.name = trimmed;
        clan.tag = plugin.getCoreConfig().defaultTag;
        clan.ownerUuid = owner.getUniqueId().toString();
        clan.members.put(owner.getUniqueId().toString(), ClanRole.LEADER);
        clan.claimRadius = plugin.getCoreConfig().defaultClaimRadius;
        clan.claimCenter = LocationCodec.from(owner.getLocation());
        clan.siegeWindows.putAll(plugin.getCoreConfig().globalSiegeWindows);

        plugin.getStorage().data().clans.put(clan.id, clan);
        getOrCreateProfile(owner.getUniqueId()).clanId = clan.id;
        reindex();
        return clan;
    }

    public void disbandClan(ClanData clan) {
        if (clan == null) {
            return;
        }
        for (String memberUuid : new ArrayList<String>(clan.members.keySet())) {
            PlayerProfile profile = plugin.getStorage().data().players.get(memberUuid);
            if (profile != null && clan.id.equals(profile.clanId)) {
                profile.clanId = null;
            }
        }
        for (ClanData other : plugin.getStorage().data().clans.values()) {
            if (other == null || other == clan) {
                continue;
            }
            other.allies.remove(clan.id);
            other.enemies.remove(clan.id);
        }
        plugin.getStorage().data().clans.remove(clan.id);
        reindex();
    }

    public boolean invitePlayer(ClanData clan, Player inviter, Player target) {
        if (clan == null || inviter == null || target == null) {
            return false;
        }
        ClanRole role = getRole(clan, inviter.getUniqueId());
        if (role == null || !role.canInvite()) {
            return false;
        }
        if (isInClan(target.getUniqueId())) {
            return false;
        }
        clan.invites.add(target.getUniqueId().toString());
        return true;
    }

    public boolean joinClan(Player player, ClanData clan) {
        if (player == null || clan == null) {
            return false;
        }
        if (isInClan(player.getUniqueId())) {
            return false;
        }
        if (!clan.invites.contains(player.getUniqueId().toString())) {
            return false;
        }
        if (clan.members.size() >= plugin.getCoreConfig().maxMembers) {
            return false;
        }
        clan.invites.remove(player.getUniqueId().toString());
        clan.members.put(player.getUniqueId().toString(), ClanRole.MEMBER);
        getOrCreateProfile(player.getUniqueId()).clanId = clan.id;
        return true;
    }

    public boolean leaveClan(Player player) {
        ClanData clan = getClan(player);
        if (clan == null) {
            return false;
        }
        String id = player.getUniqueId().toString();
        ClanRole role = clan.members.get(id);
        if (role == ClanRole.LEADER && clan.members.size() > 1) {
            return false;
        }
        clan.members.remove(id);
        getOrCreateProfile(player.getUniqueId()).clanId = null;
        if (clan.members.isEmpty()) {
            disbandClan(clan);
        } else if (id.equals(clan.ownerUuid)) {
            String next = clan.members.keySet().iterator().next();
            clan.ownerUuid = next;
            clan.members.put(next, ClanRole.LEADER);
        }
        return true;
    }

    public boolean kickMember(ClanData clan, UUID actor, UUID target) {
        if (clan == null || actor == null || target == null) {
            return false;
        }
        if (!clan.members.containsKey(target.toString())) {
            return false;
        }
        ClanRole actorRole = clan.members.get(actor.toString());
        ClanRole targetRole = clan.members.get(target.toString());
        if (actorRole == null || targetRole == null) {
            return false;
        }
        if (targetRole == ClanRole.LEADER || !actorRole.canManageMembers()) {
            return false;
        }

        clan.members.remove(target.toString());
        PlayerProfile profile = plugin.getStorage().data().players.get(target.toString());
        if (profile != null && clan.id.equals(profile.clanId)) {
            profile.clanId = null;
        }
        return true;
    }

    public ClanRole promote(ClanData clan, UUID actor, UUID target) {
        if (clan == null) {
            return null;
        }
        ClanRole actorRole = clan.members.get(actor.toString());
        ClanRole targetRole = clan.members.get(target.toString());
        if (actorRole == null || targetRole == null || !actorRole.canManageMembers()) {
            return null;
        }

        ClanRole next;
        if (targetRole == ClanRole.MEMBER) {
            next = ClanRole.OFFICER;
        } else if (targetRole == ClanRole.OFFICER) {
            next = ClanRole.CO_LEADER;
        } else {
            return null;
        }
        clan.members.put(target.toString(), next);
        return next;
    }

    public ClanRole demote(ClanData clan, UUID actor, UUID target) {
        if (clan == null) {
            return null;
        }
        ClanRole actorRole = clan.members.get(actor.toString());
        ClanRole targetRole = clan.members.get(target.toString());
        if (actorRole == null || targetRole == null || !actorRole.canManageMembers()) {
            return null;
        }

        ClanRole next;
        if (targetRole == ClanRole.CO_LEADER) {
            next = ClanRole.OFFICER;
        } else if (targetRole == ClanRole.OFFICER) {
            next = ClanRole.MEMBER;
        } else {
            return null;
        }
        clan.members.put(target.toString(), next);
        return next;
    }

    public void transferOwnership(ClanData clan, UUID newOwner) {
        if (clan == null || newOwner == null) {
            return;
        }
        String newOwnerId = newOwner.toString();
        if (!clan.members.containsKey(newOwnerId)) {
            return;
        }
        String oldOwner = clan.ownerUuid;
        clan.ownerUuid = newOwnerId;
        clan.members.put(newOwnerId, ClanRole.LEADER);
        if (oldOwner != null && !oldOwner.equals(newOwnerId) && clan.members.containsKey(oldOwner)) {
            clan.members.put(oldOwner, ClanRole.CO_LEADER);
        }
    }

    public boolean setHome(ClanData clan, Location location) {
        if (clan == null || location == null || location.getWorld() == null) {
            return false;
        }
        clan.home = LocationCodec.from(location);
        return true;
    }

    public Location getHome(ClanData clan) {
        return clan == null ? null : LocationCodec.to(clan.home);
    }

    public boolean setClaimCenter(ClanData clan, Location location) {
        if (clan == null || location == null || location.getWorld() == null) {
            return false;
        }
        clan.claimCenter = LocationCodec.from(location);
        if (clan.claimRadius <= 0) {
            clan.claimRadius = plugin.getCoreConfig().defaultClaimRadius;
        }
        return true;
    }

    public boolean isInsideClaim(ClanData clan, Location location, int margin) {
        if (clan == null || clan.claimCenter == null || location == null || location.getWorld() == null) {
            return false;
        }
        Location center = LocationCodec.to(clan.claimCenter);
        if (center == null || center.getWorld() == null || !center.getWorld().getUID().equals(location.getWorld().getUID())) {
            return false;
        }
        int radius = clan.claimRadius > 0 ? clan.claimRadius : plugin.getCoreConfig().defaultClaimRadius;
        double max = Math.max(1, radius - margin);
        return Math.abs(location.getX() - center.getX()) <= max && Math.abs(location.getZ() - center.getZ()) <= max;
    }

    public String setObjective(ClanData clan, ObjectiveType type, Location location) {
        if (clan == null || type == null || location == null) {
            return "invalid input";
        }
        String reason = validateObjective(clan, location);
        if (reason != null) {
            return reason;
        }
        clan.objectives.put(type, new ClanObjective(type, LocationCodec.from(location)));
        return null;
    }

    public boolean removeObjective(ClanData clan, ObjectiveType type) {
        if (clan == null || type == null) {
            return false;
        }
        return clan.objectives.remove(type) != null;
    }

    public ClanObjective getObjective(ClanData clan, ObjectiveType type) {
        if (clan == null || type == null) {
            return null;
        }
        return clan.objectives.get(type);
    }

    public Location getObjectiveLocation(ClanData clan, ObjectiveType type) {
        ClanObjective objective = getObjective(clan, type);
        return objective == null ? null : LocationCodec.to(objective.location);
    }

    public String validateObjective(ClanData clan, Location location) {
        if (clan == null) {
            return "clan missing";
        }
        if (location == null || location.getWorld() == null) {
            return "location invalid";
        }
        if (!isInsideClaim(clan, location, plugin.getCoreConfig().objectiveBorderMargin)) {
            return "outside clan claim or too close to border";
        }
        int y = location.getBlockY();
        if (y < plugin.getCoreConfig().minObjectiveY || y > plugin.getCoreConfig().maxObjectiveY) {
            return "invalid Y level";
        }

        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block floor = feet.getRelative(0, -1, 0);
        if (!isPassable(feet.getType()) || !isPassable(head.getType())) {
            return "not enough free space at objective";
        }
        if (floor.getType().isAir()) {
            return "objective must be placed on solid ground";
        }

        for (String blockedName : plugin.getCoreConfig().blockedMaterialsNearObjective) {
            Material blocked = Material.matchMaterial(blockedName);
            if (blocked == null) {
                continue;
            }
            for (int x = -2; x <= 2; x++) {
                for (int yOffset = -1; yOffset <= 1; yOffset++) {
                    for (int z = -2; z <= 2; z++) {
                        if (location.getBlock().getRelative(x, yOffset, z).getType() == blocked) {
                            return "blocked material nearby: " + blocked.name().toLowerCase(Locale.ROOT);
                        }
                    }
                }
            }
        }

        if (!hasRequiredReachableEntries(clan, location)) {
            return "objective is not reachable from enough attack entries";
        }
        return null;
    }

    public List<Location> getEntryPoints(Location objectiveLocation) {
        return getEntryPoints(null, objectiveLocation);
    }

    public List<Location> getEntryPoints(ClanData clan, Location objectiveLocation) {
        if (objectiveLocation == null || objectiveLocation.getWorld() == null) {
            return Collections.emptyList();
        }

        int baseDistance = Math.max(6, plugin.getCoreConfig().attackerEntryDistance);
        int minDistance = Math.max(6, baseDistance - 6);
        int maxDistance = baseDistance + 6;

        Set<String> unique = new LinkedHashSet<String>();
        List<Location> entries = new ArrayList<Location>();

        for (int angle = 0; angle < 360; angle += 30) {
            double radians = Math.toRadians(angle);
            for (int distance = minDistance; distance <= maxDistance; distance += 2) {
                double x = objectiveLocation.getX() + Math.cos(radians) * distance;
                double z = objectiveLocation.getZ() + Math.sin(radians) * distance;
                Location probe = new Location(objectiveLocation.getWorld(), x, objectiveLocation.getY(), z);
                addEntryCandidate(clan, objectiveLocation, probe, unique, entries);
            }
        }

        if (entries.isEmpty()) {
            int d = baseDistance;
            addEntryCandidate(clan, objectiveLocation, objectiveLocation.clone().add(d, 0, 0), unique, entries);
            addEntryCandidate(clan, objectiveLocation, objectiveLocation.clone().add(-d, 0, 0), unique, entries);
            addEntryCandidate(clan, objectiveLocation, objectiveLocation.clone().add(0, 0, d), unique, entries);
            addEntryCandidate(clan, objectiveLocation, objectiveLocation.clone().add(0, 0, -d), unique, entries);
        }

        Collections.sort(entries, new Comparator<Location>() {
            @Override
            public int compare(Location a, Location b) {
                return Double.compare(a.distanceSquared(objectiveLocation), b.distanceSquared(objectiveLocation));
            }
        });

        return entries;
    }

    public List<Location> getPreviewEntryPoints(ClanData clan, Location objectiveLocation) {
        if (objectiveLocation == null || objectiveLocation.getWorld() == null) {
            return Collections.emptyList();
        }

        List<Location> candidates = getEntryPoints(clan, objectiveLocation);
        if (candidates.isEmpty()) {
            return candidates;
        }

        int maxPathDistance = Math.max(20, plugin.getCoreConfig().maxPathDistance);
        List<Location> reachable = new ArrayList<Location>();
        for (Location candidate : candidates) {
            if (hasReachablePath(candidate, objectiveLocation, maxPathDistance)) {
                reachable.add(candidate);
            }
        }
        if (reachable.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> doorEntries = new ArrayList<Location>();
        for (Location entry : reachable) {
            if (isDoorLikeEntry(entry)) {
                doorEntries.add(entry);
            }
        }
        if (!doorEntries.isEmpty()) {
            return limitPreviewEntries(doorEntries, objectiveLocation, 12);
        }

        List<Location> passageEntries = new ArrayList<Location>();
        for (Location entry : reachable) {
            if (isPassageEntry(entry)) {
                passageEntries.add(entry);
            }
        }
        if (!passageEntries.isEmpty()) {
            return limitPreviewEntries(passageEntries, objectiveLocation, 12);
        }

        return limitPreviewEntries(reachable, objectiveLocation, 8);
    }

    private List<Location> limitPreviewEntries(List<Location> entries, Location objective, int max) {
        if (entries.isEmpty()) {
            return entries;
        }

        List<Location> out = new ArrayList<Location>();
        Set<Integer> sectors = new HashSet<Integer>();
        Set<String> used = new LinkedHashSet<String>();

        for (Location entry : entries) {
            int sector = sectorOf(entry, objective);
            String key = keyOf(entry.getBlockX(), entry.getBlockY(), entry.getBlockZ());
            if (sectors.add(Integer.valueOf(sector)) && used.add(key)) {
                out.add(entry);
                if (out.size() >= max) {
                    return out;
                }
            }
        }

        for (Location entry : entries) {
            String key = keyOf(entry.getBlockX(), entry.getBlockY(), entry.getBlockZ());
            if (used.add(key)) {
                out.add(entry);
                if (out.size() >= max) {
                    break;
                }
            }
        }

        return out;
    }

    private boolean isDoorLikeEntry(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Block base = location.getBlock();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Material material = base.getRelative(x, y, z).getType();
                    if (isDoorMaterial(material)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPassageEntry(Location location) {
        if (!isWalkableSpot(location)) {
            return false;
        }

        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        int solidSides = 0;

        if (feet.getRelative(1, 0, 0).getType().isSolid() && head.getRelative(1, 0, 0).getType().isSolid()) {
            solidSides++;
        }
        if (feet.getRelative(-1, 0, 0).getType().isSolid() && head.getRelative(-1, 0, 0).getType().isSolid()) {
            solidSides++;
        }
        if (feet.getRelative(0, 0, 1).getType().isSolid() && head.getRelative(0, 0, 1).getType().isSolid()) {
            solidSides++;
        }
        if (feet.getRelative(0, 0, -1).getType().isSolid() && head.getRelative(0, 0, -1).getType().isSolid()) {
            solidSides++;
        }

        return solidSides >= 2;
    }

    private boolean isDoorMaterial(Material material) {
        if (material == null) {
            return false;
        }
        String name = material.name();
        return name.endsWith("_DOOR") || name.endsWith("_TRAPDOOR") || name.endsWith("_GATE");
    }
    private void addEntryCandidate(ClanData clan, Location objectiveLocation, Location probe, Set<String> unique, List<Location> entries) {
        if (probe == null || probe.getWorld() == null) {
            return;
        }
        Location adjusted = findNearestWalkable(probe, 5);
        if (adjusted == null) {
            return;
        }

        if (clan != null && !isInsideClaim(clan, adjusted, 0)) {
            return;
        }

        if (adjusted.distanceSquared(objectiveLocation) < 16.0D) {
            return;
        }

        String key = keyOf(adjusted.getBlockX(), adjusted.getBlockY(), adjusted.getBlockZ());
        if (unique.add(key)) {
            entries.add(adjusted);
        }
    }

    private boolean hasRequiredReachableEntries(ClanData clan, Location objectiveLocation) {
        int required = Math.max(1, plugin.getCoreConfig().requiredPaths);
        int maxPathDistance = Math.max(20, plugin.getCoreConfig().maxPathDistance);

        List<Location> candidates = getEntryPoints(clan, objectiveLocation);
        if (candidates.isEmpty()) {
            return false;
        }

        Set<Integer> sectors = new HashSet<Integer>();
        int checked = 0;
        for (Location entry : candidates) {
            if (checked >= 24) {
                break;
            }
            checked++;
            if (!hasReachablePath(entry, objectiveLocation, maxPathDistance)) {
                continue;
            }
            sectors.add(Integer.valueOf(sectorOf(entry, objectiveLocation)));
            if (sectors.size() >= required) {
                return true;
            }
        }
        return sectors.size() >= required;
    }

    private int sectorOf(Location entry, Location objective) {
        double angle = Math.toDegrees(Math.atan2(entry.getZ() - objective.getZ(), entry.getX() - objective.getX()));
        if (angle < 0) {
            angle += 360.0D;
        }
        return (int) Math.floor(angle / 45.0D);
    }

    private Location findNearestWalkable(Location center, int radius) {
        int verticalRange = 6;
        for (int r = 0; r <= radius; r++) {
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    Location check = center.clone().add(x, 0, z);
                    for (int y = -verticalRange; y <= verticalRange; y++) {
                        Location candidate = check.clone().add(0, y, 0);
                        if (isWalkableSpot(candidate)) {
                            return candidate;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean hasReachablePath(Location from, Location to, int maxDistance) {
        if (from == null || to == null || from.getWorld() == null || to.getWorld() == null) {
            return false;
        }
        if (!from.getWorld().getUID().equals(to.getWorld().getUID())) {
            return false;
        }

        Location start = findNearestWalkable(from, 2);
        Location goal = findNearestWalkable(to, 2);
        if (start == null || goal == null) {
            return false;
        }

        int sx = start.getBlockX();
        int sy = start.getBlockY();
        int sz = start.getBlockZ();

        int gx = goal.getBlockX();
        int gy = goal.getBlockY();
        int gz = goal.getBlockZ();

        int maxDistanceSq = maxDistance * maxDistance;
        if (horizontalDistanceSquared(sx, sz, gx, gz) > maxDistanceSq) {
            return false;
        }

        ArrayDeque<int[]> queue = new ArrayDeque<int[]>();
        Set<String> visited = new HashSet<String>();
        queue.add(new int[] {sx, sy, sz, 0});
        visited.add(keyOf(sx, sy, sz));

        int yMin = plugin.getCoreConfig().minObjectiveY - 24;
        int yMax = plugin.getCoreConfig().maxObjectiveY + 24;
        int maxSteps = Math.max(40, maxDistance * 2);
        int maxVisited = Math.max(1200, maxDistance * maxDistance * 8);

        final int[][] dirs = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty() && visited.size() <= maxVisited) {
            int[] node = queue.poll();
            int x = node[0];
            int y = node[1];
            int z = node[2];
            int steps = node[3];

            if (horizontalDistanceSquared(x, z, gx, gz) <= 2 && Math.abs(y - gy) <= 2) {
                return true;
            }
            if (steps >= maxSteps) {
                continue;
            }

            for (int[] dir : dirs) {
                int nx = x + dir[0];
                int nz = z + dir[1];

                for (int dy = 1; dy >= -2; dy--) {
                    int ny = y + dy;
                    if (ny < yMin || ny > yMax) {
                        continue;
                    }
                    if (horizontalDistanceSquared(nx, nz, sx, sz) > maxDistanceSq) {
                        continue;
                    }

                    Location candidate = new Location(from.getWorld(), nx + 0.5D, ny, nz + 0.5D);
                    if (!isWalkableSpot(candidate)) {
                        continue;
                    }

                    String key = keyOf(nx, ny, nz);
                    if (!visited.add(key)) {
                        continue;
                    }
                    queue.add(new int[] {nx, ny, nz, steps + 1});
                    break;
                }
            }
        }

        return false;
    }

    private int horizontalDistanceSquared(int x1, int z1, int x2, int z2) {
        int dx = x1 - x2;
        int dz = z1 - z2;
        return dx * dx + dz * dz;
    }

    private String keyOf(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }

    public boolean isWalkableSpot(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        Block feet = location.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        Block floor = feet.getRelative(0, -1, 0);
        return isPassable(feet.getType()) && isPassable(head.getType()) && !floor.getType().isAir();
    }

    private boolean isPassable(Material material) {
        if (material == null) {
            return false;
        }
        if (material.isAir()) {
            return true;
        }
        switch (material) {
            case WATER:
            case LAVA:
            case COBWEB:
                return false;
            default:
                return !material.isSolid();
        }
    }
public int getOnlineMemberCount(ClanData clan) {
        if (clan == null || clan.members == null || clan.members.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (String member : clan.members.keySet()) {
            try {
                Player player = Bukkit.getPlayer(UUID.fromString(member));
                if (player != null && player.isOnline()) {
                    count++;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return count;
    }

    public List<String> getOnlineMemberNames(ClanData clan) {
        List<String> names = new ArrayList<String>();
        if (clan == null || clan.members == null || clan.members.isEmpty()) {
            return names;
        }
        for (String member : clan.members.keySet()) {
            try {
                Player player = Bukkit.getPlayer(UUID.fromString(member));
                if (player != null && player.isOnline()) {
                    names.add(player.getName());
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        Collections.sort(names);
        return names;
    }

    public boolean canManageClan(ClanData clan, Player player) {
        ClanRole role = getRole(clan, player.getUniqueId());
        return role != null && role.canManageMembers();
    }

    public boolean setSiegeWindow(ClanData clan, DayOfWeek day, String startEnd) {
        if (clan == null || day == null || startEnd == null || !startEnd.contains("-")) {
            return false;
        }
        String key = day.name().toLowerCase(Locale.ROOT);
        List<String> values = clan.siegeWindows.get(key);
        if (values == null) {
            values = new ArrayList<String>();
            clan.siegeWindows.put(key, values);
        }
        values.clear();
        values.add(startEnd);
        return true;
    }

    public Map<String, List<String>> getSiegeWindows(ClanData clan) {
        if (clan == null || clan.siegeWindows == null || clan.siegeWindows.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> copy = new LinkedHashMap<String, List<String>>();
        for (Map.Entry<String, List<String>> entry : clan.siegeWindows.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
        }
        return copy;
    }

    public boolean deposit(ClanData clan, double amount) {
        if (clan == null || !Double.isFinite(amount) || amount <= 0.0D) {
            return false;
        }
        clan.bank += amount;
        return true;
    }

    public boolean withdraw(ClanData clan, double amount) {
        if (clan == null || !Double.isFinite(amount) || amount <= 0.0D || clan.bank < amount) {
            return false;
        }
        clan.bank -= amount;
        return true;
    }

    public boolean setClanChat(UUID player, boolean enabled) {
        PlayerProfile profile = getOrCreateProfile(player);
        profile.clanChatEnabled = enabled;
        return true;
    }

    public boolean isClanChatEnabled(UUID player) {
        return getOrCreateProfile(player).clanChatEnabled;
    }

    public Set<String> allClanIds() {
        return plugin.getStorage().data().clans.keySet();
    }

    public boolean setRelation(ClanData source, ClanData target, String relation) {
        if (source == null || target == null || source.id.equals(target.id)) {
            return false;
        }
        source.allies.remove(target.id);
        source.enemies.remove(target.id);
        if ("ally".equalsIgnoreCase(relation)) {
            source.allies.add(target.id);
        } else if ("enemy".equalsIgnoreCase(relation)) {
            source.enemies.add(target.id);
        }
        return true;
    }

    public String getClanDisplayTag(ClanData clan) {
        return clan == null ? plugin.getCoreConfig().placeholderNoClan : "[" + clan.tag + "]";
    }

    public String getClanNameByPlayer(UUID uuid) {
        PlayerProfile profile = getOrCreateProfile(uuid);
        ClanData clan = getClanById(profile.clanId);
        return clan == null ? plugin.getCoreConfig().placeholderNoClan : clan.name;
    }
}
