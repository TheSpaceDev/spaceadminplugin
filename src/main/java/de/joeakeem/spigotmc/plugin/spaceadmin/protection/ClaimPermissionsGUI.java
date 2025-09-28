package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ClaimPermissionsGUI implements Listener, CommandExecutor {
    private final RegionManager regionManager;

    public ClaimPermissionsGUI(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
        if (regions.isEmpty()) {
            player.sendMessage("Du hast keine Grundstücke.");
            return true;
        }
        Region region = regions.get(0);
        openPermissionsGUI(player, region);
        return true;
    }

    public void openPermissionsGUI(Player player, Region region) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eGrundstück Berechtigungen");
        int slot = 10;
        for (UUID member : region.getAllMemberPermissions().keySet()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            meta.setDisplayName("§b" + offline.getName());
            List<String> lore = new ArrayList<>();
            for (ClaimPermission perm : ClaimPermission.values()) {
                boolean enabled = region.hasPermission(member, perm);
                lore.add((enabled ? "§a✔ " : "§c✖ ") + perm.name());
            }
            lore.add("");
            lore.add("§7Klicke, um Rechte zu ändern");
            meta.setLore(lore);
            head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§eClaim Permissions")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();
            if (!name.startsWith("§b")) return;
            String memberName = name.substring(2);
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberName);
            List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
            if (regions.isEmpty()) return;
            Region region = regions.get(0);
            // Cycle permissions for this member
            // For simplicity: toggle all perms at once (expandable)
            for (ClaimPermission perm : ClaimPermission.values()) {
                boolean current = region.hasPermission(member.getUniqueId(), perm);
                region.setPermission(member.getUniqueId(), perm, !current);
            }
            player.closeInventory();
            openPermissionsGUI(player, region);
        }
    }
}
