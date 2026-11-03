package com.eminus.mesh;

import java.util.Arrays;

public final class OcclusionWord {
    public static final int BITS = 9;
    public static final int PER_SLOT = Long.SIZE / BITS;
    public static final int INNER_SHIFT = BITS;
    public static final int NONE = 0;
    public static final int FRONT = 1;
    public static final int LOW_U = 1 << 1;
    public static final int HIGH_U = 1 << 2;
    public static final int LOW_V = 1 << 3;
    public static final int HIGH_V = 1 << 4;
    public static final int LOW_U_LOW_V = 1 << 5;
    public static final int HIGH_U_LOW_V = 1 << 6;
    public static final int LOW_U_HIGH_V = 1 << 7;
    public static final int HIGH_U_HIGH_V = 1 << 8;

    private static final int MASK = (1 << BITS) - 1;
    private static final long[] NO_STREAM = new long[0];
    private static final int HIGH_U_END = HIGH_U | HIGH_U_LOW_V | HIGH_U_HIGH_V;
    private static final int HIGH_V_END = HIGH_V | LOW_U_HIGH_V | HIGH_U_HIGH_V;
    private static final int HIGH_U_ENDS = HIGH_U_END | HIGH_U_END << INNER_SHIFT;
    private static final int HIGH_V_ENDS = HIGH_V_END | HIGH_V_END << INNER_SHIFT;

    public static int slots(int quads) {
        return Math.ceilDiv(quads, PER_SLOT);
    }

    public static int of(int outer, int inner) {
        return outer | inner << INNER_SHIFT;
    }

    public static int outer(int word) {
        return word & MASK;
    }

    public static int inner(int word) {
        return word >>> INNER_SHIFT & MASK;
    }

    public static long[] pack(int[] words, int count) {
        int outerSlots = slots(count);
        long[] slots = new long[2 * outerSlots];
        long outerAny = 0L;
        long innerAny = 0L;
        for (int index = 0; index < count; index++) {
            long outer = outer(words[index]);
            long inner = inner(words[index]);
            int shift = index % PER_SLOT * BITS;
            slots[index / PER_SLOT] |= outer << shift;
            slots[outerSlots + index / PER_SLOT] |= inner << shift;
            outerAny |= outer;
            innerAny |= inner;
        }

        if (innerAny != NONE) {
            return slots;
        }

        return outerAny == NONE ? NO_STREAM : Arrays.copyOf(slots, outerSlots);
    }

    public static boolean hasInner(long[] slots, int count) {
        return slots.length > slots(count);
    }

    public static int unpack(long[] slots, int count, int index) {
        if (slots.length == 0) {
            return NONE;
        }

        int outer = field(slots, 0, index);
        return hasInner(slots, count) ? of(outer, field(slots, slots(count), index)) : outer;
    }

    public static boolean joinsAlongU(int low, int high) {
        return halfJoinsAlongU(outer(low), outer(high)) && halfJoinsAlongU(inner(low), inner(high));
    }

    public static int joinAlongU(int low, int high) {
        return (low & ~HIGH_U_ENDS) | (high & HIGH_U_ENDS);
    }

    public static boolean joinsAlongV(int low, int high) {
        return halfJoinsAlongV(outer(low), outer(high)) && halfJoinsAlongV(inner(low), inner(high));
    }

    public static int joinAlongV(int low, int high) {
        return (low & ~HIGH_V_ENDS) | (high & HIGH_V_ENDS);
    }

    private static int field(long[] slots, int first, int index) {
        return (int) (slots[first + index / PER_SLOT] >>> (index % PER_SLOT * BITS) & MASK);
    }

    private static boolean halfJoinsAlongU(int low, int high) {
        return same(low, high, FRONT | LOW_V | HIGH_V)
                && matches(low, HIGH_U, FRONT) && matches(low, HIGH_U_LOW_V, LOW_V)
                && matches(low, HIGH_U_HIGH_V, HIGH_V) && matches(high, LOW_U, FRONT)
                && matches(high, LOW_U_LOW_V, LOW_V) && matches(high, LOW_U_HIGH_V, HIGH_V);
    }

    private static boolean halfJoinsAlongV(int low, int high) {
        return same(low, high, FRONT | LOW_U | HIGH_U)
                && matches(low, HIGH_V, FRONT) && matches(low, LOW_U_HIGH_V, LOW_U)
                && matches(low, HIGH_U_HIGH_V, HIGH_U) && matches(high, LOW_V, FRONT)
                && matches(high, LOW_U_LOW_V, LOW_U) && matches(high, HIGH_U_LOW_V, HIGH_U);
    }

    private static boolean same(int first, int second, int bits) {
        return (first & bits) == (second & bits);
    }

    private static boolean matches(int word, int bit, int other) {
        return ((word & bit) != 0) == ((word & other) != 0);
    }

    private OcclusionWord() {
    }
}
