package dev.iievietskyi.coreclans.model;

public class ClanObjective {
    public ObjectiveType type;
    public LocationData location;

    public ClanObjective() {
    }

    public ClanObjective(ObjectiveType type, LocationData location) {
        this.type = type;
        this.location = location;
    }
}
