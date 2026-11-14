package com.eminus.client.gpu;

/**
 * Indirection for render-thread checks so Cleanroom can install a vetus guard without forking {@code OpenGlGpu}.
 */
public final class RenderThreadGuard {
    private static Runnable delegate = () -> { };

    private RenderThreadGuard() {
    }

    public static void install(Runnable check) {
        delegate = check;
    }

    public static void assertOnRenderThread() {
        delegate.run();
    }
}