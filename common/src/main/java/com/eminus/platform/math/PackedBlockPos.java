package com.eminus.platform.math;

/** Long block position packing compatible with stored beam records. */
public final class PackedBlockPos {
    private static final int Y_BITS = 12;
    private static final int Z_BITS = 26;
    private static final int Y_MASK = (1 << Y_BITS) - 1;
    private static final int Z_MASK = (1 << Z_BITS) - 1;

    private PackedBlockPos() {
    }

    public static int getX(long pos) {
        return (int) (pos >> (Y_BITS + Z_BITS));
    }

    public static int getY(long pos) {
        return (int) (pos & Y_MASK);
    }

    public static int getZ(long pos) {
        return (int) (pos >> Y_BITS) & Z_MASK;
    }

    public static int sectionX(int blockX) {
        return blockX >> 4;
    }

    public static int sectionZ(int blockZ) {
        return blockZ >> 4;
    }
}