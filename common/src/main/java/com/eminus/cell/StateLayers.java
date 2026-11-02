package com.eminus.cell;

public interface StateLayers {
    int NOT_LAYERED = 0;

    int layered(int blockStateId, int fluidStateId, int blocks);

    int layerBlocks(int stateId);

    int layerBlock(int stateId);

    int layerFluid(int stateId);

    boolean holdsFluid(int stateId);
}
