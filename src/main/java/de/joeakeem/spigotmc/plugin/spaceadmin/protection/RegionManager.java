package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class RegionManager {
    private final List<Region> regions = new ArrayList<>();
    private final File saveFile = new File("plugins/spaceadminplugin/regions.json");

    public void addRegion(Region region) {
        regions.add(region);
    }

    public void removeRegion(Region region) {
        regions.remove(region);
    }

    public List<Region> getRegions() {
        return regions;
    }

    public Region getRegionAt(Location loc) {
        for (Region region : regions) {
            if (region.contains(loc)) {
                return region;
            }
        }
        return null;
    }

    public List<Region> getRegionsByOwner(UUID owner) {
        List<Region> result = new ArrayList<>();
        for (Region region : regions) {
            if (region.getOwner().equals(owner)) {
                result.add(region);
            }
        }
        return result;
    }

    public void saveRegions() {
        // Ensure directory exists
        File dir = saveFile.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (PrintWriter out = new PrintWriter(new FileWriter(saveFile))) {
            out.println("[");
            for (int i = 0; i < regions.size(); i++) {
                Region region = regions.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append("  {");
                sb.append("\"owner\": \"").append(region.getOwner()).append("\",");
                sb.append("\"world\": \"").append(region.getCorner1().getWorld().getName()).append("\",");
                sb.append("\"corner1\": [").append(region.getCorner1().getX()).append(",").append(region.getCorner1().getY()).append(",").append(region.getCorner1().getZ()).append("],");
                sb.append("\"corner2\": [").append(region.getCorner2().getX()).append(",").append(region.getCorner2().getY()).append(",").append(region.getCorner2().getZ()).append("],");
                sb.append("\"whitelist\": [");
                int wcount = 0;
                for (UUID w : region.getWhitelist()) {
                    if (wcount++ > 0) sb.append(",");
                    sb.append("\"").append(w).append("\"");
                }
                sb.append("]");
                sb.append("}");
                if (i < regions.size() - 1) sb.append(",");
                out.println(sb.toString());
            }
            out.println("]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadRegions() {
        regions.clear();
        if (!saveFile.exists()) return;
        try (BufferedReader in = new BufferedReader(new FileReader(saveFile))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                json.append(line);
            }
            JSONArray arr = new JSONArray(json.toString());
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                UUID owner = UUID.fromString(obj.getString("owner"));
                World world = Bukkit.getWorld(obj.getString("world"));
                JSONArray c1 = obj.getJSONArray("corner1");
                JSONArray c2 = obj.getJSONArray("corner2");
                Location corner1 = new Location(world, c1.getDouble(0), c1.getDouble(1), c1.getDouble(2));
                Location corner2 = new Location(world, c2.getDouble(0), c2.getDouble(1), c2.getDouble(2));
                Region region = new Region(owner, corner1, corner2);
                JSONArray whitelist = obj.getJSONArray("whitelist");
                for (int w = 0; w < whitelist.length(); w++) {
                    region.addToWhitelist(UUID.fromString(whitelist.getString(w)));
                }
                regions.add(region);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
