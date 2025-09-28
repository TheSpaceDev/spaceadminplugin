package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

public class Region {
    public boolean overlaps(Region other) {
        if (!corner1.getWorld().equals(other.corner1.getWorld())) return false;
        double minX1 = Math.min(corner1.getX(), corner2.getX());
        double maxX1 = Math.max(corner1.getX(), corner2.getX());
        double minZ1 = Math.min(corner1.getZ(), corner2.getZ());
        double maxZ1 = Math.max(corner1.getZ(), corner2.getZ());
        double minX2 = Math.min(other.corner1.getX(), other.corner2.getX());
        double maxX2 = Math.max(other.corner1.getX(), other.corner2.getX());
        double minZ2 = Math.min(other.corner1.getZ(), other.corner2.getZ());
        double maxZ2 = Math.max(other.corner1.getZ(), other.corner2.getZ());
        return maxX1 >= minX2 && minX1 <= maxX2 && maxZ1 >= minZ2 && minZ1 <= maxZ2;
    }
    private final UUID owner;
    private final Location corner1;
    private final Location corner2;
    private final Set<UUID> whitelist = new HashSet<>();

    public Region(UUID owner, Location corner1, Location corner2) {
        this.owner = owner;
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public UUID getOwner() { return owner; }
    public Location getCorner1() { return corner1; }
    public Location getCorner2() { return corner2; }

    public Set<UUID> getWhitelist() { return whitelist; }
    public void addToWhitelist(UUID uuid) { whitelist.add(uuid); }
    public void removeFromWhitelist(UUID uuid) { whitelist.remove(uuid); }
    public boolean isWhitelisted(UUID uuid) { return whitelist.contains(uuid); }

    public boolean contains(Location loc) {
        if (!loc.getWorld().equals(corner1.getWorld())) return false;
        double x1 = Math.min(corner1.getX(), corner2.getX());
        double x2 = Math.max(corner1.getX(), corner2.getX());
        double z1 = Math.min(corner1.getZ(), corner2.getZ());
        double z2 = Math.max(corner1.getZ(), corner2.getZ());
        double x = loc.getX(), z = loc.getZ();
        return x >= x1 && x <= x2 && z >= z1 && z <= z2;
    }
}
