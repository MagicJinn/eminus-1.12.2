package com.eminus.ingest.port;

/**
 * Loader-neutral 16³ section samples for {@link com.eminus.ingest.SectionConverter}.
 * Cleanroom implements this from {@code Chunk} / {@code ExtendedBlockStorage}.
 */
public interface SectionSlice {
    Object blockStateKey(int x, int y, int z);

    int skyLight(int x, int y, int z);

    int blockLight(int x, int y, int z);

    String biomeKey(int x, int y, int z);
}