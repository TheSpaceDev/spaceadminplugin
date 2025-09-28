package de.joeakeem.spigotmc.plugin.spaceadmin.commands;

import de.joeakeem.spigotmc.plugin.spaceadmin.protection.Region;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.RegionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;

public class AdminClaimsListener implements Listener {
    private final RegionManager regionManager;

    public AdminClaimsListener(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
    if (!event.getView().getTitle().equals("All Claimed Regions")) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String displayName = clicked.getItemMeta().getDisplayName();
        Player player = (Player) event.getWhoClicked();
        Region region = null;
        // Find region by coordinates in lore
        if (clicked.getItemMeta().getLore() != null && clicked.getItemMeta().getLore().size() >= 2) {
            String c1 = clicked.getItemMeta().getLore().get(0).replace("Corner1: ", "");
            String c2 = clicked.getItemMeta().getLore().get(1).replace("Corner2: ", "");
            String[] c1parts = c1.split(",");
            String[] c2parts = c2.split(",");
            int x1 = Integer.parseInt(c1parts[0]);
            int z1 = Integer.parseInt(c1parts[1]);
            int x2 = Integer.parseInt(c2parts[0]);
            int z2 = Integer.parseInt(c2parts[1]);
            for (Region r : regionManager.getRegions()) {
                if (r.getCorner1().getBlockX() == x1 && r.getCorner1().getBlockZ() == z1 &&
                    r.getCorner2().getBlockX() == x2 && r.getCorner2().getBlockZ() == z2) {
                    region = r;
                    break;
                }
            }
        }
        if (region != null) {
            if (displayName.startsWith("Teleport to:")) {
                double centerX = (region.getCorner1().getX() + region.getCorner2().getX()) / 2.0;
                double centerY = (region.getCorner1().getY() + region.getCorner2().getY()) / 2.0;
                double centerZ = (region.getCorner1().getZ() + region.getCorner2().getZ()) / 2.0;
                player.teleport(new org.bukkit.Location(region.getCorner1().getWorld(), centerX, centerY, centerZ));
                player.sendMessage("Teleported to region center.");
            } else if (displayName.startsWith("Remove:")) {
                regionManager.removeRegion(region);
                regionManager.saveRegions();
                player.sendMessage("Region removed.");
                player.closeInventory();
            }
        }
    }
}
