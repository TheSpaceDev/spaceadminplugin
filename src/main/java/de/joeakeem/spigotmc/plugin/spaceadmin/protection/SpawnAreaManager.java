package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class SpawnAreaManager {
    private final java.io.File saveFile = new java.io.File("plugins/spaceadminplugin/spawnareas.json");

    public void saveSpawnAreas() {
        try {
            org.json.JSONArray arr = new org.json.JSONArray();
            for (SpawnArea area : spawnAreas) {
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("world", area.getCorner1().getWorld().getName());
                obj.put("x1", area.getCorner1().getBlockX());
                obj.put("z1", area.getCorner1().getBlockZ());
                obj.put("x2", area.getCorner2().getBlockX());
                obj.put("z2", area.getCorner2().getBlockZ());
                obj.put("creator", area.getCreator().toString());
                arr.put(obj);
            }
            try (java.io.FileWriter fw = new java.io.FileWriter(saveFile)) {
                fw.write(arr.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadSpawnAreas(org.bukkit.Server server) {
        spawnAreas.clear();
        if (!saveFile.exists()) return;
        try {
            String json = new String(java.nio.file.Files.readAllBytes(saveFile.toPath()));
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                org.bukkit.World world = server.getWorld(obj.getString("world"));
                if (world == null) continue;
                org.bukkit.Location c1 = new org.bukkit.Location(world, obj.getInt("x1"), world.getHighestBlockYAt(obj.getInt("x1"), obj.getInt("z1")), obj.getInt("z1"));
                org.bukkit.Location c2 = new org.bukkit.Location(world, obj.getInt("x2"), world.getHighestBlockYAt(obj.getInt("x2"), obj.getInt("z2")), obj.getInt("z2"));
                java.util.UUID creator = java.util.UUID.fromString(obj.getString("creator"));
                spawnAreas.add(new SpawnArea(c1, c2, creator));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private final List<SpawnArea> spawnAreas = new ArrayList<>();
    private Location pendingStart = null;

    public void startArea(Location loc) {
        pendingStart = loc;
    }

    public boolean hasPending() {
        return pendingStart != null;
    }

    public Location getPendingStart() {
        return pendingStart;
    }

    public SpawnArea finishArea(Location loc, java.util.UUID creator) {
        if (pendingStart == null) return null;
        SpawnArea area = new SpawnArea(pendingStart, loc, creator);
        spawnAreas.add(area);
        pendingStart = null;
        return area;
    }

    public void deleteAll() {
        spawnAreas.clear();
    }

    public List<SpawnArea> getSpawnAreas() {
        return spawnAreas;
    }

    public boolean isInSpawnArea(Location loc) {
        for (SpawnArea area : spawnAreas) {
            if (area.contains(loc)) return true;
        }
        return false;
    }
}
