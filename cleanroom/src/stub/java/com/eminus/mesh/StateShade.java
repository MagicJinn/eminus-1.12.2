package com.eminus.mesh;

import net.minecraft.world.level.block.state.BlockState;

/** Vetus stub; shade queries are unused on placeholder mesh path. */
public final class StateShade {
    public static final int DARK = 1;
    public static final int PERMEABLE = 1 << 1;
    public static final int FULL_COLLISION = 1 << 2;
    public static final int OPEN = PERMEABLE;

    public static int of(BlockState state) {
        return PERMEABLE;
    }

    public static boolean has(int shade, int flag) {
        return (shade & flag) != 0;
    }

    private StateShade() {
    }
}