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
            player.sendMessage("Die erste Ecke wurde gesetzt. Nutze /claim finish um die zweite Ecke festzulegen.");
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("finish")) {
            Location corner1 = firstCorner.get(uuid);
            if (corner1 == null) {
                player.sendMessage("Benutze zuerst /claim start");
                return true;
            }
            if (regionManager.getRegionsByOwner(effectiveUUID).size() >= 3) {
                player.sendMessage("Du kannst höchstens 3 Grunstücke claimen!");
                return true;
            }
            Location corner2 = player.getLocation();
            // Only allow claiming in overworld
            String worldName = corner1.getWorld().getName();
            if (!worldName.equalsIgnoreCase("world") && !worldName.equalsIgnoreCase("overworld")) {
                player.sendMessage("Du kannst nur Grundstücke in der Overworld claimen!");
                return true;
            }
            // ...existing code...
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
            //überprüfe auch, dass es keine spawnarea ist
            

            regionManager.addRegion(newRegion);
            regionManager.saveRegions();
            firstCorner.remove(uuid);
            player.sendMessage("Du hast das Grundstück erfolgreich geclaimed! Mit /claimborder kannst du die Grenze anzeigen/ausblenden und mit /claimmanage die Berechtigungen verwalten.");

            // Show claimborder for 30 seconds even if disabled
            org.bukkit.plugin.Plugin plugin = org.bukkit.Bukkit.getPluginManager().getPlugin("ProtectionPlugin");
            if (plugin instanceof ProtectionPlugin) {
                java.util.Set<UUID> borderEnabled = ((ProtectionPlugin) plugin).getBorderEnabledSet();
                if (!borderEnabled.contains(uuid)) {
                    borderEnabled.add(uuid);
                    org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> borderEnabled.remove(uuid), 20L * 30);
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
}
