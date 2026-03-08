package dev.iievietskyi.coreclans.service;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import dev.iievietskyi.coreclans.model.LocationData;

public final class LocationCodec {
    private LocationCodec() {
    }

    public static LocationData from(Location location) {
        if (location == null || location.getWorld() == null) {
            return null;
        }
        return new LocationData(
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    public static Location to(LocationData data) {
        if (data == null || data.world == null) {
            return null;
        }
        World world = Bukkit.getWorld(data.world);
        if (world == null) {
            return null;
        }
        return new Location(world, data.x, data.y, data.z, data.yaw, data.pitch);
    }
}
