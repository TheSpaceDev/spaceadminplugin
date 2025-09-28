package de.joeakeem.spigotmc.plugin.spaceadmin.protection;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
    private final RegionManager regionManager;
    private final Map<UUID, UUID> simulationMap;

    public ProtectionListener(RegionManager regionManager, Map<UUID, UUID> simulationMap) {
        this.regionManager = regionManager;
        this.simulationMap = simulationMap;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Block block = event.getClickedBlock();
        BlockState state = block.getState();
        if (state instanceof Chest) {
            Player player = event.getPlayer();
            UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
            Region region = regionManager.getRegionAt(block.getLocation());
            // BYPASS: If player is inside a stronghold, allow interaction
            Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
            if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
            if (region != null && !region.getOwner().equals(effectiveUUID) && !region.isWhitelisted(effectiveUUID)) {
                event.setCancelled(true);
                player.sendMessage("You cannot open chests in this claimed region.");
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
        Player player = event.getPlayer();
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
    // BYPASS: If player is inside a stronghold, allow breaking
    Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
    if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
        if (region != null) {
            if (!region.getOwner().equals(effectiveUUID) && !region.isWhitelisted(effectiveUUID)) {
                event.setCancelled(true);
                player.sendMessage("You cannot break blocks in this claimed region.");
            }
        }
    }

    // Admin claims command stub (actual implementation should be in a separate command class)
    // Example usage: /adminclaims to open GUI
    // You would need to implement a CommandExecutor and InventoryClickEvent listener for full GUI support.

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        UUID effectiveUUID = simulationMap.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Region region = regionManager.getRegionAt(event.getBlock().getLocation());
    // BYPASS: If player is inside a stronghold, allow placing
    Location stronghold = player.getWorld().locateNearestStructure(player.getLocation(), org.bukkit.StructureType.STRONGHOLD, 100, true);
    if (stronghold != null && player.getLocation().distance(stronghold) < 100) return;
        if (region != null) {
            if (!region.getOwner().equals(effectiveUUID) && !region.isWhitelisted(effectiveUUID)) {
                event.setCancelled(true);
                player.sendMessage("You cannot place blocks in this claimed region.");
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity exploder = event.getEntity();
        UUID effectiveUUID = null;
        if (exploder instanceof TNTPrimed) {
            Entity source = ((TNTPrimed) exploder).getSource();
            if (source instanceof Player) {
                Player sourcePlayer = (Player) source;
                effectiveUUID = simulationMap.getOrDefault(sourcePlayer.getUniqueId(), sourcePlayer.getUniqueId());
            }
        }
        // For each block, only cancel if the TNT placer is NOT owner or whitelisted
        for (org.bukkit.block.Block block : event.blockList()) {
            Location loc = block.getLocation();
            Region region = regionManager.getRegionAt(loc);
            if (region != null) {
                if (effectiveUUID == null || (!region.getOwner().equals(effectiveUUID) && !region.isWhitelisted(effectiveUUID))) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }
}
