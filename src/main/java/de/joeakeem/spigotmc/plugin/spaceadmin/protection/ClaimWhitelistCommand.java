package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class ClaimWhitelistCommand implements CommandExecutor {
    private final RegionManager regionManager;

    public ClaimWhitelistCommand(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        UUID owner = player.getUniqueId();
        if (args.length != 2 || !(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            sender.sendMessage("Usage: /claimwhitelist add <playername> or /claimwhitelist remove <playername>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage("Player not found.");
            return true;
        }
        List<Region> regions = regionManager.getRegionsByOwner(owner);
        if (regions.isEmpty()) {
            sender.sendMessage("You have no claimed regions.");
            return true;
        }
        for (Region region : regions) {
            if (args[0].equalsIgnoreCase("add")) {
                region.addToWhitelist(target.getUniqueId());
            } else {
                region.removeFromWhitelist(target.getUniqueId());
            }
        }
        sender.sendMessage("Whitelist updated for " + target.getName() + ".");
        return true;
    }
}
