package com.eminus.vetus.ingest;

import java.util.concurrent.ConcurrentHashMap;

import com.eminus.cell.VoxelEntry;
import com.eminus.ingest.port.VoxelStateLookup;

/**
 * Maps stable 1.12 block identity keys ({@code blockId|meta}) to voxel state ids for upstream ingest.
 */
public final class VetusStateLookup implements VoxelStateLookup {
    private final ConcurrentHashMap<String, Integer> ids = new ConcurrentHashMap<>();

  public VetusStateLookup() {
        ids.put("minecraft:air", VoxelEntry.AIR_STATE_ID);
    }

    @Override
    public int idOf(Object stateKey) {
        if (stateKey == null) {
            return VoxelEntry.AIR_STATE_ID;
        }
        String key = stateKey.toString();
        if ("minecraft:air".equals(key)) {
            return VoxelEntry.AIR_STATE_ID;
        }
        return ids.computeIfAbsent(key, ignored -> ids.size());
    }
}