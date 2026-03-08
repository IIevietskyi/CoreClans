package dev.iievietskyi.coreclans.model;

public class BountyContract {
    public String id;
    public String issuerClanId;
    public String targetClanId;
    public double reward;
    public long createdAt;
    public long expiresAt;
    public int progress;
    public int target;
    public boolean completed;
}
