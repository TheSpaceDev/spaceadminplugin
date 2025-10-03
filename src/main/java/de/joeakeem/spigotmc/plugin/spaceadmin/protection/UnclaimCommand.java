package de.joeakeem.spigotmc.plugin.spaceadmin.protection;
import de.joeakeem.spigotmc.plugin.spaceadmin.protection.ProtectionPlugin;

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
            player.sendMessage("Du hast hier kein Grundstück geclaimt. Du kannst /claimmanage benutzen, um deine Grundstücke von überall zu verwalten.");
            return true;
        }
        if (!region.getOwner().equals(player.getUniqueId())) {
            player.sendMessage("Dieses Grundstück gehört dir nicht.");
            return true;
        }
        regionManager.removeRegion(region);
        ProtectionPlugin plugin = (ProtectionPlugin) org.bukkit.Bukkit.getPluginManager().getPlugin("spaceadminplugin");
        if (plugin != null) {
            plugin.removeClaimPillars(region);
        }
        player.sendMessage("Grundstück erfolgreich ungeclaimed. Jeder kann es jetzt betreten, abbauen oder selbst beanspruchen.");
        return true;
    }
}
