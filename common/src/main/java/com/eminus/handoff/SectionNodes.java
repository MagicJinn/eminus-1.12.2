package com.eminus.handoff;

/**
 * Section coordinate packing matching vanilla {@code SectionPos#asLong} layout.
 */
public final class SectionNodes {
    private static final int X_BITS = 22;
    private static final int Z_BITS = 22;
    private static final int Y_BITS = 20;
    private static final long X_MASK = (1L << X_BITS) - 1L;
    private static final long Y_MASK = (1L << Y_BITS) - 1L;
    private static final long Z_MASK = (1L << Z_BITS) - 1L;

    private SectionNodes() {
    }

    public static long pack(int sectionX, int sectionY, int sectionZ) {
        return (sectionX & X_MASK) | (sectionZ & Z_MASK) << X_BITS | ((long) sectionY & Y_MASK) << (X_BITS + Z_BITS);
    }
}