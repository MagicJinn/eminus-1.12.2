package com.eminus.model;

import com.eminus.cell.Dictionary;

/**
 * Single magenta model for loaders without block model baking yet (Cleanroom vetus placeholder path).
 */
public final class PlaceholderModelSource implements ModelSource, AutoCloseable {
    public static final int PLACEHOLDER_COLOR = 0xFFFF_00FF;

    private final Dictionary<BakedModel> models = new Dictionary<>((id, model) -> { });
    private final BakedModel placeholder = BakedModel.solid(PLACEHOLDER_COLOR);
    private final PackBlocks packBlocks;

    public PlaceholderModelSource(PackBlocks packBlocks) {
        this.packBlocks = packBlocks;
        models.register(placeholder);
    }

    public PackBlocks packBlocks() {
        return packBlocks;
    }

    @Override
    public int modelCount() {
        return 1;
    }

    @Override
    public BakedModel model(int modelId) {
        return placeholder;
    }

    @Override
    public void close() {
    }
}