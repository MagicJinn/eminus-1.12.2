package net.minecraft.world.level.block.state;

/** Marker type for {@link com.eminus.model.PackBlocks} and tint records on vetus. */
public interface BlockState {
    default boolean isAir() {
        return false;
    }
}