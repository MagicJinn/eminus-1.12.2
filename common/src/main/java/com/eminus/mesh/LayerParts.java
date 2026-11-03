package com.eminus.mesh;

import com.eminus.cell.StateLayers;
import com.eminus.cell.VoxelEntry;

final class LayerParts {
    static long block(long entry, int blocks, int voxelBlocks) {
        if (blocks == StateLayers.NOT_LAYERED) {
            return entry;
        }

        int highGap = VoxelEntry.highGap(entry);
        return VoxelEntry.withGaps(entry, voxelBlocks - highGap - blocks, highGap);
    }

    static long fluid(long entry, int blocks) {
        return blocks == StateLayers.NOT_LAYERED ? entry
                : VoxelEntry.withGaps(entry, VoxelEntry.lowGap(entry), VoxelEntry.highGap(entry) + blocks);
    }

    private LayerParts() {
    }
}
