package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnAreaCommand implements CommandExecutor {
    private final SpawnAreaManager spawnAreaManager;

    public SpawnAreaCommand(SpawnAreaManager spawnAreaManager) {
        this.spawnAreaManager = spawnAreaManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cNur Operatoren können diesen Befehl nutzen.");
            return true;
        }
        if (args.length == 0) {
            player.sendMessage("§eVerwendung: /spawnarea <start|finish|delete>");
            return true;
        }
        String sub = args[0].toLowerCase();
        if (sub.equals("start")) {
            spawnAreaManager.startArea(player.getLocation());
            player.sendMessage("§bErste Ecke des Spawnbereichs gesetzt. Benutze /spawnarea finish an der zweiten Ecke.");
        } else if (sub.equals("finish")) {
            if (!spawnAreaManager.hasPending()) {
                player.sendMessage("§cDu musst zuerst /spawnarea start ausführen.");
                return true;
            }
            SpawnArea area = spawnAreaManager.finishArea(player.getLocation(), player.getUniqueId());
            if (area != null) {
                player.sendMessage("§aSpawnbereich erfolgreich gesetzt.");
            } else {
                player.sendMessage("§cFehler beim Setzen des Spawnbereichs.");
            }
        } else if (sub.equals("delete")) {
            spawnAreaManager.deleteAll();
            player.sendMessage("§aAlle Spawnbereiche wurden gelöscht.");
        } else {
            player.sendMessage("§eVerwendung: /spawnarea <start|finish|delete>");
        }
        return true;
    }
}
