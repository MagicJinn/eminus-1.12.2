package net.minecraft.util;

/** Minimal math helper for vetus upstream color helpers. */
public final class Mth {
    private Mth() {
    }

    public static int floor(float value) {
        return (int) Math.floor(value);
    }

    public static long getSeed(int x, int y, int z) {
        long seed = (long) x * 3129871L ^ (long) z * 116129781L ^ (long) y;
        seed = seed * seed * 42317861L + seed * 11L;
        return seed;
    }
}