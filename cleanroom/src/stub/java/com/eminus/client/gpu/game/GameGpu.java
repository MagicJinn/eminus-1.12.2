package com.eminus.client.gpu.game;

import com.eminus.client.gpu.DeviceQueries;
import com.eminus.gpu.Gpu;

/** Vetus uses {@link com.eminus.client.gpu.opengl.OpenGlGpu} only. */
public final class GameGpu {
    private GameGpu() {
    }

    public static String backendName() {
        return DeviceQueries.OPENGL;
    }

    public static Gpu create() {
        throw new UnsupportedOperationException("Game GPU is not available on Cleanroom vetus");
    }
}