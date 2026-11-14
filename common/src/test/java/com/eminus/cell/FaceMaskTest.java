package com.eminus.cell;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eminus.geom.Face;

import org.junit.jupiter.api.Test;

class FaceMaskTest {
    private static final int LAST = DetailLevel.VOXELS_PER_SIDE - 1;
    private static final int INSIDE = 5;

    @Test
    void eachBitSitsAtItsDirectionOrdinal() {
        assertEquals(Face.DOWN.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.DOWN));
        assertEquals(Face.UP.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.UP));
        assertEquals(Face.NORTH.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.NORTH));
        assertEquals(Face.SOUTH.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.SOUTH));
        assertEquals(Face.WEST.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.WEST));
        assertEquals(Face.EAST.vanillaIndex(), Integer.numberOfTrailingZeros(FaceMask.EAST));
    }

    @Test
    void theLowCornerCarriesTheThreeNegativeFaces() {
        assertEquals(FaceMask.DOWN | FaceMask.NORTH | FaceMask.WEST, FaceMask.of(0, 0, 0));
    }

    @Test
    void theHighCornerCarriesTheThreePositiveFaces() {
        assertEquals(FaceMask.UP | FaceMask.SOUTH | FaceMask.EAST, FaceMask.of(LAST, LAST, LAST));
    }

    @Test
    void anInteriorVoxelCarriesNoFace() {
        assertEquals(FaceMask.NONE, FaceMask.of(INSIDE, INSIDE, INSIDE));
    }
}
