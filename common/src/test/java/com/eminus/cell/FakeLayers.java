package com.eminus.cell;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntPredicate;

public final class FakeLayers implements StateLayers {
    private static final int FIRST_LAYERED = 1 << 24;

    private record Layer(int block, int fluid, int blocks) {
    }

    private final IntPredicate fluids;
    private final Map<Layer, Integer> ids = new ConcurrentHashMap<>();
    private final Map<Integer, Layer> layers = new ConcurrentHashMap<>();

    public FakeLayers(IntPredicate fluids) {
        this.fluids = fluids;
    }

    @Override
    public synchronized int layered(int blockStateId, int fluidStateId, int blocks) {
        Layer layer = new Layer(blockStateId, layerFluid(fluidStateId), blocks);
        return ids.computeIfAbsent(layer, key -> {
            int id = FIRST_LAYERED + layers.size();
            layers.put(id, key);
            return id;
        });
    }

    @Override
    public int layerBlocks(int stateId) {
        Layer layer = layers.get(stateId);
        return layer == null ? NOT_LAYERED : layer.blocks();
    }

    @Override
    public int layerBlock(int stateId) {
        Layer layer = layers.get(stateId);
        return layer == null ? stateId : layer.block();
    }

    @Override
    public int layerFluid(int stateId) {
        Layer layer = layers.get(stateId);
        return layer == null ? stateId : layer.fluid();
    }

    @Override
    public boolean holdsFluid(int stateId) {
        return fluids.test(layerFluid(stateId));
    }
}
