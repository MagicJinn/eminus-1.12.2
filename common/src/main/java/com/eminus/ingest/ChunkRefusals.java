package com.eminus.ingest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.world.level.Level;

// mc/1.21: compat/sable registers the plot grid of Sable sub-levels, whose chunks are not terrain where they sit.
public final class ChunkRefusals {
    private static final List<Refusal> REFUSALS = new CopyOnWriteArrayList<>();
    private static final List<DimensionRefusal> DIMENSION_REFUSALS = new CopyOnWriteArrayList<>();

    public static void register(Refusal refusal) {
        REFUSALS.add(refusal);
    }

    public static void registerDimension(DimensionRefusal refusal) {
        DIMENSION_REFUSALS.add(refusal);
    }

    public static boolean refusesDimension(int dimensionId, int chunkX, int chunkZ) {
        for (DimensionRefusal refusal : DIMENSION_REFUSALS) {
            if (refusal.refuses(dimensionId, chunkX, chunkZ)) {
                return true;
            }
        }

        return false;
    }

    public static boolean refuses(Level level, int chunkX, int chunkZ) {
        for (Refusal refusal : REFUSALS) {
            if (refusal.refuses(level, chunkX, chunkZ)) {
                return true;
            }
        }

        return false;
    }

    @FunctionalInterface
    public interface Refusal {
        boolean refuses(Level level, int chunkX, int chunkZ);
    }

    @FunctionalInterface
    public interface DimensionRefusal {
        boolean refuses(int dimensionId, int chunkX, int chunkZ);
    }

    private ChunkRefusals() {
    }
}
