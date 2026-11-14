package com.eminus.vetus.client;

import com.eminus.ingest.port.ChunkColumnSink;
import com.eminus.render.port.FarLayerEnvironment;
import com.eminus.vetus.EminusVetus;
import com.eminus.vetus.ingest.VetusChunkColumnSink;

public final class VetusClientState {
    private static final FarLayerEnvironment SHADERS = new OptiFineShaderEnvironment();
    private static final ChunkColumnSink INGEST = new VetusChunkColumnSink();
    private static volatile boolean shaderWarned;

    public static FarLayerEnvironment shaderEnvironment() {
        return SHADERS;
    }

    public static ChunkColumnSink chunkIngest() {
        return INGEST;
    }

    public static boolean farLayerAllowed() {
        if (!SHADERS.externalShadersActive()) {
            return true;
        }

        if (!shaderWarned) {
            shaderWarned = true;
            EminusVetus.LOGGER.warn(
                    "Eminus Vetus far LOD is disabled while {} are active",
                    SHADERS.externalShaderLabel());
        }

        return false;
    }

    private VetusClientState() {
    }
}