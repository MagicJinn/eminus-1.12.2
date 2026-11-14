package com.eminus.cell;

import org.jspecify.annotations.Nullable;

/**
 * Dictionary-backed traits until a loader supplies full {@link StateTable} baking.
 */
public final class DictionaryStateTable implements CellStates {
    private final Dictionary<String> ids;

    public DictionaryStateTable(Dictionary<String> ids) {
        this.ids = ids;
        if (ids.value(VoxelEntry.AIR_STATE_ID) == null) {
            ids.register("minecraft:air");
        }
    }

    public @Nullable String name(int stateId) {
        return ids.value(stateId);
    }

    @Override
    public int opacity(int stateId) {
        return stateId == VoxelEntry.AIR_STATE_ID ? 0 : 15;
    }

    @Override
    public int layered(int blockStateId, int fluidStateId, int blocks) {
        return blockStateId;
    }

    @Override
    public int layerBlocks(int stateId) {
        return DetailLevel.VOXELS_PER_SIDE;
    }

    @Override
    public int layerBlock(int stateId) {
        return stateId;
    }

    @Override
    public int layerFluid(int stateId) {
        return -2;
    }

    @Override
    public boolean holdsFluid(int stateId) {
        return false;
    }
}