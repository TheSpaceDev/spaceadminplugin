package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import java.util.Map;
import java.util.UUID;

public class ProtectionListener implements Listener {
    // Helper to check if a block is a blue glass pillar at a claim or spawn area corner
    private boolean isProtectedGlassPillar(Location loc) {
        org.bukkit.Material glass = org.bukkit.Material.BLUE_STAINED_GLASS;
        if (loc.getBlock().getType() != glass) return false;
        // Check all claim corners
        for (Region region : regionManager.getRegions()) {
            Location[] corners = getCorners(region.getCorner1(), region.getCorner2());
            for (Location corner : corners) {
                if (corner.getWorld().equals(loc.getWorld()) && corner.getBlockX() == loc.getBlockX() && corner.getBlockZ() == loc.getBlockZ()) {
                    return true;
                }
            }
        }
        // Check all spawn area corners
        if (spawnAreaManager != null) {
            for (SpawnArea area : spawnAreaManager.getSpawnAreas()) {
                Location[] corners = getCorners(area.getCorner1(), area.getCorner2());
                for (Location corner : corners) {
                    if (corner.getWorld().equals(loc.getWorld()) && corner.getBlockX() == loc.getBlockX() && corner.getBlockZ() == loc.getBlockZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Location[] getCorners(Location c1, Location c2) {
        int minX = Math.min(c1.getBlockX(), c2.getBlockX());
        int maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());
        int minY = Math.min(c1.getBlockY(), c2.getBlockY());
        Location worldLoc = new Location(c1.getWorld(), 0, 0, 0);
        return new Location[] {
            new Location(c1.getWorld(), minX, minY, minZ),
            new Location(c1.getWorld(), minX, minY, maxZ),
            new Location(c1.getWorld(), maxX, minY, minZ),
            new Location(c1.getWorld(), maxX, minY, maxZ)
        };
    }
    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(player.getLocation()) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage("§cIm Spawnbereich darfst du nichts machen.");
        }
        // Prevent dropping blue glass from pillars
        if (isProtectedGlassPillar(event.getItemDrop().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cDu kannst diese Glassäule nicht droppen.");
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(player.getLocation()) && !player.isOp()) {
            event.setCancelled(true);
            player.sendMessage("§cIm Spawnbereich darfst du nichts machen.");
        }
        // Prevent picking up blue glass from pillars
        if (isProtectedGlassPillar(event.getItem().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(player.getLocation())) {
            event.setCancelled(true);
            // player.sendMessage("§cIm Spawnbereich kannst du keinen Schaden nehmen.");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player player = (Player) event.getDamager();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(player.getLocation())) {
            event.setCancelled(true);
            // player.sendMessage("§cIm Spawnbereich darfst du niemanden angreifen.");
        }
    }
    private final RegionManager regionManager;
    private final Map<UUID, UUID> simulationMap;
    private final SpawnAreaManager spawnAreaManager;

    public ProtectionListener(RegionManager regionManager, Map<UUID, UUID> simulationMap, SpawnAreaManager spawnAreaManager) {
        this.regionManager = regionManager;
        this.simulationMap = simulationMap;
        this.spawnAreaManager = spawnAreaManager;
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        // if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(event.getTo())) {
        //     event.setCancelled(true);
        //     player.sendMessage("§cDu darfst den Spawnbereich nicht verlassen/teleportieren.");
        //     return;
        // }
        if (event.getCause() != TeleportCause.ENDER_PEARL) return;
        if (player.hasPermission("luckperms.admin.bypassprotection")) return;
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getTo());
        Region fromRegion = regionManager.getRegionAt(event.getFrom());
        // Only check when entering a new region
        if (region != null && region != fromRegion && !region.getOwner().equals(effectiveUUID)) {
            if (!region.hasPermission(effectiveUUID, ClaimPermission.ENTER)) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst dieses Gebiet nicht mit einer Enderperle betreten.");
            }
        }
    }
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(event.getTo())) {
            // Only allow entering, not leaving
            if (!spawnAreaManager.isInSpawnArea(event.getFrom())) {
                // Entering spawn area is allowed
                return;
            } else {
                // Already in spawn area, allow movement
                return;
            }
        }
        if (player.hasPermission("luckperms.admin.bypassprotection")) return;
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getTo());
        Region fromRegion = regionManager.getRegionAt(event.getFrom());
        // Only check when entering a new region
        if (region != null && region != fromRegion && !region.getOwner().equals(effectiveUUID)) {
            if (!region.hasPermission(effectiveUUID, ClaimPermission.ENTER)) {
                event.setTo(event.getFrom());
                player.sendMessage("§cDu darfst dieses Gebiet nicht betreten.");
            }
        }
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(player.getLocation())) {
            event.setCancelled(true);
            // player.sendMessage("§cIm Spawnbereich darfst du nichts machen.");
            return;
        }
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        if (player.hasPermission("luckperms.admin.bypassprotection")) return;
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(block.getLocation());
        // BYPASS: If player is inside a stronghold, allow interaction
        Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
        if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
        if (region != null && !region.getOwner().equals(effectiveUUID)) {
            // Chest protection
            if (state instanceof Chest) {
                if (!region.hasPermission(effectiveUUID, ClaimPermission.CHEST)) {
                    event.setCancelled(true);
                    player.sendMessage("§cDu darfst in diesem Gebiet keine Kisten öffnen.");
                }
            }
            // Door protection
            else if (block.getType().toString().contains("DOOR")) {
                if (!region.hasPermission(effectiveUUID, ClaimPermission.DOOR)) {
                    event.setCancelled(true);
                    player.sendMessage("§cDu darfst in diesem Gebiet keine Türen öffnen.");
                }
            }
            // General interact protection
            else {
                if (!region.hasPermission(effectiveUUID, ClaimPermission.INTERACT)) {
                    event.setCancelled(true);
                    player.sendMessage("§cDu darfst hier nicht interagieren.");
                }
            }
        }
    }
    // Restrict /devsimulate visibility and execution
    // In DevSimulateCommand.java, add:
    // if (!player.isOp()) {
    //     player.sendMessage("You must be a server operator to use this command.");
    //     return true;
    // }
    // if (!player.getName().equalsIgnoreCase("thespacedev")) {
    //     player.sendMessage("Only 'thespacedev' can use this command.");
    //     return true;
    // }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Prevent breaking blue glass pillars
        if (isProtectedGlassPillar(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cDu kannst diese Glassäule nicht zerstören.");
        }
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cIm Spawnbereich darfst du nichts machen.");
            return;
        }
        if (player.hasPermission("luckperms.admin.bypassprotection")) return;
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
        // BYPASS: If player is inside a stronghold, allow breaking
        Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
        if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
        if (region != null && !region.getOwner().equals(effectiveUUID)) {
            if (!region.hasPermission(effectiveUUID, ClaimPermission.BUILD)) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst in diesem Gebiet keine Blöcke abbauen.");
            }
        }
    }

    // Admin claims command stub (actual implementation should be in a separate command class)
    // Example usage: /adminclaims to open GUI
    // You would need to implement a CommandExecutor and InventoryClickEvent listener for full GUI support.

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Prevent placing blue glass at pillar locations
        if (isProtectedGlassPillar(event.getBlock().getLocation())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cDu kannst hier keine Glassäule platzieren.");
        }
        Player player = event.getPlayer();
        if (player.isOp()) return;
        if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cIm Spawnbereich darfst du nichts machen.");
            return;
        }
        if (player.hasPermission("luckperms.admin.bypassprotection")) return;
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
        // BYPASS: If player is inside a stronghold, allow placing
        Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
        if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
        if (region != null && !region.getOwner().equals(effectiveUUID)) {
            if (!region.hasPermission(effectiveUUID, ClaimPermission.BUILD)) {
                event.setCancelled(true);
                player.sendMessage("§cDu darfst in diesem Gebiet keine Blöcke platzieren.");
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Prevent explosions in spawn area
        for (org.bukkit.block.Block block : event.blockList()) {
            if (spawnAreaManager != null && spawnAreaManager.isInSpawnArea(block.getLocation())) {
                event.setCancelled(true);
                return;
            }
        }
        Entity exploder = event.getEntity();
        UUID effectiveUUID = null;
        if (exploder instanceof TNTPrimed) {
            Entity source = ((TNTPrimed) exploder).getSource();
            if (source instanceof Player) {
                Player sourcePlayer = (Player) source;
                if (sourcePlayer.hasPermission("luckperms.admin.bypassprotection")) return;
                effectiveUUID = simulationMap.getOrDefault(sourcePlayer.getUniqueId(), sourcePlayer.getUniqueId());
            }
        }
        // For each block, only cancel if the TNT placer is NOT owner or whitelisted
        for (org.bukkit.block.Block block : event.blockList()) {
            Location loc = block.getLocation();
            Region region = regionManager.getRegionAt(loc);
            if (region != null) {
                if (effectiveUUID == null || (!region.getOwner().equals(effectiveUUID) && !region.hasPermission(effectiveUUID, ClaimPermission.BUILD))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
