package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class Region {
    // Default permissions for non-members
    private final EnumSet<ClaimPermission> defaultPermissions = EnumSet.noneOf(ClaimPermission.class);

    // Per-player permissions: UUID -> Set<ClaimPermission>
    private final Map<UUID, EnumSet<ClaimPermission>> memberPermissions = new HashMap<>();

    public void setPermission(UUID member, ClaimPermission perm, boolean value) {
        EnumSet<ClaimPermission> perms = memberPermissions.computeIfAbsent(member, k -> EnumSet.noneOf(ClaimPermission.class));
        if (value) {
            perms.add(perm);
        } else {
            perms.remove(perm);
        }
    }

    // Default permissions methods
    public void setDefaultPermission(ClaimPermission perm, boolean value) {
        if (value) {
            defaultPermissions.add(perm);
        } else {
            defaultPermissions.remove(perm);
        }
    }

    public boolean hasDefaultPermission(ClaimPermission perm) {
        return defaultPermissions.contains(perm);
    }

    public EnumSet<ClaimPermission> getDefaultPermissions() {
        return EnumSet.copyOf(defaultPermissions);
    }

    public boolean hasPermission(UUID member, ClaimPermission perm) {
        EnumSet<ClaimPermission> perms = memberPermissions.get(member);
        if (perms != null && perms.contains(perm)) {
            return true;
        }
        // If not a member, check default permissions
        return defaultPermissions.contains(perm);
    }

    public EnumSet<ClaimPermission> getPermissions(UUID member) {
        return memberPermissions.getOrDefault(member, EnumSet.noneOf(ClaimPermission.class));
    }

    public Map<UUID, EnumSet<ClaimPermission>> getAllMemberPermissions() {
        return memberPermissions;
    }

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
    public void addToWhitelist(UUID uuid) {
        whitelist.add(uuid);
        // Set all permissions to allowed by default
        EnumSet<ClaimPermission> perms = memberPermissions.computeIfAbsent(uuid, k -> EnumSet.noneOf(ClaimPermission.class));
        for (ClaimPermission perm : ClaimPermission.values()) {
            perms.add(perm);
        }
    }
    public void removeFromWhitelist(UUID uuid) {
        whitelist.remove(uuid);
        memberPermissions.remove(uuid);
    }
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
