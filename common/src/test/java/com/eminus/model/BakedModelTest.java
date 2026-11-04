package com.eminus.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;

import com.eminus.cell.FaceMask;
import com.eminus.model.port.MipStrategy;

import net.minecraft.core.Direction;

import org.junit.jupiter.api.Test;

class BakedModelTest {
    private static final int OPAQUE_WHITE = 0xFFFF_FFFF;
    private static final int CLEAR = 0x0000_0000;
    private static final int HALF_ALPHA_WHITE = 0x80FF_FFFF;
    private static final int HOLE = 37;
    private static final int UP = 1 << Direction.UP.get3DDataValue();
    private static final int DOWN = 1 << Direction.DOWN.get3DDataValue();
    private static final FaceMip CUTOUT = new FaceMip(MipStrategy.CUTOUT, true);
    private static final FaceMip UNMIPPED_CUTOUT = new FaceMip(MipStrategy.CUTOUT, false);

    @Test
    void aFaceOfOpaqueTexelsIsSolid() {
        assertEquals(FaceMask.ALL, model(filled(OPAQUE_WHITE), FaceMask.ALL, 0, FaceMip.uniform(CUTOUT)).solidFaces());
    }

    @Test
    void aFaceWithOneClearTexelIsNotSolid() {
        int[] faces = filled(OPAQUE_WHITE);
        faces[Direction.UP.get3DDataValue() * BakedModel.FACE_TEXELS + HOLE] = CLEAR;

        assertEquals(FaceMask.ALL & ~UP, model(faces, FaceMask.ALL, 0, FaceMip.uniform(CUTOUT)).solidFaces());
    }

    @Test
    void aFaceOfHalfAlphaTexelsIsNotSolid() {
        assertEquals(FaceMask.NONE,
                model(filled(HALF_ALPHA_WHITE), FaceMask.ALL, 0, FaceMip.uniform(FaceMip.MEAN)).solidFaces());
    }

    @Test
    void aFaceTheModelDoesNotDrawIsNotSolid() {
        assertEquals(FaceMask.ALL & ~DOWN,
                model(filled(OPAQUE_WHITE), FaceMask.ALL & ~DOWN, 0, FaceMip.uniform(CUTOUT)).solidFaces());
    }

    @Test
    void aForcedOpaqueModelIsSolidOnEveryFaceItDrawsWhateverItsAlpha() {
        int[] faces = filled(OPAQUE_WHITE);
        faces[Direction.UP.get3DDataValue() * BakedModel.FACE_TEXELS + HOLE] = CLEAR;

        assertEquals(FaceMask.ALL & ~DOWN,
                model(faces, FaceMask.ALL & ~DOWN, ModelMetadata.OPAQUE, FaceMip.uniform(CUTOUT)).solidFaces());
    }

    @Test
    void twoModelsTheGameDrawsInLayersOfDifferentMipsAreNotEqual() {
        assertNotEquals(model(filled(OPAQUE_WHITE), FaceMask.ALL, 0, FaceMip.uniform(CUTOUT)),
                model(filled(OPAQUE_WHITE), FaceMask.ALL, 0, FaceMip.uniform(UNMIPPED_CUTOUT)));
    }

    @Test
    void theUnmippedFacesAreTheOnesWhoseMipTheGameSkips() {
        FaceMip[] mips = FaceMip.uniform(CUTOUT);
        mips[Direction.UP.get3DDataValue()] = UNMIPPED_CUTOUT;
        mips[Direction.DOWN.get3DDataValue()] = UNMIPPED_CUTOUT;

        assertEquals(UP | DOWN, model(filled(OPAQUE_WHITE), FaceMask.ALL, 0, mips).unmippedFaces());
    }

    private static BakedModel model(int[] faces, int present, int flags, FaceMip[] mips) {
        return new BakedModel(faces, BakedModel.untintedMask(), new float[BakedModel.FACE_COUNT],
                BakedModel.fullBounds(), ModelMetadata.pack(present, FaceMask.NONE, FaceMask.NONE, 0, flags),
                BiomeColors.NO_ROW, mips).solidUnder(VanillaMipBlend.BLEND);
    }

    private static int[] filled(int argb) {
        int[] faces = new int[BakedModel.FACE_COUNT * BakedModel.FACE_TEXELS];
        Arrays.fill(faces, argb);
        return faces;
    }
}
