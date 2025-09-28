package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;

public class DevSimulateCommand implements CommandExecutor {
    private final Map<UUID, UUID> simulationMap;

    public DevSimulateCommand(Map<UUID, UUID> simulationMap) {
        this.simulationMap = simulationMap;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            sender.sendMessage("You must be a server operator to use this command.");
            return true;
        }
        if (!player.getName().equalsIgnoreCase("thespacedev")) {
            sender.sendMessage("Only 'thespacedev' can use this command.");
            return true;
        }
        if (!player.hasPermission("spaceadmin.devsimulate")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length == 1) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (target == null || target.getUniqueId() == null) {
                sender.sendMessage("Player not found.");
                return true;
            }
            simulationMap.put(player.getUniqueId(), target.getUniqueId());
            sender.sendMessage("You are now simulating as " + target.getName());
            return true;
        } else if (args.length == 0) {
            simulationMap.remove(player.getUniqueId());
            sender.sendMessage("Simulation cleared. You are now yourself.");
            return true;
        } else {
            sender.sendMessage("Usage: /devsimulate <playername> or /devsimulate to reset");
            return true;
        }
    }
}
