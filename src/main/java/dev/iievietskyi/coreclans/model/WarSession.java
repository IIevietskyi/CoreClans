package dev.iievietskyi.coreclans.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class WarSession {
    public String id;
    public String attackerClanId;
    public String defenderClanId;
    public WarMode mode;
    public WarStatus status = WarStatus.WARMUP;

    public long createdAt;
    public long warmupEndsAt;
    public long endsAt;

    public int captureProgressSeconds;
    public int captureTargetSeconds;
    public double stakePot;

    public double crystalHp;
    public double crystalMaxHp;
    public String crystalWorld;
    public double crystalX;
    public double crystalY;
    public double crystalZ;
    public String crystalEntityUuid;

    public int attackersCap;

    public Queue<String> attackerQueue = new LinkedList<String>();
    public Set<String> activeAttackers = new HashSet<String>();

    public Map<String, Double> crystalDamageByPlayer = new HashMap<String, Double>();
    public Map<String, Integer> captureSecondsByPlayer = new HashMap<String, Integer>();
    public Map<String, Integer> killsByPlayer = new HashMap<String, Integer>();

    public String winnerClanId;
    public String endReason;
}
