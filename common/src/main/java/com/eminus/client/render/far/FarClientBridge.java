package com.eminus.client.render.far;

import java.util.List;

import com.eminus.cell.CellFrame;
import com.eminus.client.frame.GameFrame;
import com.eminus.client.handoff.NearSectionTable;
import com.eminus.mesh.MeshSummary;
import com.eminus.session.DimensionRuntime;

/**
 * Loader-neutral client hooks for {@link FarRenderer}. Modern uses {@link ModernFarClientBridge}.
 * Cleanroom supplies {@code com.eminus.vetus.client.render.VetusFarClientBridge}.
 */
public interface FarClientBridge {
    GameFrame readFrame();

    float settingsFovDegrees();

    int renderDistanceChunks();

    LevelExtents levelExtents();

    void fillNearSections(NearSectionTable table, GameFrame game, CellFrame frame, int extraChunks,
            List<MeshSummary> meshes);

    void updateOverlays(FarOverlays overlays, DimensionRuntime runtime, GameFrame game);

    int biomeBlendRadius();

    record LevelExtents(int minBuildHeight, int height, int minSectionY, int sectionCount) {
    }
}