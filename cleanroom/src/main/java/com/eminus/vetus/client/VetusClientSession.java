package com.eminus.vetus.client;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.SectionPyramid;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;

/**
 * Cleanroom session shim. Holds ingest handoff until upstream {@code DimensionRuntime} and {@code FarRenderer} wire in
 * on this loader (see docs/PORTING.md).
 */
public final class VetusClientSession {
    private static int sectionHandoffs;
    private static boolean renderHookLogged;

    private VetusClientSession() {
    }

    public static void ingestSection(ColumnRef column, int sectionIndex, SectionPyramid pyramid, IngestTrigger trigger) {
        sectionHandoffs++;
        if (sectionHandoffs == 1 || sectionHandoffs % 64 == 0) {
            long samples = pyramid.level(com.eminus.cell.DetailLevel.MIN).length;
            EminusVetus.LOGGER.info(
                    "Vetus handed section {} of column {},{} dim {} to upstream pyramid ({} voxels, trigger {}, total {})",
                    sectionIndex,
                    column.chunkX(),
                    column.chunkZ(),
                    column.dimensionId(),
                    samples,
                    trigger,
                    sectionHandoffs);
        }
    }

    public static void dropColumn(ColumnRef column) {
        EminusVetus.LOGGER.debug("Vetus dropped column {},{} dim {}", column.chunkX(), column.chunkZ(),
                column.dimensionId());
    }

    public static void drawFarLayer() {
        if (!VetusClientState.farLayerAllowed()) {
            return;
        }
        if (!renderHookLogged) {
            renderHookLogged = true;
            EminusVetus.LOGGER.info(
                    "Vetus far render pass (EntityRenderer hook). FarRenderer GPU wiring lands next in this port.");
        }
    }
}