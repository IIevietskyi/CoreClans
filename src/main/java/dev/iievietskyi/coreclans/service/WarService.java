package dev.iievietskyi.coreclans.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.ObjectiveType;
import dev.iievietskyi.coreclans.model.WarMode;
import dev.iievietskyi.coreclans.model.WarReplay;
import dev.iievietskyi.coreclans.model.WarRequest;
import dev.iievietskyi.coreclans.model.WarSession;
import dev.iievietskyi.coreclans.model.WarStatus;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class WarService {
    private final CoreClansPlugin plugin;

    private final Map<String, WarSession> activeWarsById = new HashMap<String, WarSession>();
    private final Map<String, String> clanToWarId = new HashMap<String, String>();

    private BukkitTask tickTask;

    public WarService(CoreClansPlugin plugin) {
        this.plugin = plugin;
        startTicker();
    }

    public void shutdown() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        for (WarSession session : activeWarsById.values()) {
            removeCrystalEntity(session);
        }
        activeWarsById.clear();
        clanToWarId.clear();
    }

    public WarRequest createChallenge(ClanData attacker, ClanData defender, WarMode mode, double stake) {
        if (attacker == null || defender == null || mode == null) {
            return null;
        }
        if (attacker.id.equals(defender.id)) {
            return null;
        }
        if (isClanInWar(attacker.id) || isClanInWar(defender.id)) {
            return null;
        }
        if (!plugin.getSiegeWindowService().isOpenNow(defender)) {
            return null;
        }
        if (plugin.getClanService().getObjective(defender, mode.requiredObjective()) == null) {
            return null;
        }

        WarRequest request = new WarRequest();
        request.id = ++plugin.getStorage().data().lastWarRequestId;
        request.attackerClanId = attacker.id;
        request.defenderClanId = defender.id;
        request.mode = mode;
        request.stake = Math.max(0.0D, stake);
        if (request.stake > 0.0D && (attacker.bank < request.stake || defender.bank < request.stake)) {
            return null;
        }
        request.createdAt = System.currentTimeMillis();
        request.expiresAt = request.createdAt + 5L * 60L * 1000L;

        plugin.getStorage().data().pendingWarRequests.put(Integer.valueOf(request.id), request);
        return request;
    }

    public WarRequest getWarRequest(int id) {
        return plugin.getStorage().data().pendingWarRequests.get(Integer.valueOf(id));
    }

    public boolean denyWarRequest(int id) {
        return plugin.getStorage().data().pendingWarRequests.remove(Integer.valueOf(id)) != null;
    }

    public WarSession acceptWarRequest(int requestId) {
        WarRequest request = plugin.getStorage().data().pendingWarRequests.get(Integer.valueOf(requestId));
        if (request == null) {
            return null;
        }
        if (request.expiresAt < System.currentTimeMillis()) {
            plugin.getStorage().data().pendingWarRequests.remove(Integer.valueOf(requestId));
            return null;
        }

        ClanData attacker = plugin.getClanService().getClanById(request.attackerClanId);
        ClanData defender = plugin.getClanService().getClanById(request.defenderClanId);
        if (attacker == null || defender == null) {
            return null;
        }
        if (attacker.id.equals(defender.id)) {
            return null;
        }
        if (isClanInWar(attacker.id) || isClanInWar(defender.id)) {
            return null;
        }
        if (!plugin.getSiegeWindowService().isOpenNow(defender)) {
            return null;
        }
        Location objectiveLocation = plugin.getClanService().getObjectiveLocation(defender, request.mode.requiredObjective());
        if (objectiveLocation == null || objectiveLocation.getWorld() == null) {
            return null;
        }
        if (request.stake > 0.0D && (attacker.bank < request.stake || defender.bank < request.stake)) {
            return null;
        }

        WarSession session = new WarSession();
        session.id = UUID.randomUUID().toString();
        session.attackerClanId = attacker.id;
        session.defenderClanId = defender.id;
        session.mode = request.mode;
        session.createdAt = System.currentTimeMillis();
        session.warmupEndsAt = session.createdAt + plugin.getCoreConfig().warWarmupSeconds * 1000L;
        session.endsAt = session.createdAt + plugin.getCoreConfig().warDurationMinutes * 60L * 1000L;
        session.captureTargetSeconds = plugin.getCoreConfig().captureTargetSeconds;
        session.crystalMaxHp = plugin.getCoreConfig().crystalDefaultHp;
        session.crystalHp = session.crystalMaxHp;
        session.attackersCap = Math.max(1, plugin.getClanService().getOnlineMemberCount(defender) + plugin.getCoreConfig().attackerCapOffset);

        if (session.mode == WarMode.CRYSTAL) {
            spawnCrystal(session, objectiveLocation);
        }

        if (request.stake > 0.0D) {
            attacker.bank -= request.stake;
            defender.bank -= request.stake;
            session.stakePot = request.stake * 2.0D;
        }

        activeWarsById.put(session.id, session);
        clanToWarId.put(session.attackerClanId, session.id);
        clanToWarId.put(session.defenderClanId, session.id);
        plugin.getStorage().data().pendingWarRequests.remove(Integer.valueOf(requestId));

        return session;
    }

    public boolean isClanInWar(String clanId) {
        return clanId != null && clanToWarId.containsKey(clanId);
    }

    public WarSession getWarByClan(String clanId) {
        if (clanId == null) {
            return null;
        }
        String warId = clanToWarId.get(clanId);
        return warId == null ? null : activeWarsById.get(warId);
    }

    public WarSession getWarByPlayer(Player player) {
        ClanData clan = plugin.getClanService().getClan(player);
        return clan == null ? null : getWarByClan(clan.id);
    }

    public boolean forfeit(String clanId) {
        WarSession session = getWarByClan(clanId);
        if (session == null) {
            return false;
        }
        String winner = clanId.equals(session.attackerClanId) ? session.defenderClanId : session.attackerClanId;
        endWar(session, winner, "forfeit");
        return true;
    }

    public int getQueuePosition(WarSession session, UUID playerId) {
        if (session == null || playerId == null) {
            return -1;
        }
        int pos = 1;
        for (String queued : session.attackerQueue) {
            if (playerId.toString().equals(queued)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    public double applyCrystalDamage(WarSession session, Player attacker, double baseDamage) {
        if (session == null || attacker == null || baseDamage <= 0 || session.mode != WarMode.CRYSTAL) {
            return 0.0D;
        }
        ClanData attackerClan = plugin.getClanService().getClan(attacker);
        if (attackerClan == null || !attackerClan.id.equals(session.attackerClanId)) {
            return 0.0D;
        }

        if (plugin.getCoreConfig().enableAttackerQueue && !session.activeAttackers.contains(attacker.getUniqueId().toString())) {
            return 0.0D;
        }

        double effectiveDamage = baseDamage;
        if (plugin.getCoreConfig().antiZergEnabled && plugin.getCoreConfig().antiZergCrystalDamageScaling) {
            int attackerOnline = plugin.getClanService().getOnlineMemberCount(plugin.getClanService().getClanById(session.attackerClanId));
            int defenderOnline = plugin.getClanService().getOnlineMemberCount(plugin.getClanService().getClanById(session.defenderClanId));
            int penalty = penaltyPercentForLargerSide(attackerOnline, defenderOnline, true);
            effectiveDamage = baseDamage * (1.0D - penalty / 100.0D);
        }

        session.crystalHp = Math.max(0.0D, session.crystalHp - effectiveDamage);
        String key = attacker.getUniqueId().toString();
        Double total = session.crystalDamageByPlayer.get(key);
        session.crystalDamageByPlayer.put(key, Double.valueOf((total == null ? 0.0D : total.doubleValue()) + effectiveDamage));

        if (session.crystalHp <= 0.0D) {
            endWar(session, session.attackerClanId, "crystal_destroyed");
        }
        return effectiveDamage;
    }

    public double applyUnderdogDamageModifier(Player attacker, Player victim, double damage) {
        if (attacker == null || victim == null || damage <= 0.0D || !plugin.getCoreConfig().antiZergEnabled) {
            return damage;
        }
        ClanData attackerClan = plugin.getClanService().getClan(attacker);
        ClanData victimClan = plugin.getClanService().getClan(victim);
        if (attackerClan == null || victimClan == null) {
            return damage;
        }
        WarSession war = getWarByClan(attackerClan.id);
        if (war == null || getWarByClan(victimClan.id) != war) {
            return damage;
        }
        int attackerOnline = plugin.getClanService().getOnlineMemberCount(attackerClan);
        int defenderOnline = plugin.getClanService().getOnlineMemberCount(victimClan);
        if (defenderOnline >= attackerOnline) {
            return damage;
        }

        double bonus = plugin.getCoreConfig().antiZergUnderdogDefenseBonusPercent / 100.0D;
        return damage * Math.max(0.0D, 1.0D - bonus);
    }

    public void recordKill(WarSession session, Player killer) {
        if (session == null || killer == null) {
            return;
        }
        String key = killer.getUniqueId().toString();
        Integer current = session.killsByPlayer.get(key);
        session.killsByPlayer.put(key, Integer.valueOf((current == null ? 0 : current.intValue()) + 1));
    }

    public void cleanupRequests() {
        Iterator<WarRequest> iterator = plugin.getStorage().data().pendingWarRequests.values().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            WarRequest request = iterator.next();
            if (request == null || request.expiresAt < now) {
                iterator.remove();
            }
        }
    }

    private void startTicker() {
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, 20L, 20L);
    }

    private void tick() {
        cleanupRequests();

        List<WarSession> sessions = new ArrayList<WarSession>(activeWarsById.values());
        long now = System.currentTimeMillis();

        for (WarSession session : sessions) {
            if (session.status == WarStatus.ENDED) {
                continue;
            }

            if (!plugin.getSiegeWindowService().isOpenNow(plugin.getClanService().getClanById(session.defenderClanId))) {
                endWar(session, session.defenderClanId, "siege_window_closed");
                continue;
            }

            if (session.status == WarStatus.WARMUP && now >= session.warmupEndsAt) {
                session.status = WarStatus.ACTIVE;
                broadcastWarMessage("&cWar is now ACTIVE: " + warTitle(session));
            }

            if (session.status != WarStatus.ACTIVE) {
                continue;
            }

            session.attackersCap = Math.max(1, plugin.getClanService().getOnlineMemberCount(plugin.getClanService().getClanById(session.defenderClanId)) + plugin.getCoreConfig().attackerCapOffset);
            refreshAttackerQueue(session);

            if (session.mode == WarMode.CAPTURE || session.mode == WarMode.BEACON) {
                tickCaptureMode(session);
            } else if (session.mode == WarMode.CRYSTAL && session.crystalHp <= 0.0D) {
                endWar(session, session.attackerClanId, "crystal_destroyed");
                continue;
            }

            if (now >= session.endsAt) {
                String winner;
                if (session.mode == WarMode.CAPTURE || session.mode == WarMode.BEACON) {
                    winner = session.captureProgressSeconds >= session.captureTargetSeconds ? session.attackerClanId : session.defenderClanId;
                } else {
                    winner = session.crystalHp <= 0.0D ? session.attackerClanId : session.defenderClanId;
                }
                endWar(session, winner, "time_limit");
            }
        }

        plugin.getBountyService().cleanupExpired();
    }

    private void refreshAttackerQueue(WarSession session) {
        session.activeAttackers.clear();
        session.attackerQueue.clear();

        ClanData attackerClan = plugin.getClanService().getClanById(session.attackerClanId);
        ClanData defenderClan = plugin.getClanService().getClanById(session.defenderClanId);
        if (attackerClan == null || defenderClan == null) {
            return;
        }

        Location objective = objectiveLocation(session);
        if (objective == null) {
            return;
        }

        List<Player> nearbyAttackers = nearbyClanMembers(attackerClan, objective, plugin.getCoreConfig().captureRadius + 10);
        Collections.sort(nearbyAttackers, new Comparator<Player>() {
            @Override
            public int compare(Player a, Player b) {
                return Double.compare(a.getLocation().distanceSquared(objective), b.getLocation().distanceSquared(objective));
            }
        });

        int cap = Math.max(1, session.attackersCap);
        for (int i = 0; i < nearbyAttackers.size(); i++) {
            Player player = nearbyAttackers.get(i);
            if (!plugin.getCoreConfig().enableAttackerQueue || i < cap) {
                session.activeAttackers.add(player.getUniqueId().toString());
            } else {
                session.attackerQueue.add(player.getUniqueId().toString());
                player.sendMessage(plugin.msg("war.queue", mapOf("position", String.valueOf(i - cap + 1))));
            }
        }
    }

    private void tickCaptureMode(WarSession session) {
        Location objective = objectiveLocation(session);
        if (objective == null || objective.getWorld() == null) {
            endWar(session, session.defenderClanId, "objective_missing");
            return;
        }

        ClanData attackerClan = plugin.getClanService().getClanById(session.attackerClanId);
        ClanData defenderClan = plugin.getClanService().getClanById(session.defenderClanId);
        if (attackerClan == null || defenderClan == null) {
            endWar(session, session.defenderClanId, "clan_missing");
            return;
        }

        List<Player> attackers = nearbyClanMembers(attackerClan, objective, plugin.getCoreConfig().captureRadius);
        List<Player> defenders = nearbyClanMembers(defenderClan, objective, plugin.getCoreConfig().captureRadius);

        if (plugin.getCoreConfig().enableAttackerQueue) {
            List<Player> filtered = new ArrayList<Player>();
            for (Player attacker : attackers) {
                if (session.activeAttackers.contains(attacker.getUniqueId().toString())) {
                    filtered.add(attacker);
                }
            }
            attackers = filtered;
        }

        showCaptureMarker(objective);

        if (attackers.isEmpty() || attackers.size() <= defenders.size()) {
            sendCaptureStatus(attackers, defenders, session);
            return;
        }

        double gain = 1.0D;
        if (plugin.getCoreConfig().antiZergEnabled && plugin.getCoreConfig().antiZergCaptureSpeedScaling) {
            int attackerOnline = plugin.getClanService().getOnlineMemberCount(attackerClan);
            int defenderOnline = plugin.getClanService().getOnlineMemberCount(defenderClan);
            int penalty = penaltyPercentForLargerSide(attackerOnline, defenderOnline, true);
            gain = Math.max(0.1D, 1.0D - penalty / 100.0D);
        }

        int gainedSeconds = (int) Math.max(1, Math.round(gain));
        session.captureProgressSeconds += gainedSeconds;

        for (Player attacker : attackers) {
            String key = attacker.getUniqueId().toString();
            Integer current = session.captureSecondsByPlayer.get(key);
            session.captureSecondsByPlayer.put(key, Integer.valueOf((current == null ? 0 : current.intValue()) + gainedSeconds));
        }

        sendCaptureStatus(attackers, defenders, session);

        if (session.captureProgressSeconds >= session.captureTargetSeconds) {
            endWar(session, session.attackerClanId, "capture_complete");
        }
    }

    private void showCaptureMarker(Location objective) {
        if (objective == null || objective.getWorld() == null) {
            return;
        }

        double radius = Math.max(1.5D, plugin.getCoreConfig().captureRadius - 0.5D);
        for (int angle = 0; angle < 360; angle += 45) {
            double radians = Math.toRadians(angle);
            double x = objective.getX() + 0.5D + Math.cos(radians) * radius;
            double z = objective.getZ() + 0.5D + Math.sin(radians) * radius;
            objective.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, x, objective.getY() + 1.1D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        objective.getWorld().spawnParticle(Particle.CRIT_MAGIC, objective.getX() + 0.5D, objective.getY() + 1.2D, objective.getZ() + 0.5D,
                4, 0.25D, 0.1D, 0.25D, 0.0D);
    }

    private void sendCaptureStatus(List<Player> attackers, List<Player> defenders, WarSession session) {
        if ((attackers == null || attackers.isEmpty()) && (defenders == null || defenders.isEmpty())) {
            return;
        }

        int percent = session.captureTargetSeconds <= 0 ? 0 : (int) Math.min(100,
                Math.round(session.captureProgressSeconds * 100.0D / session.captureTargetSeconds));
        String bar = Texts.color("&eObjective: &f" + session.captureProgressSeconds + "/" + session.captureTargetSeconds
                + " &8| &f" + percent + "% &8| &cA:" + (attackers == null ? 0 : attackers.size()) + " &9D:" + (defenders == null ? 0 : defenders.size()));

        java.util.Set<String> sent = new java.util.HashSet<String>();
        if (attackers != null) {
            for (Player player : attackers) {
                if (player != null && sent.add(player.getUniqueId().toString())) {
                    sendActionBar(player, bar);
                }
            }
        }
        if (defenders != null) {
            for (Player player : defenders) {
                if (player != null && sent.add(player.getUniqueId().toString())) {
                    sendActionBar(player, bar);
                }
            }
        }
    }

    private void sendActionBar(Player player, String message) {
        if (player == null || message == null) {
            return;
        }
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
    }

    private List<Player> nearbyClanMembers(ClanData clan, Location center, double radius) {
        List<Player> out = new ArrayList<Player>();
        if (clan == null || center == null || center.getWorld() == null) {
            return out;
        }

        double maxHorizontalDistanceSq = radius * radius;
        double maxVerticalDistance = Math.max(6.0D, radius + 2.0D);
        for (String memberUuid : clan.members.keySet()) {
            Player player;
            try {
                player = Bukkit.getPlayer(UUID.fromString(memberUuid));
            } catch (IllegalArgumentException ex) {
                continue;
            }
            if (player == null || !player.isOnline()) {
                continue;
            }
            if (!player.getWorld().getUID().equals(center.getWorld().getUID())) {
                continue;
            }

            Location playerLocation = player.getLocation();
            double dx = playerLocation.getX() - center.getX();
            double dz = playerLocation.getZ() - center.getZ();
            if (dx * dx + dz * dz > maxHorizontalDistanceSq) {
                continue;
            }
            if (Math.abs(playerLocation.getY() - center.getY()) > maxVerticalDistance) {
                continue;
            }
            out.add(player);
        }
        return out;
    }

    private Location objectiveLocation(WarSession session) {
        ObjectiveType required = session.mode.requiredObjective();
        return plugin.getClanService().getObjectiveLocation(plugin.getClanService().getClanById(session.defenderClanId), required);
    }

    private void spawnCrystal(WarSession session, Location objective) {
        if (objective == null || objective.getWorld() == null) {
            return;
        }
        Entity entity = objective.getWorld().spawnEntity(objective.clone().add(0.5D, 1.0D, 0.5D), EntityType.ENDER_CRYSTAL);
        if (entity instanceof EnderCrystal) {
            EnderCrystal crystal = (EnderCrystal) entity;
            crystal.setShowingBottom(false);
            crystal.setCustomName(Texts.color("&dClan Core"));
            crystal.setCustomNameVisible(true);
            session.crystalEntityUuid = crystal.getUniqueId().toString();
            session.crystalWorld = objective.getWorld().getName();
            session.crystalX = crystal.getLocation().getX();
            session.crystalY = crystal.getLocation().getY();
            session.crystalZ = crystal.getLocation().getZ();
        }
    }

    private void removeCrystalEntity(WarSession session) {
        if (session == null || session.crystalEntityUuid == null || session.crystalWorld == null) {
            return;
        }
        UUID entityId;
        try {
            entityId = UUID.fromString(session.crystalEntityUuid);
        } catch (IllegalArgumentException ex) {
            return;
        }
        Entity entity = Bukkit.getEntity(entityId);
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
    }

    public WarSession getWarByCrystalEntity(Entity entity) {
        if (entity == null) {
            return null;
        }
        String id = entity.getUniqueId().toString();
        for (WarSession session : activeWarsById.values()) {
            if (session.mode == WarMode.CRYSTAL && id.equals(session.crystalEntityUuid) && session.status != WarStatus.ENDED) {
                return session;
            }
        }
        return null;
    }

    private void endWar(WarSession session, String winnerClanId, String reason) {
        if (session == null || session.status == WarStatus.ENDED) {
            return;
        }
        session.status = WarStatus.ENDED;
        session.winnerClanId = winnerClanId;
        session.endReason = reason;
        removeCrystalEntity(session);

        ClanData winner = plugin.getClanService().getClanById(winnerClanId);
        ClanData loser = plugin.getClanService().getClanById(winnerClanId != null && winnerClanId.equals(session.attackerClanId) ? session.defenderClanId : session.attackerClanId);

        if (winner != null) {
            plugin.getSeasonService().addPoints(winner.id, 100);
            winner.bank += 100.0D;
            if (session.stakePot > 0.0D) {
                winner.bank += session.stakePot;
            }
        }
        if (loser != null) {
            plugin.getSeasonService().addPoints(loser.id, -30);
        }
        if (winner != null && loser != null) {
            plugin.getBountyService().onClanWarWin(winner.id, loser.id);
        }

        WarReplay replay = buildReplay(session);
        plugin.getStorage().data().replays.put(session.id, replay);
        plugin.getStorage().data().recentWarIds.add(0, session.id);
        while (plugin.getStorage().data().recentWarIds.size() > 20) {
            plugin.getStorage().data().recentWarIds.remove(plugin.getStorage().data().recentWarIds.size() - 1);
        }

        activeWarsById.remove(session.id);
        clanToWarId.remove(session.attackerClanId);
        clanToWarId.remove(session.defenderClanId);

        String winnerName = winner == null ? "none" : winner.name;
        broadcastWarMessage(plugin.getMessages().get("war.ended", mapOf("winner", winnerName, "reason", reason)));
    }

    private WarReplay buildReplay(WarSession session) {
        WarReplay replay = new WarReplay();
        replay.warId = session.id;
        replay.attackerClanId = session.attackerClanId;
        replay.defenderClanId = session.defenderClanId;
        replay.mode = session.mode;
        replay.winnerClanId = session.winnerClanId;
        replay.endReason = session.endReason;
        replay.startedAt = session.createdAt;
        replay.endedAt = System.currentTimeMillis();

        String mvp = null;
        double bestScore = -1.0D;

        Map<String, Double> scoreMap = new HashMap<String, Double>();
        for (Map.Entry<String, Double> entry : session.crystalDamageByPlayer.entrySet()) {
            scoreMap.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Integer> entry : session.captureSecondsByPlayer.entrySet()) {
            Double old = scoreMap.get(entry.getKey());
            scoreMap.put(entry.getKey(), Double.valueOf((old == null ? 0.0D : old.doubleValue()) + entry.getValue().doubleValue()));
        }
        for (Map.Entry<String, Integer> entry : session.killsByPlayer.entrySet()) {
            Double old = scoreMap.get(entry.getKey());
            scoreMap.put(entry.getKey(), Double.valueOf((old == null ? 0.0D : old.doubleValue()) + entry.getValue().doubleValue() * 25.0D));
        }

        for (Map.Entry<String, Double> entry : scoreMap.entrySet()) {
            if (entry.getValue().doubleValue() > bestScore) {
                bestScore = entry.getValue().doubleValue();
                mvp = entry.getKey();
            }
        }

        replay.mvpPlayer = mvp;
        replay.mvpScore = Math.max(0.0D, bestScore);
        replay.summary = "mode=" + session.mode.name().toLowerCase(Locale.ROOT)
                + ", capture=" + session.captureProgressSeconds + "/" + session.captureTargetSeconds
                + ", crystal=" + String.format(Locale.US, "%.1f", session.crystalHp) + "/" + String.format(Locale.US, "%.1f", session.crystalMaxHp)
                + ", attackersCap=" + session.attackersCap;
        return replay;
    }

    private int penaltyPercentForLargerSide(int attackerOnline, int defenderOnline, boolean attackerAction) {
        if (attackerOnline == defenderOnline) {
            return 0;
        }
        boolean attackerIsLarger = attackerOnline > defenderOnline;
        if ((attackerAction && !attackerIsLarger) || (!attackerAction && attackerIsLarger)) {
            return 0;
        }

        int diff = Math.abs(attackerOnline - defenderOnline);
        int penalty = 0;
        for (Map.Entry<Integer, Integer> tier : plugin.getCoreConfig().antiZergTiers.entrySet()) {
            if (diff >= tier.getKey().intValue()) {
                penalty = Math.max(penalty, tier.getValue().intValue());
            }
        }
        return Math.min(plugin.getCoreConfig().antiZergMaxPenaltyPercent, Math.max(0, penalty));
    }

    private void broadcastWarMessage(String message) {
        String colored = Texts.color(plugin.getCoreConfig().prefix + message);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(colored);
        }
    }

    private String warTitle(WarSession session) {
        ClanData attacker = plugin.getClanService().getClanById(session.attackerClanId);
        ClanData defender = plugin.getClanService().getClanById(session.defenderClanId);
        return (attacker == null ? "?" : attacker.name) + " vs " + (defender == null ? "?" : defender.name)
                + " (" + session.mode.name().toLowerCase(Locale.ROOT) + ")";
    }

    private Map<String, String> mapOf(String... values) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i + 1 < values.length; i += 2) {
            map.put(values[i], values[i + 1]);
        }
        return map;
    }
}
