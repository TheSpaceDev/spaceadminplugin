package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Add missing imports for RegionManager, Region, SpawnAreaManager, SpawnArea
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.RegionManager;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.Region;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.SpawnAreaManager;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.SpawnArea;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.ProtectionPlugin;

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
            player.sendMessage("Die erste Ecke wurde gesetzt. Nutze /claim finish um die zweite Ecke festzulegen.");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("finish")) {
            Location corner1 = firstCorner.get(uuid);
            if (corner1 == null) {
                player.sendMessage("Benutze zuerst /claim start");
                return true;
            }
            if (regionManager.getRegionsByOwner(effectiveUUID).size() >= 4) {
                player.sendMessage("Du kannst höchstens 4 Grunstücke claimen!");
                return true;
            }
            Location corner2 = player.getLocation();
            // Only allow claiming in overworld
            String worldName = corner1.getWorld().getName();
            if (!worldName.equalsIgnoreCase("world") && !worldName.equalsIgnoreCase("overworld")) {
                player.sendMessage("Du kannst nur Grundstücke in der Overworld claimen!");
                return true;
            }
            double x1 = Math.min(corner1.getX(), corner2.getX());
            double x2 = Math.max(corner1.getX(), corner2.getX());
            double z1 = Math.min(corner1.getZ(), corner2.getZ());
            double z2 = Math.max(corner1.getZ(), corner2.getZ());
            double area = (x2 - x1 + 1) * (z2 - z1 + 1);
            if (area > 5000) { //das entspricht x * z blocken
                player.sendMessage("Die maximale Fläche pro Grundstück beträgt 5.000 Blöcke. Du hast: " + (int)area);
                return true;
            }

            Region newRegion = new Region(effectiveUUID, corner1, corner2);
            for (Region existing : regionManager.getRegions()) {
                if (existing.overlaps(newRegion)) {
                    player.sendMessage("Dein Grundstück überlappt sich mit einem bestehenden Grundstück.");
                    return true;
                }
            }
            // Block claims that overlap or are inside any spawn area
            ProtectionPlugin plugin = (ProtectionPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("spaceadminplugin");
            if (plugin != null) {
                SpawnAreaManager spawnAreaManager = plugin.getSpawnAreaManager();
                if (spawnAreaManager != null) {
                    for (SpawnArea spawnArea : spawnAreaManager.getSpawnAreas()) {
                        if (regionOverlapsSpawnAreaStrict(newRegion, spawnArea)) {
                            player.sendMessage("Du kannst nicht im Spawnbereich oder überlappend mit dem Spawnbereich claimen.");
                            return true;
                        }
                    }
                }
            }

            regionManager.addRegion(newRegion);
            regionManager.saveRegions();
            firstCorner.remove(uuid);

            player.sendMessage("Du hast das Grundstück erfolgreich geclaimed! Mit /claimborder kannst du die Grenze anzeigen/ausblenden und mit /claimmanage die Berechtigungen verwalten.");

            // Show claimborder for 30 seconds even if disabled
            org.bukkit.plugin.Plugin protectionPlugin = org.bukkit.Bukkit.getPluginManager().getPlugin("ProtectionPlugin");
            if (protectionPlugin instanceof ProtectionPlugin) {
                java.util.Set<UUID> borderEnabled = ((ProtectionPlugin) protectionPlugin).getBorderEnabledSet();
                if (!borderEnabled.contains(uuid)) {
                    borderEnabled.add(uuid);
                    org.bukkit.Bukkit.getScheduler().runTaskLater(protectionPlugin, () -> borderEnabled.remove(uuid), 20L * 30);
                }
            }
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("cancel")) {
            if (firstCorner.containsKey(uuid)) {
                firstCorner.remove(uuid);
                player.sendMessage("Claim Vorgang abgebrochen");
            } else {
                player.sendMessage("Es gibt kein Claim Vorgang zum abbrechen.");
            }
            return true;
        } else {
            player.sendMessage("Command Nutzung: /claim start | /claim finish | /claim cancel");
            return true;
        }
    }

    // Helper to strictly check overlap between region and spawn area
    private boolean regionOverlapsSpawnAreaStrict(Region region, SpawnArea area) {
        Location r1 = region.getCorner1();
        Location r2 = region.getCorner2();
        Location s1 = area.getCorner1();
        Location s2 = area.getCorner2();
        if (!r1.getWorld().equals(s1.getWorld())) return false;
        double minX1 = Math.min(r1.getX(), r2.getX());
        double maxX1 = Math.max(r1.getX(), r2.getX());
        double minZ1 = Math.min(r1.getZ(), r2.getZ());
        double maxZ1 = Math.max(r1.getZ(), r2.getZ());
        double minX2 = Math.min(s1.getX(), s2.getX());
        double maxX2 = Math.max(s1.getX(), s2.getX());
        double minZ2 = Math.min(s1.getZ(), s2.getZ());
        double maxZ2 = Math.max(s1.getZ(), s2.getZ());
        // Strict overlap: any intersection at all blocks claim
        boolean xOverlap = maxX1 >= minX2 && minX1 <= maxX2;
        boolean zOverlap = maxZ1 >= minZ2 && minZ1 <= maxZ2;
        return xOverlap && zOverlap;
    }
}
