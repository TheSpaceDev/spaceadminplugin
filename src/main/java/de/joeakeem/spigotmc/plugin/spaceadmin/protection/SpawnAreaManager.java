package de.joeakeem.spigotmc.plugin.spaceadmin.protection;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class SpawnAreaManager {
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
