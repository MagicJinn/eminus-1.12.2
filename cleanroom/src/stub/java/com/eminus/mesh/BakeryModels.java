package com.eminus.mesh;

import com.eminus.model.BakedModel;
import com.eminus.model.ModelIndex;
import com.eminus.model.ModelSource;

import net.minecraft.core.Direction;

/** Type token for {@link com.eminus.client.mesh.MeshWiring}. Vetus uses {@link PlaceholderMeshModels}. */
public final class BakeryModels implements MeshModels {
    private static final int NO_METADATA = 0;

    private final ModelIndex index;
    private final ModelSource models;

    public BakeryModels(ModelIndex index, ModelSource models) {
        this.index = index;
        this.models = models;
    }

    @Override
    public int modelId(int stateId, Runnable whenBaked) {
        return index.modelId(stateId, whenBaked);
    }

    @Override
    public int positionalModelId(int stateId, int blockX, int blockY, int blockZ, Runnable whenBaked) {
        return index.positionalModelId(stateId, blockX, blockY, blockZ, whenBaked);
    }

    @Override
    public int fluidModelId(int stateId, Runnable whenBaked) {
        return index.fluidModelId(stateId, whenBaked);
    }

    @Override
    public int submergedModelId(int modelId) {
        return index.submergedModelId(modelId);
    }

    @Override
    public int opaqueModelId(int modelId) {
        return index.opaqueModelId(modelId);
    }

    @Override
    public int[] pieceIds(int modelId) {
        return index.pieceIds(modelId);
    }

    @Override
    public int overlayModelId(int modelId) {
        return index.overlayModelId(modelId);
    }

    @Override
    public int oneSidedModelId(int modelId) {
        return index.oneSidedModelId(modelId);
    }

    @Override
    public int inwardModelId(int seabedModelId, int fluidModelId) {
        return index.inwardModelId(seabedModelId, fluidModelId);
    }

    @Override
    public int metadata(int modelId) {
        BakedModel model = models.model(modelId);
        return model == null ? NO_METADATA : model.metadata();
    }

    @Override
    public float inset(int modelId, int face) {
        BakedModel model = models.model(modelId);
        return model == null ? BakedModel.EMPTY_INSET : model.insets()[face];
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
        return false;
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
        return index.layerBlocks(stateId);
    }

    @Override
    public boolean solid(int stateId) {
        return stateId != AIR && stateId != 0;
    }

    @Override
    public int shade(int stateId) {
        return StateShade.PERMEABLE;
    }
}