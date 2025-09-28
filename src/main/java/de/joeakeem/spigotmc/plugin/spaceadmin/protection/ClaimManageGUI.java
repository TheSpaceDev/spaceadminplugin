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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;

public class ClaimManageGUI implements Listener, CommandExecutor {

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (addMemberMode.containsKey(uuid)) {
            event.setCancelled(true);
            String name = event.getMessage().trim();
            Region region = addMemberMode.remove(uuid);
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ProtectionPlugin"), () -> {
                @SuppressWarnings("deprecation")
                OfflinePlayer target = Bukkit.getOfflinePlayer(name);
                if (target == null || target.getUniqueId() == null) {
                    player.sendMessage("§cSpieler nicht gefunden.");
                } else if (region.getWhitelist().contains(target.getUniqueId())) {
                    player.sendMessage("§cSpieler ist bereits Mitglied.");
                } else {
                    region.addToWhitelist(target.getUniqueId());
                    player.sendMessage("§a" + target.getName() + " wurde zu deinem Claim hinzugefügt.");
                }
                openMembersGUI(player, region);
            });
        }
    }
    private static final Map<UUID, Integer> permsPage = new HashMap<>();
    private final RegionManager regionManager;
    private static final Map<UUID, Region> addMemberMode = new HashMap<>();

    public ClaimManageGUI(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Nur Spieler können diesen Befehl nutzen.");
            return true;
        }
        Player player = (Player) sender;
        List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
        if (regions.isEmpty()) {
            player.sendMessage("Du hast keinen Claim zum Verwalten.");
            return true;
        }
        Region region = regions.get(0);
        openMainGUI(player, region);
        return true;
    }


    public void openMainGUI(Player player, Region region) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eClaim Management");
        // Info
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§bClaim Info");
        infoMeta.setLore(List.of(
            "Welt: " + region.getCorner1().getWorld().getName(),
            "Ecke1: " + region.getCorner1().getBlockX() + "," + region.getCorner1().getBlockZ(),
            "Ecke2: " + region.getCorner2().getBlockX() + "," + region.getCorner2().getBlockZ()
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(10, info);
        // Members
        ItemStack members = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta membersMeta = members.getItemMeta();
        membersMeta.setDisplayName("§aMitglieder verwalten");
        membersMeta.setLore(List.of("Füge Spieler zu deinem Claim hinzu oder entferne sie."));
        members.setItemMeta(membersMeta);
        inv.setItem(12, members);
        // Permissions
        ItemStack perms = new ItemStack(Material.REPEATER);
        ItemMeta permsMeta = perms.getItemMeta();
        permsMeta.setDisplayName("§eBerechtigungen");
        permsMeta.setLore(List.of("Setze Berechtigungen für Mitglieder."));
        perms.setItemMeta(permsMeta);
        inv.setItem(14, perms);
        // Default permissions
        ItemStack defPerms = new ItemStack(Material.PAPER);
        ItemMeta defPermsMeta = defPerms.getItemMeta();
        defPermsMeta.setDisplayName("§bStandard-Berechtigungen");
        defPermsMeta.setLore(List.of("Setze Berechtigungen für Nicht-Mitglieder."));
        defPerms.setItemMeta(defPermsMeta);
        inv.setItem(16, defPerms);
        // Remove claim
        ItemStack remove = new ItemStack(Material.BARRIER);
        ItemMeta removeMeta = remove.getItemMeta();
        removeMeta.setDisplayName("§cClaim entfernen");
        removeMeta.setLore(List.of("Löscht deinen Claim dauerhaft."));
        remove.setItemMeta(removeMeta);
        inv.setItem(18, remove);
        player.openInventory(inv);
    }

    public void openMembersGUI(Player player, Region region) {
        Inventory inv = Bukkit.createInventory(null, 27, "§aMitglieder verwalten");
        int slot = 10;
        for (UUID member : region.getWhitelist()) {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            String displayName = offline.getName() != null ? offline.getName() : ("unbekannt [" + member.toString().substring(0, 8) + "]");
            meta.setDisplayName("§b" + displayName);
            meta.setLore(List.of("§cKlicke zum Entfernen", "§8UUID:" + member.toString()));
            head.setItemMeta(meta);
            inv.setItem(slot++, head);
        }
        // Add member item
        ItemStack add = new ItemStack(Material.GREEN_WOOL);
        ItemMeta addMeta = add.getItemMeta();
        addMeta.setDisplayName("§aMitglied hinzufügen");
        addMeta.setLore(List.of("Füge einen Spieler per Chat hinzu."));
        add.setItemMeta(addMeta);
        inv.setItem(22, add);
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        back.setItemMeta(backMeta);
        inv.setItem(26, back);
        player.openInventory(inv);
    }

    public void openPermissionsGUI(Player player, Region region) {
        if (region.getWhitelist().isEmpty()) {
            player.sendMessage("§cDu hast keine Mitglieder in deinem Claim, für die du Berechtigungen verwalten kannst.");
            return;
        }
        final int MEMBERS_PER_PAGE = 5;
        List<UUID> members = new ArrayList<>(region.getWhitelist());
        int totalPages = (int) Math.ceil(members.size() / (double) MEMBERS_PER_PAGE);
        int page = permsPage.getOrDefault(player.getUniqueId(), 0);
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        permsPage.put(player.getUniqueId(), page);

        int permCount = ClaimPermission.values().length;
        int rows = Math.max(1, Math.min(6, MEMBERS_PER_PAGE));
        int size = ((rows * 9) < 54) ? rows * 9 : 54;
        Inventory inv = Bukkit.createInventory(null, size, "§eBerechtigungen");

        int row = 0;
        int start = page * MEMBERS_PER_PAGE;
        int end = Math.min(start + MEMBERS_PER_PAGE, members.size());
        for (int i = start; i < end; i++) {
            UUID member = members.get(i);
            int base = row * 9;
            if (base >= inv.getSize()) break;
            OfflinePlayer offline = Bukkit.getOfflinePlayer(member);
            // Player head at start of row
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            String displayName = offline.getName() != null ? offline.getName() : ("unbekannt [" + member.toString().substring(0, 8) + "]");
            meta.setDisplayName("§b" + displayName);
            meta.setLore(Collections.singletonList("§7Mitglied"));
            head.setItemMeta(meta);
            inv.setItem(base, head);
            // Each permission as a button
            int col = 1;
            for (ClaimPermission perm : ClaimPermission.values()) {
                if (base + col >= inv.getSize()) break;
                boolean enabled = region.hasPermission(member, perm);
                ItemStack permItem = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
                ItemMeta permMeta = permItem.getItemMeta();
                permMeta.setDisplayName((enabled ? "§a" : "§c") + perm.name());
                List<String> lore = new ArrayList<>();
                lore.add("§7Klicke zum Umschalten");
                lore.add("§8MEMBER:" + member.toString());
                lore.add("§8PERM:" + perm.name());
                permMeta.setLore(lore);
                permItem.setItemMeta(permMeta);
                inv.setItem(base + col, permItem);
                col++;
            }
            row++;
        }
        // Back button (slot 18)
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        back.setItemMeta(backMeta);
        inv.setItem(size - 9, back);
        // Previous page button (slot 19)
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            prevMeta.setDisplayName("§eVorherige Seite");
            prev.setItemMeta(prevMeta);
            inv.setItem(size - 8, prev);
        }
        // Next page button (slot 25)
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            nextMeta.setDisplayName("§eNächste Seite");
            next.setItemMeta(nextMeta);
            inv.setItem(size - 2, next);
        }
        player.openInventory(inv);
    }

    public void openDefaultPermissionsGUI(Player player, Region region) {
        int permCount = ClaimPermission.values().length;
        int size = Math.max(9, Math.min(permCount + 2, 27));
        Inventory inv = Bukkit.createInventory(null, size, "§bStandard-Berechtigungen");
        int slot = 0;
        for (ClaimPermission perm : ClaimPermission.values()) {
            boolean enabled = region.hasDefaultPermission(perm);
            ItemStack permItem = new ItemStack(enabled ? Material.LIME_DYE : Material.GRAY_DYE);
            ItemMeta permMeta = permItem.getItemMeta();
            permMeta.setDisplayName((enabled ? "§a" : "§c") + perm.name());
            List<String> lore = new ArrayList<>();
            lore.add("§7Klicke zum Umschalten");
            lore.add("§8PERM:" + perm.name());
            permMeta.setLore(lore);
            permItem.setItemMeta(permMeta);
            inv.setItem(slot++, permItem);
        }
        // Back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        back.setItemMeta(backMeta);
        inv.setItem(size - 1, back);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
    if (title.equals("§eClaim Management")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();
            List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
            if (regions.isEmpty()) return;
            Region region = regions.get(0);
            if (name.equals("§aMitglieder verwalten")) {
                openMembersGUI(player, region);
            } else if (name.equals("§eBerechtigungen")) {
                openPermissionsGUI(player, region);
            } else if (name.equals("§bStandard-Berechtigungen")) {
                openDefaultPermissionsGUI(player, region);
            } else if (name.equals("§cClaim entfernen")) {
                regionManager.removeRegion(region);
                player.closeInventory();
                player.sendMessage("§cClaim entfernt.");
            }
        } else if (title.equals("§aMitglieder verwalten")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            String name = clicked.getItemMeta().getDisplayName();
            List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
            if (regions.isEmpty()) return;
            Region region = regions.get(0);
            if (name.equals("§aMitglied hinzufügen")) {
                player.closeInventory();
                player.sendMessage("§7Gib den Namen des Spielers im Chat ein:");
                addMemberMode.put(player.getUniqueId(), region);
            } else if (name.equals("§cZurück")) {
                openMainGUI(player, region);
            } else if (name.startsWith("§b")) {
                List<String> lore = clicked.getItemMeta().getLore();
                if (lore != null) {
                    String uuidLine = null;
                    for (String l : lore) {
                        if (l.startsWith("§8UUID:")) uuidLine = l;
                    }
                    if (uuidLine != null) {
                        try {
                            UUID memberUUID = UUID.fromString(uuidLine.substring("§8UUID:".length()));
                            region.removeFromWhitelist(memberUUID);
                            player.sendMessage("§cMitglied entfernt.");
                            openMembersGUI(player, region);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } else if (title.equals("§bStandard-Berechtigungen")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
            if (regions.isEmpty()) return;
            Region region = regions.get(0);
            ItemMeta meta = clicked.getItemMeta();
            String name = meta.getDisplayName();
            if (name.equals("§cZurück")) {
                openMainGUI(player, region);
                return;
            }
            List<String> lore = meta.getLore();
            if (lore != null) {
                String permLine = null;
                for (String l : lore) {
                    if (l.startsWith("§8PERM:")) permLine = l;
                }
                if (permLine != null) {
                    try {
                        ClaimPermission perm = ClaimPermission.valueOf(permLine.substring("§8PERM:".length()));
                        boolean current = region.hasDefaultPermission(perm);
                        region.setDefaultPermission(perm, !current);
                        openDefaultPermissionsGUI(player, region);
                    } catch (Exception ignored) {}
                }
            }
        } else if (title.equals("§eBerechtigungen")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;
            List<Region> regions = regionManager.getRegionsByOwner(player.getUniqueId());
            if (regions.isEmpty()) return;
            Region region = regions.get(0);
            ItemMeta meta = clicked.getItemMeta();
            String name = meta.getDisplayName();
            int page = permsPage.getOrDefault(player.getUniqueId(), 0);
            if (name.equals("§cZurück")) {
                permsPage.remove(player.getUniqueId());
                openMainGUI(player, region);
                return;
            } else if (name.equals("§eVorherige Seite")) {
                permsPage.put(player.getUniqueId(), page - 1);
                openPermissionsGUI(player, region);
                return;
            } else if (name.equals("§eNächste Seite")) {
                permsPage.put(player.getUniqueId(), page + 1);
                openPermissionsGUI(player, region);
                return;
            }
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() >= 3) {
                String memberLine = null, permLine = null;
                for (String l : lore) {
                    if (l.startsWith("§8MEMBER:")) memberLine = l;
                    if (l.startsWith("§8PERM:")) permLine = l;
                }
                if (memberLine != null && permLine != null) {
                    try {
                        UUID memberUUID = UUID.fromString(memberLine.substring("§8MEMBER:".length()));
                        ClaimPermission perm = ClaimPermission.valueOf(permLine.substring("§8PERM:".length()));
                        boolean current = region.hasPermission(memberUUID, perm);
                        region.setPermission(memberUUID, perm, !current);
                        openPermissionsGUI(player, region);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}

