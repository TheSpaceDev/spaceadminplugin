
package de.joeakeem.spigotmc.plugin.spaceadmin.protection;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.SpawnAreaManager;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.SpawnAreaCommand;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProtectionPlugin extends JavaPlugin {
    public SpawnAreaManager getSpawnAreaManager() {
        return spawnAreaManager;
    }
    public Set<UUID> getBorderEnabledSet() {
        return borderEnabled;
    }
    private final Map<UUID, UUID> simulationMap = new HashMap<>();
    private final Set<UUID> borderEnabled = new HashSet<>();
    private RegionManager regionManager;
    private SpawnAreaManager spawnAreaManager;

    @Override
    public void onEnable() {
        // Enable claimborder by default for all online players
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
                borderEnabled.add(event.getPlayer().getUniqueId());
            }
        }, this);
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            borderEnabled.add(player.getUniqueId());
        }
        regionManager = new RegionManager();
        regionManager.loadRegions();
        spawnAreaManager = new SpawnAreaManager();
        this.getCommand("claim").setExecutor(new ClaimCommand(regionManager, simulationMap));
        this.getCommand("claim").setTabCompleter(new ClaimTabCompleter());
        this.getCommand("unclaim").setExecutor(new UnclaimCommand(regionManager));
        this.getCommand("unclaim").setTabCompleter(new UnclaimTabCompleter());
        this.getCommand("listclaims").setExecutor(new ListClaimsCommand(regionManager));
        this.getCommand("listclaims").setTabCompleter(new ListClaimsTabCompleter());
        
        this.getCommand("claimborder").setExecutor(new ClaimBorderCommand(borderEnabled));
        this.getCommand("claimborder").setTabCompleter(new ClaimBorderTabCompleter());
        ClaimManageGUI claimManageGUI = new ClaimManageGUI(regionManager);
        this.getCommand("claimmanage").setExecutor(claimManageGUI);
        getServer().getPluginManager().registerEvents(claimManageGUI, this);
    getServer().getPluginManager().registerEvents(new ProtectionListener(regionManager, simulationMap, spawnAreaManager), this);
        this.getCommand("adminclaims").setExecutor(new de.joeakeem.spigotmc.plugin.spaceadmin.commands.AdminClaimsCommand(regionManager));
        getServer().getPluginManager().registerEvents(new de.joeakeem.spigotmc.plugin.spaceadmin.commands.AdminClaimsListener(regionManager), this);
        this.getCommand("spawnarea").setExecutor(new SpawnAreaCommand(spawnAreaManager));
        // Schedule glass pillar display for toggled players
        getServer().getScheduler().runTaskTimer(this, new Runnable() {
            private boolean show = true;
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    // Show pillars for claims
                    for (Region region : regionManager.getRegions()) {
                        showOrHidePillars(region.getCorner1(), region.getCorner2(), player, show);
                    }
                    // Show pillars for spawn areas
                    if (spawnAreaManager != null) {
                        for (SpawnArea area : spawnAreaManager.getSpawnAreas()) {
                            showOrHidePillars(area.getCorner1(), area.getCorner2(), player, show);
                        }
                    }
                }
                show = !show;
            }
        }, 0L, 100L); // 5 seconds
    }

    // Helper to show/hide blue glass pillars at corners
    private void showOrHidePillars(org.bukkit.Location c1, org.bukkit.Location c2, org.bukkit.entity.Player player, boolean show) {
        org.bukkit.World world = c1.getWorld();
        int minX = Math.min(c1.getBlockX(), c2.getBlockX());
        int maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());
        org.bukkit.Material glass = org.bukkit.Material.BLUE_STAINED_GLASS;
        org.bukkit.Location[] corners = new org.bukkit.Location[] {
            new org.bukkit.Location(world, minX, world.getHighestBlockYAt(minX, minZ) + 1, minZ),
            new org.bukkit.Location(world, minX, world.getHighestBlockYAt(minX, maxZ) + 1, maxZ),
            new org.bukkit.Location(world, maxX, world.getHighestBlockYAt(maxX, minZ) + 1, minZ),
            new org.bukkit.Location(world, maxX, world.getHighestBlockYAt(maxX, maxZ) + 1, maxZ)
        };
        int pillarHeight = 20;
        for (org.bukkit.Location corner : corners) {
            for (int y = 0; y < pillarHeight; y++) {
                org.bukkit.Location blockLoc = corner.clone().add(0, y, 0);
                if (show) {
                    player.sendBlockChange(blockLoc, glass.createBlockData());
                } else {
                    player.sendBlockChange(blockLoc, blockLoc.getBlock().getBlockData());
                }
            }
        }
    }

    @Override
    public void onDisable() {
        regionManager.saveRegions();
    }
}
