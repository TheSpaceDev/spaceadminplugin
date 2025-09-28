package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import java.util.UUID;

public class SpawnArea {
    private final Location corner1;
    private final Location corner2;
    private final UUID creator;

    public SpawnArea(Location corner1, Location corner2, UUID creator) {
        this.corner1 = corner1;
        this.corner2 = corner2;
        this.creator = creator;
    }

    public Location getCorner1() { return corner1; }
    public Location getCorner2() { return corner2; }
    public UUID getCreator() { return creator; }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(corner1.getWorld())) return false;
        double minX = Math.min(corner1.getX(), corner2.getX());
        double maxX = Math.max(corner1.getX(), corner2.getX());
        double minY = Math.min(corner1.getY(), corner2.getY());
        double maxY = Math.max(corner1.getY(), corner2.getY());
        double minZ = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ = Math.max(corner1.getZ(), corner2.getZ());
        return loc.getX() >= minX && loc.getX() <= maxX
            && loc.getY() >= minY && loc.getY() <= maxY
            && loc.getZ() >= minZ && loc.getZ() <= maxZ;
    }
}
