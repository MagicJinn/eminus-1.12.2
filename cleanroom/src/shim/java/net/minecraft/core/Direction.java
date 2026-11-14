package net.minecraft.core;

/**
 * Minimal modern-facing enum for upstream mesh code on 1.12.2. Values match Mojang {@code Direction} indices.
 */
public enum Direction {
    DOWN(0, 0, -1, 0),
    UP(1, 0, 1, 0),
    NORTH(2, 0, 0, -1),
    SOUTH(3, 0, 0, 1),
    WEST(4, -1, 0, 0),
    EAST(5, 1, 0, 0);

    public enum Axis {
        X, Y, Z;

        public int choose(int x, int y, int z) {
            return switch (this) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }

        public float choose(float x, float y, float z) {
            return switch (this) {
                case X -> x;
                case Y -> y;
                case Z -> z;
            };
        }
    }

    public enum AxisDirection {
        POSITIVE, NEGATIVE
    }

    private static final Direction[] BY_3D = new Direction[values().length];

    static {
        for (Direction direction : values()) {
            BY_3D[direction.data3d] = direction;
        }
    }

    private final int data3d;
    private final int stepX;
    private final int stepY;
    private final int stepZ;

    Direction(int data3d, int stepX, int stepY, int stepZ) {
        this.data3d = data3d;
        this.stepX = stepX;
        this.stepY = stepY;
        this.stepZ = stepZ;
    }

    public int get3DDataValue() {
        return data3d;
    }

    public int getStepX() {
        return stepX;
    }

    public int getStepY() {
        return stepY;
    }

    public int getStepZ() {
        return stepZ;
    }

    public Axis getAxis() {
        if (stepY != 0) {
            return Axis.Y;
        }
        if (stepZ != 0) {
            return Axis.Z;
        }
        return Axis.X;
    }

    public AxisDirection getAxisDirection() {
        int step = stepX != 0 ? stepX : (stepY != 0 ? stepY : stepZ);
        return step < 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE;
    }

    public static Direction from3DDataValue(int value) {
        return BY_3D[value];
    }

    public static Direction get(AxisDirection direction, Axis axis) {
        for (Direction face : values()) {
            if (face.getAxis() == axis && face.getAxisDirection() == direction) {
                return face;
            }
        }
        throw new IllegalArgumentException(axis + " " + direction);
    }

    public Direction getOpposite() {
        return switch (this) {
            case DOWN -> UP;
            case UP -> DOWN;
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case WEST -> EAST;
            case EAST -> WEST;
        };
    }
}