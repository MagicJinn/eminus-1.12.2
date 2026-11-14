package net.minecraft.world.level.block.state;

public interface BlockState {
    default boolean isAir() {
        return false;
    }
}