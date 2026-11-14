package com.eminus.ingest.port;

/**
 * Maps loader block identity objects to voxel state ids for ingest. Modern {@link com.eminus.cell.StateTable} wraps
 * {@code BlockState}. Cleanroom uses {@code VetusStateLookup}.
 */
public interface VoxelStateLookup {
    int idOf(Object stateKey);
}