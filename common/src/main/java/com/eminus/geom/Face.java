package com.eminus.geom;

/**
 * Axis-aligned cube face. Index order matches vanilla {@code Direction} and 1.12 {@code EnumFacing}
 * ({@code get3DDataValue()} / {@code getIndex()}).
 */
public enum Face {
    DOWN(0, 0, -1, 0),
    UP(1, 0, 1, 0),
    NORTH(2, 0, 0, -1),
    SOUTH(3, 0, 0, 1),
    WEST(4, -1, 0, 0),
    EAST(5, 1, 0, 0);

    private static final Face[] BY_INDEX = values();

    public static final int COUNT = values().length;

    private final int vanillaIndex;
    private final int stepX;
    private final int stepY;
    private final int stepZ;

    Face(int vanillaIndex, int stepX, int stepY, int stepZ) {
        this.vanillaIndex = vanillaIndex;
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
    }

    public static Face fromVanillaIndex(int index) {
        if (index < 0 || index >= BY_INDEX.length) {
            throw new IllegalArgumentException("Face index out of range: " + index);
        }
        return BY_INDEX[index];
    }

    public int vanillaIndex() {
        return vanillaIndex;
    }

    public int stepX() {
        return stepX;
    }

    public int stepY() {
        return stepY;
    }

    public int stepZ() {
        return stepZ;
    }

    public Axis axis() {
        return switch (this) {
            case DOWN, UP -> Axis.Y;
            case NORTH, SOUTH -> Axis.Z;
            case WEST, EAST -> Axis.X;
        };
    }

    public boolean positive() {
        return stepX > 0 || stepY > 0 || stepZ > 0;
    }

    public Face opposite() {
        return fromVanillaIndex(vanillaIndex ^ 1);
    }

    public enum Axis {
        X,
        Y,
        Z
    }
}