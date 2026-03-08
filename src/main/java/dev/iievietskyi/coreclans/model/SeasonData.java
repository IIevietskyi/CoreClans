package dev.iievietskyi.coreclans.model;

import java.util.HashMap;
import java.util.Map;

public class SeasonData {
    public String name;
    public long startedAt;
    public long endsAt;
    public Map<String, Integer> clanPoints = new HashMap<String, Integer>();
}
