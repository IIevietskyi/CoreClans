package dev.iievietskyi.coreclans.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClanData {
    public String id;
    public String name;
    public String tag;
    public String ownerUuid;
    public Map<String, ClanRole> members = new HashMap<String, ClanRole>();
    public Set<String> invites = new HashSet<String>();
    public Set<String> allies = new HashSet<String>();
    public Set<String> enemies = new HashSet<String>();
    public double bank;
    public int points;
    public LocationData home;
    public LocationData claimCenter;
    public int claimRadius;
    public Map<ObjectiveType, ClanObjective> objectives = new HashMap<ObjectiveType, ClanObjective>();
    public Map<String, List<String>> siegeWindows = new HashMap<String, List<String>>();

    public ClanData() {
    }

    public List<String> memberUuids() {
        return new ArrayList<String>(members.keySet());
    }
}
