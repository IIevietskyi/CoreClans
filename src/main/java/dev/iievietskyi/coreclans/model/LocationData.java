package dev.iievietskyi.coreclans.model;

public class LocationData {
    public String world;
    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;

    public LocationData() {
    }

    public LocationData(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }
}
