package com.eminus.mesh;

/** Constants-only stub for {@link com.eminus.client.render.far.FarQuads} on vetus. */
public final class QuadOffset {
    public static final int NONE = 0;
    public static final int AXIS_BITS = 10;
    public static final int X_SHIFT = 0;
    public static final int Y_SHIFT = AXIS_BITS;
    public static final int Z_SHIFT = 2 * AXIS_BITS;
    public static final float STEPS_PER_BLOCK = 256.0F;

    private static final int AXIS_MASK = (1 << AXIS_BITS) - 1;
    private static final int SIGN_SHIFT = Integer.SIZE - AXIS_BITS;

    public static float x(int offset) {
        return axis(offset, X_SHIFT);
    }

    public static float y(int offset) {
        return axis(offset, Y_SHIFT);
    }

    public static float z(int offset) {
        return axis(offset, Z_SHIFT);
    }

    private static float axis(int offset, int shift) {
        return ((offset >>> shift & AXIS_MASK) << SIGN_SHIFT >> SIGN_SHIFT) / STEPS_PER_BLOCK;
    }

    private QuadOffset() {
    }
}