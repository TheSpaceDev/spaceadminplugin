package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

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

public class ClaimManageCommand implements CommandExecutor {
    private final RegionManager regionManager;

    public ClaimManageCommand(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        List<Region> regions = regionManager.getRegionsByOwner(uuid);
        if (regions.isEmpty()) {
            player.sendMessage("Du hast kein Grundstück zum Verwalten.");
            return true;
        }
        Region region = regions.get(0); // Only one claim per player
        Inventory inv = Bukkit.createInventory(null, 27, "Grundstück verwalten");

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§bClaim Info");
        infoMeta.setLore(List.of(
            "Welt: " + region.getCorner1().getWorld().getName(),
            "Ecke 1: " + region.getCorner1().getBlockX() + "," + region.getCorner1().getBlockZ(),
            "Ecke 2: " + region.getCorner2().getBlockX() + "," + region.getCorner2().getBlockZ()
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(10, info);

        // Members item
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        membersMeta.setDisplayName("§aMitglieder verwalten");
        membersMeta.setLore(List.of("Füge Spieler zu deinem Grundstück hinzu oder entferne sie."));
        members.setItemMeta(membersMeta);
        inv.setItem(12, members);

        // Permissions item
        ItemStack perms = new ItemStack(Material.REPEATER);
        ItemMeta permsMeta = perms.getItemMeta();
        permsMeta.setDisplayName("§eBerechtigungen verwalten");
        permsMeta.setLore(List.of("Setze individuelle Berechtigungen für Mitglieder."));
        perms.setItemMeta(permsMeta);
        inv.setItem(14, perms);

        // Remove claim item
        ItemStack remove = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName("§cGrundstück löschen");
        removeMeta.setLore(List.of("Lösche dein Grundstück dauerhaft."));
        remove.setItemMeta(removeMeta);
        inv.setItem(16, remove);

        player.openInventory(inv);
        return true;
    }
}
