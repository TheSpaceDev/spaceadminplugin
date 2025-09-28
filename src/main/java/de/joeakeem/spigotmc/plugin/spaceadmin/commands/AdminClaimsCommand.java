package de.joeakeem.spigotmc.plugin.spaceadmin.commands;

import de.joeakeem.spigotmc.plugin.spaceadmin.protection.Region;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.RegionManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

public class AdminClaimsCommand implements CommandExecutor {
    private final RegionManager regionManager;

    public AdminClaimsCommand(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("You must be a server operator to use this command.");
            return true;
        }

        List<Region> regions = regionManager.getRegions();
        Inventory inv = Bukkit.createInventory(null, Math.max(9, ((regions.size() + 8) / 9) * 9), "All Claimed Regions");

        for (Region region : regions) {
            // Teleport option (left-click)
            ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
            ItemMeta teleportMeta = teleportItem.getItemMeta();
            teleportMeta.setDisplayName("Teleport to: " + Bukkit.getOfflinePlayer(region.getOwner()).getName());
            teleportMeta.setLore(List.of(
                "Corner1: " + region.getCorner1().getBlockX() + "," + region.getCorner1().getBlockZ(),
                "Corner2: " + region.getCorner2().getBlockX() + "," + region.getCorner2().getBlockZ(),
                "Left-click to teleport"
            ));
            teleportItem.setItemMeta(teleportMeta);
            inv.addItem(teleportItem);

            // Remove option (right-click)
            ItemStack removeItem = new ItemStack(Material.PAPER);
            ItemMeta removeMeta = removeItem.getItemMeta();
            removeMeta.setDisplayName("Remove: " + Bukkit.getOfflinePlayer(region.getOwner()).getName());
            removeMeta.setLore(List.of(
                "Corner1: " + region.getCorner1().getBlockX() + "," + region.getCorner1().getBlockZ(),
                "Corner2: " + region.getCorner2().getBlockX() + "," + region.getCorner2().getBlockZ(),
                "Right-click to remove"
            ));
            removeItem.setItemMeta(removeMeta);
            inv.addItem(removeItem);
        }

        player.openInventory(inv);
        return true;
    }
}
