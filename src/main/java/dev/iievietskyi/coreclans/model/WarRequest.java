package dev.iievietskyi.coreclans.model;

public class WarRequest {
    public int id;
    public String attackerClanId;
    public String defenderClanId;
    public WarMode mode;
    public double stake;
    public long createdAt;
    public long expiresAt;
}
