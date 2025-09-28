
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
        // Schedule particle border display for toggled players
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                UUID effectiveUUID = simulationMap.getOrDefault(uuid, uuid);
                // Show claim borders if enabled
                if (borderEnabled.contains(uuid)) {
                    boolean hasOwnClaim = false;
                    for (Region region : regionManager.getRegions()) {
                        if (region.getOwner().equals(effectiveUUID)) {
                            showRegionBorder(player, region, org.bukkit.Particle.VILLAGER_HAPPY);
                            hasOwnClaim = true;
                        }
                    }
                    for (Region region : regionManager.getRegions()) {
                        if (!region.getOwner().equals(effectiveUUID)) {
                            showRegionBorder(player, region, org.bukkit.Particle.REDSTONE);
                        }
                    }
                }
                // Always show blue border for all spawn areas for all players
                if (spawnAreaManager != null) {
                    for (SpawnArea area : spawnAreaManager.getSpawnAreas()) {
                        showSpawnAreaBorder(player, area);
                    }
                }
            }
        }, 20L, 40L); // every 2 seconds

    }

    private void showSpawnAreaBorder(org.bukkit.entity.Player player, SpawnArea area) {
        org.bukkit.Location c1 = area.getCorner1();
        org.bukkit.Location c2 = area.getCorner2();
        int minX = Math.min(c1.getBlockX(), c2.getBlockX());
        int maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());
        // Show border from y=-100 to y=200
        org.bukkit.Particle.DustOptions blue = new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0, 102, 255), 1.0F);
        for (int y = -100; y <= 200; y += 5) {
            for (int x = minX; x <= maxX; x++) {
                player.spawnParticle(org.bukkit.Particle.REDSTONE, x + 0.5, y + 0.2, minZ + 0.5, 1, 0, 0, 0, 0, blue);
                player.spawnParticle(org.bukkit.Particle.REDSTONE, x + 0.5, y + 0.2, maxZ + 0.5, 1, 0, 0, 0, 0, blue);
            }
            for (int z = minZ; z <= maxZ; z++) {
                player.spawnParticle(org.bukkit.Particle.REDSTONE, minX + 0.5, y + 0.2, z + 0.5, 1, 0, 0, 0, 0, blue);
                player.spawnParticle(org.bukkit.Particle.REDSTONE, maxX + 0.5, y + 0.2, z + 0.5, 1, 0, 0, 0, 0, blue);
            }
        }
    }

    private void showRegionBorder(org.bukkit.entity.Player player, Region region, org.bukkit.Particle particleType) {
        org.bukkit.Location c1 = region.getCorner1();
        org.bukkit.Location c2 = region.getCorner2();
        int minX = Math.min(c1.getBlockX(), c2.getBlockX());
        int maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());
        // Show border from y=-100 to y=200
        Object data = null;
        if (particleType == org.bukkit.Particle.REDSTONE) {
            data = new org.bukkit.Particle.DustOptions(org.bukkit.Color.RED, 1.0F);
        }
        for (int y = -100; y <= 200; y += 5) {
            for (int x = minX; x <= maxX; x++) {
                if (data != null)
                    player.spawnParticle(particleType, x + 0.5, y + 0.2, minZ + 0.5, 1, 0, 0, 0, 0, data);
                else
                    player.spawnParticle(particleType, x + 0.5, y + 0.2, minZ + 0.5, 1);
                if (data != null)
                    player.spawnParticle(particleType, x + 0.5, y + 0.2, maxZ + 0.5, 1, 0, 0, 0, 0, data);
                else
                    player.spawnParticle(particleType, x + 0.5, y + 0.2, maxZ + 0.5, 1);
            }
            for (int z = minZ; z <= maxZ; z++) {
                if (data != null)
                    player.spawnParticle(particleType, minX + 0.5, y + 0.2, z + 0.5, 1, 0, 0, 0, 0, data);
                else
                    player.spawnParticle(particleType, minX + 0.5, y + 0.2, z + 0.5, 1);
                if (data != null)
                    player.spawnParticle(particleType, maxX + 0.5, y + 0.2, z + 0.5, 1, 0, 0, 0, 0, data);
                else
                    player.spawnParticle(particleType, maxX + 0.5, y + 0.2, z + 0.5, 1);
            }
        }
    }

    @Override
    public void onDisable() {
        regionManager.saveRegions();
    }
}
