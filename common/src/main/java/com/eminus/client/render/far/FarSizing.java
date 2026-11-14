package com.eminus.client.render.far;

import java.util.OptionalLong;

import com.eminus.client.mesh.MeshWiring;
import com.eminus.gpu.Gpu;
import com.eminus.gpu.Sparse;
import com.eminus.render.arena.ArenaSizing;
import com.eminus.render.tree.NodeTable;
import com.eminus.settings.Settings;

import org.jspecify.annotations.Nullable;

record FarSizing(long arenaBytes, @Nullable Sparse sparse, int nodeCapacity) {
    static FarSizing of(FarClientBridge bridge, Gpu gpu, OptionalLong freeBytes, long replacedArenaBytes, int lowestLevel,
            Settings settings, int levelHeight) {
        long ceiling = ArenaBudget.ceiling(gpu.capabilities(), freeBytes, replacedArenaBytes);
        float focal = FarProjection.focalPixels(bridge.settingsFovDegrees(), gpu.mainColor().height());
        int subdivisionPixels = settings.detailDistance().pixels();
        @Nullable Sparse sparse = ArenaBudget.sparse(gpu);
        long bytes = ArenaBudget.bytes(ArenaSizing.wanted(settings.farRenderCells(), subdivisionPixels, focal,
                lowestLevel, MeshWiring.occlusion(settings.lodQuality().cornerOcclusion())), ceiling, sparse);

        return new FarSizing(bytes, sparse, NodeTable.capacity(lowestLevel, focal, subdivisionPixels,
                settings.farRenderCells(), levelHeight));
    }
}
