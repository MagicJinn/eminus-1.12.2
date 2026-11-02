package com.eminus.mesh;

import com.eminus.cell.VoxelEntry;

import net.minecraft.core.Direction;

final class FaceSide {
    final int edge;

    @SuppressWarnings("NullAway.Init")
    Direction towards;
    long entry;
    long facing;
    int x;
    int y;
    int z;
    int model;
    int drawn;
    boolean face;
    boolean border;

    FaceSide(int edge) {
        this.edge = edge;
    }

    void at(CellVoxels voxels, int atX, int atY, int atZ) {
        x = atX;
        y = atY;
        z = atZ;
        entry = voxels.entry(atX, atY, atZ);
        facing = entry;
    }

    void part(boolean liquid, MeshModels models, int voxelBlocks) {
        facing = liquid || VoxelEntry.isAir(entry) ? entry
                : LayerParts.block(entry, models.layerBlocks(VoxelEntry.state(entry)), voxelBlocks);
    }
}
