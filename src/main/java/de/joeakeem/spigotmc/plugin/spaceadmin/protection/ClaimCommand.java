package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimCommand implements CommandExecutor {
    private final RegionManager regionManager;
    private final Map<UUID, UUID> simulationMap;
    private final Map<UUID, Location> firstCorner = new HashMap<>();

    public ClaimCommand(RegionManager regionManager) {
        this(regionManager, new HashMap<>());
    }

    public ClaimCommand(RegionManager regionManager, Map<UUID, UUID> simulationMap) {
        this.regionManager = regionManager;
        this.simulationMap = simulationMap;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
    Player player = (Player) sender;
    UUID uuid = player.getUniqueId();
    UUID effectiveUUID = simulationMap.getOrDefault(uuid, uuid);
        // Use simulated UUID if present
        if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
            firstCorner.put(uuid, player.getLocation());
            player.sendMessage("First corner set! Move to the second corner and use /claim finish.");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("finish")) {
            Location corner1 = firstCorner.get(uuid);
            if (corner1 == null) {
                player.sendMessage("Use /claim start first.");
                return true;
            }
            if (!regionManager.getRegionsByOwner(effectiveUUID).isEmpty()) {
                player.sendMessage("You can only claim one region.");
                return true;
            }
            Location corner2 = player.getLocation();
            // Only allow claiming in overworld
            String worldName = corner1.getWorld().getName();
            if (!worldName.equalsIgnoreCase("world") && !worldName.equalsIgnoreCase("overworld")) {
                player.sendMessage("You can only claim regions in the overworld.");
                return true;
            }
            // ...existing code...
            double x1 = Math.min(corner1.getX(), corner2.getX());
            double x2 = Math.max(corner1.getX(), corner2.getX());
            double z1 = Math.min(corner1.getZ(), corner2.getZ());
            double z2 = Math.max(corner1.getZ(), corner2.getZ());
            double area = (x2 - x1 + 1) * (z2 - z1 + 1);
            if (area > 2000) {
                player.sendMessage("Maximum region size is 2000 blocks (X x Z area). Your selection: " + (int)area);
                return true;
            }
            Region newRegion = new Region(effectiveUUID, corner1, corner2);
            for (Region existing : regionManager.getRegions()) {
                if (existing.overlaps(newRegion)) {
                    player.sendMessage("You cannot claim a region that overlaps with an existing claim.");
                    return true;
                }
            }
            regionManager.addRegion(newRegion);
            regionManager.saveRegions();
            firstCorner.remove(uuid);
            player.sendMessage("Region claimed!");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (firstCorner.containsKey(uuid)) {
                firstCorner.remove(uuid);
                player.sendMessage("Claim process cancelled.");
            } else {
                player.sendMessage("No claim process to cancel.");
            }
            return true;
        } else {
            player.sendMessage("Usage: /claim start | /claim finish | /claim cancel");
            return true;
        }
    }
}
