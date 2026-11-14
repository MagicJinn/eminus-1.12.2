package com.eminus.vetus.client;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.SectionPyramid;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;
import com.eminus.vetus.platform.VetusPlatform;

import net.minecraft.client.Minecraft;

/**
 * Vetus client session. Pyramid handoff logs here until {@link com.eminus.session.DimensionRuntime} and
 * {@link com.eminus.client.render.far.FarRenderer} bundle on {@code :core} for Cleanroom.
 */
public final class VetusClientSession {
    private static int sectionHandoffs;
    private static boolean renderHookLogged;
    private static boolean sessionStarted;

    private VetusClientSession() {
    }

    public static void ingestSection(ColumnRef column, int sectionIndex, SectionPyramid pyramid, IngestTrigger trigger) {
        sectionHandoffs++;
        if (sectionHandoffs == 1 || sectionHandoffs % 64 == 0) {
            long samples = pyramid.level(com.eminus.cell.DetailLevel.MIN).length;
            EminusVetus.LOGGER.info(
                    "Vetus merged section {} of column {},{} dim {} into CellMerger handoff ({} voxels, trigger {}, total {})",
                    sectionIndex,
                    column.chunkX(),
                    column.chunkZ(),
                    column.dimensionId(),
                    samples,
                    trigger,
                    sectionHandoffs);
        }
        // DimensionRuntime.merger().merge when far core bundle lands on :core.
    }

    public static void dropColumn(ColumnRef column) {
        EminusVetus.LOGGER.debug("Vetus dropped column {},{} dim {}", column.chunkX(), column.chunkZ(),
                column.dimensionId());
    }

    public static void drawFarLayer() {
        if (!VetusClientState.farLayerAllowed()) {
            return;
        }
        if (!sessionStarted) {
            sessionStarted = true;
            VetusPlatform.install();
            com.eminus.client.gpu.RenderThreadGuard.install(() -> {
                if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
                    throw new IllegalStateException("Eminus far rendering must run on the client thread");
                }
            });
        }
        if (!renderHookLogged) {
            renderHookLogged = true;
            EminusVetus.LOGGER.info(
                    "Vetus far render pass (EntityRenderer hook). GL guard active. FarRenderer starts when core bundles the GPU far stack.");
        }
        try (VetusGlStateGuard ignored = VetusGlStateGuard.capture()) {
            // FarRenderer.newFrame() and frame() run here once FarStart is on the vetus classpath.
        }
    }
}