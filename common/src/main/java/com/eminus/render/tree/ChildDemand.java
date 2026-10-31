package com.eminus.render.tree;

final class ChildDemand {
    static final float PREFETCH_SHARE = 0.8F;

    static boolean wants(CellBox box, float size, int subdivisionPixels, double farBlocks, TreeHorizon horizon) {
        return size > subdivisionPixels * PREFETCH_SHARE && box.horizontalDistance() <= farBlocks
                && withinHorizon(box, horizon);
    }

    static boolean withinHorizon(CellBox box, TreeHorizon horizon) {
        return horizon.refines(TreeHorizon.key(box.horizontalDistance(), box.level()));
    }

    private ChildDemand() {
    }
}
