package dev.iievietskyi.coreclans.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dev.iievietskyi.coreclans.CoreClansPlugin;
import dev.iievietskyi.coreclans.model.ClanData;

public class SiegeWindowService {
    private final CoreClansPlugin plugin;

    public SiegeWindowService(CoreClansPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean isOpenNow(ClanData clan) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();

        List<String> windows = getWindowsForDay(clan, day);
        if (windows.isEmpty()) {
            return false;
        }

        for (String range : windows) {
            TimeRange parsed = parse(range);
            if (parsed != null && parsed.contains(time)) {
                return true;
            }
        }
        return false;
    }

    public String nextWindowText(ClanData clan) {
        LocalDateTime now = LocalDateTime.now();
        for (int offset = 0; offset < 7; offset++) {
            DayOfWeek day = now.plusDays(offset).getDayOfWeek();
            List<String> windows = getWindowsForDay(clan, day);
            if (!windows.isEmpty()) {
                return day.name().toLowerCase(Locale.ROOT) + " " + windows.get(0);
            }
        }
        return "none";
    }

    private List<String> getWindowsForDay(ClanData clan, DayOfWeek day) {
        String key = day.name().toLowerCase(Locale.ROOT);
        if (clan != null && clan.siegeWindows != null && clan.siegeWindows.containsKey(key)) {
            return clan.siegeWindows.get(key);
        }
        List<String> fallback = plugin.getCoreConfig().globalSiegeWindows.get(key);
        return fallback == null ? Collections.<String>emptyList() : new ArrayList<String>(fallback);
    }

    private TimeRange parse(String value) {
        if (value == null || !value.contains("-")) {
            return null;
        }
        String[] parts = value.split("-", 2);
        try {
            return new TimeRange(LocalTime.parse(parts[0]), LocalTime.parse(parts[1]));
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class TimeRange {
        private final LocalTime start;
        private final LocalTime end;

        private TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        private boolean contains(LocalTime value) {
            if (start.equals(end)) {
                return true;
            }
            if (end.isAfter(start)) {
                return !value.isBefore(start) && value.isBefore(end);
            }
            return !value.isBefore(start) || value.isBefore(end);
        }
    }
}
