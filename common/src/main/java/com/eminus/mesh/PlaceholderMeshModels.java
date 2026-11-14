package com.eminus.mesh;

import com.eminus.model.ModelSource;
import com.eminus.model.PlaceholderModelSource;

import net.minecraft.core.Direction;

/** Dictionary-state mesh path until full {@link com.eminus.cell.StateTable} baking on vetus. */
public final class PlaceholderMeshModels implements MeshModels {
    private final ModelSource models;

    public PlaceholderMeshModels(ModelSource models) {
        this.models = models;
    }

    @Override
    public int modelId(int stateId, Runnable whenBaked) {
        whenBaked.run();
        return 0;
    }

    @Override
    public int positionalModelId(int stateId, int blockX, int blockY, int blockZ, Runnable whenBaked) {
        return modelId(stateId, whenBaked);
    }

    @Override
    public int fluidModelId(int stateId, Runnable whenBaked) {
        return NO_FLUID;
    }

    @Override
    public int submergedModelId(int modelId) {
        return modelId;
    }

    @Override
    public int opaqueModelId(int modelId) {
        return modelId;
    }

    @Override
    public int[] pieceIds(int modelId) {
        return new int[0];
    }

    @Override
    public int overlayModelId(int modelId) {
        return NO_OVERLAY;
    }

    @Override
    public int oneSidedModelId(int modelId) {
        return modelId;
    }

    @Override
    public int inwardModelId(int seabedModelId, int fluidModelId) {
        return seabedModelId;
    }

    @Override
    public int metadata(int modelId) {
        return 0;
    }

    @Override
    public float inset(int modelId, int face) {
        return 0.0F;
    }

    @Override
    public int solidFaces(int modelId) {
        return 0;
    }

    @Override
    public boolean skipsFace(int stateId, int facingStateId, Direction face) {
        return false;
    }

    @Override
    public boolean overlaysFluid(int stateId, int facingStateId) {
        return false;
    }

    @Override
    public int tintRow(int modelId) {
        return 0;
    }

    @Override
    public boolean fillsHeight(int modelId) {
        return models instanceof PlaceholderModelSource;
    }

    @Override
    public int offset(int stateId, int blockX, int blockY, int blockZ) {
        return 0;
    }

    @Override
    public boolean holdsFluid(int stateId) {
        return false;
    }

    @Override
    public boolean sameFluid(int stateId, int otherStateId) {
        return false;
    }

    @Override
    public boolean water(int stateId) {
        return false;
    }

    @Override
    public float fluidHeight(int stateId) {
        return 0.0F;
    }

    @Override
    public int layerBlocks(int stateId) {
        return 16;
    }

    @Override
    public boolean solid(int stateId) {
        return stateId != AIR && stateId != 0;
    }

    @Override
    public int shade(int stateId) {
        return 15;
    }
}