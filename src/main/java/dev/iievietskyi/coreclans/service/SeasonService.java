package dev.iievietskyi.coreclans.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;
import dev.iievietskyi.coreclans.model.Division;
import dev.iievietskyi.coreclans.model.SeasonData;

public class SeasonService {
    private final CoreClansPlugin plugin;

    public SeasonService(CoreClansPlugin plugin) {
        this.plugin = plugin;
        ensureSeason();
    }

    public void ensureSeason() {
        SeasonData season = plugin.getStorage().data().season;
        long now = System.currentTimeMillis();
        if (season.name == null || season.name.trim().isEmpty()) {
            season.name = plugin.getCoreConfig().seasonName;
        }
        if (season.startedAt <= 0L || season.endsAt <= season.startedAt) {
            season.startedAt = now;
            season.endsAt = now + plugin.getCoreConfig().seasonLengthDays * 24L * 60L * 60L * 1000L;
        }
        if (now > season.endsAt) {
            resetSeason(plugin.getCoreConfig().seasonName);
        }
    }

    public int getPoints(String clanId) {
        if (clanId == null) {
            return 0;
        }
        Integer points = plugin.getStorage().data().season.clanPoints.get(clanId);
        return points == null ? 0 : points.intValue();
    }

    public void addPoints(String clanId, int amount) {
        if (clanId == null || amount == 0) {
            return;
        }
        int updated = getPoints(clanId) + amount;
        plugin.getStorage().data().season.clanPoints.put(clanId, Integer.valueOf(updated));
        ClanData clan = plugin.getClanService().getClanById(clanId);
        if (clan != null) {
            clan.points = updated;
        }
    }

    public Division getDivision(int points) {
        if (points >= plugin.getCoreConfig().goldMin) {
            return Division.GOLD;
        }
        if (points >= plugin.getCoreConfig().silverMin) {
            return Division.SILVER;
        }
        return Division.BRONZE;
    }

    public Division getDivision(String clanId) {
        return getDivision(getPoints(clanId));
    }

    public long daysLeft() {
        long millis = Math.max(0L, plugin.getStorage().data().season.endsAt - System.currentTimeMillis());
        return millis / (24L * 60L * 60L * 1000L);
    }

    public void resetSeason(String newName) {
        SeasonData season = plugin.getStorage().data().season;
        season.name = newName == null || newName.isEmpty() ? plugin.getCoreConfig().seasonName : newName;
        season.startedAt = System.currentTimeMillis();
        season.endsAt = season.startedAt + plugin.getCoreConfig().seasonLengthDays * 24L * 60L * 60L * 1000L;
        season.clanPoints.clear();
        for (ClanData clan : plugin.getStorage().data().clans.values()) {
            clan.points = 0;
        }
    }

    public List<Map.Entry<String, Integer>> top(int limit) {
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(plugin.getStorage().data().season.clanPoints.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return Integer.compare(b.getValue().intValue(), a.getValue().intValue());
            }
        });
        if (limit <= 0 || list.size() <= limit) {
            return list;
        }
        return new ArrayList<Map.Entry<String, Integer>>(list.subList(0, limit));
    }

    public int rankOf(String clanId) {
        if (clanId == null) {
            return -1;
        }
        int rank = 1;
        for (Map.Entry<String, Integer> entry : top(0)) {
            if (clanId.equals(entry.getKey())) {
                return rank;
            }
            rank++;
        }
        return -1;
    }
}
