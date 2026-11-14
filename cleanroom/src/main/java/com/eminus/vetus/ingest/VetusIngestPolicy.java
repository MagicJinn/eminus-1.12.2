package com.eminus.vetus.ingest;

import net.minecraft.client.Minecraft;

/**
 * Side rules for vetus ingest hooks. {@link com.eminus.vetus.client.VetusClientEvents} is the only
 * registered ingest entry today and listens on the Forge client bus.
 */
public final class VetusIngestPolicy {
    private VetusIngestPolicy() {
    }

    /**
     * True when the player is connected to a remote server with no integrated server in this JVM.
     * Ingest must use client chunk loading only, never chunk generation hooks.
     */
    public static boolean remoteClientOnly(Minecraft client) {
        return !client.isIntegratedServerRunning();
    }

    /**
     * Chunk generation or population mixins and server {@code ChunkEvent} hooks must not register when
     * this returns true.
     */
    public static boolean forbidServerChunkgenHooks(Minecraft client) {
        return remoteClientOnly(client);
    }

    /**
     * Reserved for a future server distribution. The client-only jar never registers generation hooks.
     */
    public static boolean mayRegisterIntegratedServerGenerationHooks(Minecraft client) {
        return client.isIntegratedServerRunning();
    }
}