package com.eminus.cell;

/**
 * Loader-neutral voxel state traits used by meshing and dimension runtimes.
 */
public interface CellStates extends StateOpacity, StateLayers {
    String DICTIONARY_NAME = "state";

    default StateOpacity seeThroughLeaves() {
        return this;
    }
}