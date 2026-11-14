package com.eminus.cell;

import net.minecraft.world.level.block.state.BlockState;

/** Type token for {@link com.eminus.client.mesh.MeshWiring}. Vetus uses {@link DictionaryStateTable}. */
public final class StateTable implements CellStates {
    public static final int FULL_OPACITY = 15;
    public BlockState state(int stateId) {
        return null;
    }

    @Override
    public int opacity(int stateId) {
        return 0;
    }

    @Override
    public int layered(int blockStateId, int fluidStateId, int blocks) {
        return StateLayers.NOT_LAYERED;
    }

    @Override
    public int layerBlocks(int stateId) {
        return StateLayers.NOT_LAYERED;
    }

    @Override
    public int layerBlock(int stateId) {
        return 0;
    }

    @Override
    public int layerFluid(int stateId) {
        return 0;
    }

    @Override
    public boolean holdsFluid(int stateId) {
        return false;
    }
}