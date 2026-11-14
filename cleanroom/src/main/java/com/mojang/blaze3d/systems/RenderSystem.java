package com.mojang.blaze3d.systems;

import net.minecraft.client.Minecraft;

/**
 * Shim for upstream {@code OpenGlGpu} on Cleanroom. Only render-thread assertion is required for vetus.
 */
public final class RenderSystem {
    private RenderSystem() {
    }

    public static void assertOnRenderThread() {
        Minecraft client = Minecraft.getMinecraft();
        if (client == null) {
            return;
        }
        if (!client.isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Eminus far rendering must run on the client thread");
        }
    }
}