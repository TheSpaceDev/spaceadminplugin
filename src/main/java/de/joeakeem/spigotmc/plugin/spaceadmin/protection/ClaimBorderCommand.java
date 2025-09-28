package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Set;
import java.util.UUID;

public class ClaimBorderCommand implements CommandExecutor {
    private final Set<UUID> borderEnabled;

    public ClaimBorderCommand(Set<UUID> borderEnabled) {
        this.borderEnabled = borderEnabled;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl verwenden.");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        if (borderEnabled.contains(uuid)) {
            borderEnabled.remove(uuid);
            player.sendMessage("Die Grundstücks Grenzpartikel wurden deaktiviert.");
        } else {
            borderEnabled.add(uuid);
            player.sendMessage("Die Grundstücks Grenzpartikel wurden aktiviert.");
        }
        return true;
    }
}
