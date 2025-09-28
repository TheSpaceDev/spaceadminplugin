package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnclaimCommand implements CommandExecutor {
    private final RegionManager regionManager;

    public UnclaimCommand(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        Location loc = player.getLocation();
        Region region = regionManager.getRegionAt(loc);
        if (region == null) {
            player.sendMessage("No claimed region at your location.");
            return true;
        }
        if (!region.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("You do not own this region.");
            return true;
        }
        regionManager.removeRegion(region);
        player.sendMessage("Region unclaimed.");
        return true;
    }
}
