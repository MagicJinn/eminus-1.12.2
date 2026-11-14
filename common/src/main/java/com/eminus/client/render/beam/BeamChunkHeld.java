package com.eminus.client.render.beam;

/** Chunk availability for far beam culling without a loader level type. */
@FunctionalInterface
public interface BeamChunkHeld {
    boolean chunkHeld(int sectionChunkX, int sectionChunkZ);
}