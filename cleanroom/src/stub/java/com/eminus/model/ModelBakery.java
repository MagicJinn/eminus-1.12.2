package com.eminus.model;

import com.eminus.cell.Dictionary;

import net.minecraft.world.level.block.state.BlockState;

/**
 * Compile-time bakery surface for {@link ModelIndex}. Vetus baking uses {@link PlaceholderModelSource}.
 */
public final class ModelBakery implements ModelSource {
    public static final int MISSING = Dictionary.MISSING;
    public static final int NO_FLUID = -2;
    public static final int POSITIONAL = -4;
    public static final int NO_OVERLAY = -5;

    private static final int[] NO_PIECES = new int[0];

    private ModelBakery() {
    }

    @Override
    public int modelCount() {
        return 0;
    }

    @Override
    public BakedModel model(int modelId) {
        return null;
    }

    public boolean positional(BlockState state) {
        return false;
    }

    public boolean fluidOverlaid(BlockState state) {
        return false;
    }

    public int submergedModelId(int modelId) {
        return MISSING;
    }

    public int opaqueModelId(int modelId) {
        return modelId;
    }

    public int[] pieceIds(int modelId) {
        return NO_PIECES;
    }

    public int overlayModelId(int modelId) {
        return NO_OVERLAY;
    }

    public int oneSidedModelId(int modelId) {
        return modelId;
    }

    public int inwardModelId(int seabedModelId, int fluidModelId) {
        return seabedModelId;
    }

    public int fluidModelId(BlockState state) {
        return NO_FLUID;
    }

    public int request(BlockState state, Runnable whenBaked) {
        whenBaked.run();
        return MISSING;
    }

    public int positionalModelId(BlockState state, int blockX, int blockY, int blockZ, Runnable whenBaked) {
        return request(state, whenBaked);
    }

    public void stop() {
    }
}