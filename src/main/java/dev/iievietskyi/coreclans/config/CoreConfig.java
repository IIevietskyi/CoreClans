package dev.iievietskyi.coreclans.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class CoreConfig {
    public String prefix;

    public String storageFile;
    public int autosaveSeconds;

    public int maxMembers;
    public int minNameLength;
    public int maxNameLength;
    public String defaultTag;

    public int defaultClaimRadius;
    public int minClaimRadius;
    public int maxClaimRadius;
    public int objectiveBorderMargin;
    public int minObjectiveY;
    public int maxObjectiveY;

    public int warDurationMinutes;
    public int warWarmupSeconds;
    public int captureRadius;
    public int captureTargetSeconds;
    public double crystalDefaultHp;
    public int attackerEntryDistance;
    public int requiredPaths;
    public int maxPathDistance;
    public int attackerCapOffset;
    public boolean enableAttackerQueue;
    public List<String> blockedMaterialsNearObjective;

    public boolean antiZergEnabled;
    public int antiZergMaxPenaltyPercent;
    public Map<Integer, Integer> antiZergTiers;
    public boolean antiZergCrystalDamageScaling;
    public boolean antiZergCaptureSpeedScaling;
    public int antiZergUnderdogDefenseBonusPercent;

    public String seasonName;
    public int seasonLengthDays;
    public int bronzeMin;
    public int silverMin;
    public int goldMin;

    public boolean bountyEnabled;
    public int bountyDefaultDurationHours;

    public String placeholderNoClan;
    public String placeholderNoWar;

    public Map<String, List<String>> globalSiegeWindows = new java.util.LinkedHashMap<String, List<String>>();

    public static CoreConfig from(FileConfiguration cfg) {
        CoreConfig out = new CoreConfig();
        out.prefix = cfg.getString("prefix", "&6[CoreClans]&r ");

        out.storageFile = cfg.getString("storage.file", "coreclans-data.json");
        out.autosaveSeconds = Math.max(10, cfg.getInt("storage.autosave-seconds", 30));

        out.maxMembers = Math.max(2, cfg.getInt("clan.max-members", 25));
        out.minNameLength = Math.max(2, cfg.getInt("clan.min-name-length", 3));
        out.maxNameLength = Math.max(out.minNameLength, cfg.getInt("clan.max-name-length", 16));
        out.defaultTag = cfg.getString("clan.default-tag", "CORE");

        out.defaultClaimRadius = Math.max(16, cfg.getInt("claim.default-radius", 64));
        out.minClaimRadius = Math.max(8, cfg.getInt("claim.min-radius", 16));
        out.maxClaimRadius = Math.max(out.minClaimRadius, cfg.getInt("claim.max-radius", 256));
        out.defaultClaimRadius = Math.max(out.minClaimRadius, Math.min(out.maxClaimRadius, out.defaultClaimRadius));
        out.objectiveBorderMargin = Math.max(0, cfg.getInt("claim.objective-border-margin", 12));
        out.minObjectiveY = cfg.getInt("claim.min-y", 35);
        out.maxObjectiveY = cfg.getInt("claim.max-y", 180);

        out.warDurationMinutes = Math.max(5, cfg.getInt("war.duration-minutes", 30));
        out.warWarmupSeconds = Math.max(0, cfg.getInt("war.warmup-seconds", 15));
        out.captureRadius = Math.max(2, cfg.getInt("war.capture-radius", 5));
        out.captureTargetSeconds = Math.max(30, cfg.getInt("war.capture-target-seconds", 180));
        out.crystalDefaultHp = Math.max(50, cfg.getDouble("war.crystal-default-hp", 1000));
        out.attackerEntryDistance = Math.max(10, cfg.getInt("war.attacker-entry-distance", 22));
        out.requiredPaths = Math.max(1, cfg.getInt("war.required-paths", 2));
        out.maxPathDistance = Math.max(20, cfg.getInt("war.max-path-distance", 110));
        out.attackerCapOffset = Math.max(0, cfg.getInt("war.attacker-cap-offset", 2));
        out.enableAttackerQueue = cfg.getBoolean("war.enable-attacker-queue", true);
        out.blockedMaterialsNearObjective = cfg.getStringList("war.blocked-materials-near-objective");

        out.antiZergEnabled = cfg.getBoolean("anti-zerg.enabled", true);
        out.antiZergMaxPenaltyPercent = Math.max(0, cfg.getInt("anti-zerg.max-penalty-percent", 40));
        out.antiZergTiers = new java.util.TreeMap<Integer, Integer>();
        ConfigurationSection tiers = cfg.getConfigurationSection("anti-zerg.tiers");
        if (tiers != null) {
            for (String key : tiers.getKeys(false)) {
                try {
                    int diff = Integer.parseInt(key);
                    int penalty = tiers.getInt(key);
                    out.antiZergTiers.put(diff, penalty);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        if (out.antiZergTiers.isEmpty()) {
            out.antiZergTiers.put(Integer.valueOf(2), Integer.valueOf(10));
            out.antiZergTiers.put(Integer.valueOf(4), Integer.valueOf(20));
            out.antiZergTiers.put(Integer.valueOf(6), Integer.valueOf(30));
        }
        out.antiZergCrystalDamageScaling = cfg.getBoolean("anti-zerg.crystal-damage-scaling", true);
        out.antiZergCaptureSpeedScaling = cfg.getBoolean("anti-zerg.capture-speed-scaling", true);
        out.antiZergUnderdogDefenseBonusPercent = Math.max(0, cfg.getInt("anti-zerg.underdog-defense-bonus-percent", 8));

        out.seasonName = cfg.getString("season.name", "Season 1");
        out.seasonLengthDays = Math.max(7, cfg.getInt("season.length-days", 30));
        out.bronzeMin = cfg.getInt("season.divisions.bronze-min", 0);
        out.silverMin = cfg.getInt("season.divisions.silver-min", 500);
        out.goldMin = cfg.getInt("season.divisions.gold-min", 1200);

        out.bountyEnabled = cfg.getBoolean("bounty.enabled", true);
        out.bountyDefaultDurationHours = Math.max(1, cfg.getInt("bounty.default-duration-hours", 24));

        out.placeholderNoClan = cfg.getString("placeholders.no-clan", "-");
        out.placeholderNoWar = cfg.getString("placeholders.no-war", "none");

        ConfigurationSection windows = cfg.getConfigurationSection("siege-windows");
        if (windows != null) {
            String[] days = new String[] {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
            for (String day : days) {
                List<String> values = windows.getStringList(day);
                out.globalSiegeWindows.put(day, values == null ? Collections.<String>emptyList() : new ArrayList<String>(values));
            }
        }

        return out;
    }
}