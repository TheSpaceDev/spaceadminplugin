package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ProtectionPlugin extends JavaPlugin {
    private final Map<UUID, UUID> simulationMap = new HashMap<>();
    private final Set<UUID> borderEnabled = new HashSet<>();

    private RegionManager regionManager;

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
        this.getCommand("claim").setExecutor(new ClaimCommand(regionManager, simulationMap));
        this.getCommand("claim").setTabCompleter(new ClaimTabCompleter());
        this.getCommand("unclaim").setExecutor(new UnclaimCommand(regionManager));
        this.getCommand("unclaim").setTabCompleter(new UnclaimTabCompleter());
        this.getCommand("listclaims").setExecutor(new ListClaimsCommand(regionManager));
        this.getCommand("listclaims").setTabCompleter(new ListClaimsTabCompleter());
        this.getCommand("devsimulate").setExecutor(new DevSimulateCommand(simulationMap));
        this.getCommand("devsimulate").setTabCompleter(new DevSimulateTabCompleter());
        this.getCommand("claimborder").setExecutor(new ClaimBorderCommand(borderEnabled));
        this.getCommand("claimborder").setTabCompleter(new ClaimBorderTabCompleter());
        this.getCommand("claimwhitelist").setExecutor(new ClaimWhitelistCommand(regionManager));
        this.getCommand("claimwhitelist").setTabCompleter(new ClaimWhitelistTabCompleter());
    getServer().getPluginManager().registerEvents(new ProtectionListener(regionManager, simulationMap), this);
    this.getCommand("adminclaims").setExecutor(new de.joeakeem.spigotmc.plugin.spaceadmin.commands.AdminClaimsCommand(regionManager));
    getServer().getPluginManager().registerEvents(new de.joeakeem.spigotmc.plugin.spaceadmin.commands.AdminClaimsListener(regionManager), this);

        // Schedule particle border display for toggled players
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                UUID effectiveUUID = simulationMap.getOrDefault(uuid, uuid);
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
            }
        }, 20L, 40L); // every 2 seconds
    }

    private void showRegionBorder(org.bukkit.entity.Player player, Region region, org.bukkit.Particle particleType) {
        org.bukkit.Location c1 = region.getCorner1();
        org.bukkit.Location c2 = region.getCorner2();
        int minX = Math.min(c1.getBlockX(), c2.getBlockX());
        int maxX = Math.max(c1.getBlockX(), c2.getBlockX());
        int minZ = Math.min(c1.getBlockZ(), c2.getBlockZ());
        int maxZ = Math.max(c1.getBlockZ(), c2.getBlockZ());
        // Show border from y=-100 to y=200
        for (int y = -100; y <= 200; y += 5) {
            for (int x = minX; x <= maxX; x++) {
                player.spawnParticle(particleType, x + 0.5, y + 0.2, minZ + 0.5, 1);
                player.spawnParticle(particleType, x + 0.5, y + 0.2, maxZ + 0.5, 1);
            }
            for (int z = minZ; z <= maxZ; z++) {
                player.spawnParticle(particleType, minX + 0.5, y + 0.2, z + 0.5, 1);
                player.spawnParticle(particleType, maxX + 0.5, y + 0.2, z + 0.5, 1);
            }
        }
    }

    @Override
    public void onDisable() {
        regionManager.saveRegions();
    }
}
