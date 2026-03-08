package dev.iievietskyi.coreclans.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreDataStore {
    public int lastWarRequestId = 0;
    public Map<String, ClanData> clans = new HashMap<String, ClanData>();
    public Map<String, PlayerProfile> players = new HashMap<String, PlayerProfile>();
    public Map<Integer, WarRequest> pendingWarRequests = new HashMap<Integer, WarRequest>();
    public Map<String, WarReplay> replays = new HashMap<String, WarReplay>();
    public Map<String, BountyContract> bounties = new HashMap<String, BountyContract>();
    public SeasonData season = new SeasonData();
    public List<String> recentWarIds = new ArrayList<String>();
}
