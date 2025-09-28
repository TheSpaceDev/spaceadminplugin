package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class ListClaimsCommand implements CommandExecutor {
    private final RegionManager regionManager;

    public ListClaimsCommand(RegionManager regionManager) {
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
            player.sendMessage("Du hast noch nichts geclaimed!");
        } else {
            player.sendMessage("Deine geclaimten Grundstücke:");
            for (Region region : regions) {
                player.sendMessage("- Welt: " + region.getCorner1().getWorld().getName() + 
                    " Von: " + region.getCorner1().getBlockX() + "," + region.getCorner1().getBlockY() + "," + region.getCorner1().getBlockZ() +
                    " Bis: " + region.getCorner2().getBlockX() + "," + region.getCorner2().getBlockY() + "," + region.getCorner2().getBlockZ());
            }
        }
        return true;
    }
}
