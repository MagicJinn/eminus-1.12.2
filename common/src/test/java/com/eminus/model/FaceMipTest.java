package com.eminus.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eminus.model.port.MipStrategy;
import com.eminus.model.port.Sprite;

import org.junit.jupiter.api.Test;

class FaceMipTest {
    private static final Sprite MEAN = new MipSprite(MipStrategy.MEAN, true);
    private static final Sprite CUTOUT = new MipSprite(MipStrategy.CUTOUT, true);
    private static final Sprite UNMIPPED_CUTOUT = new MipSprite(MipStrategy.CUTOUT, false);

    @Test
    void aFaceNoSpriteDrewIsMean() {
        assertEquals(FaceMip.MEAN, FaceMip.drawnBy(new Sprite[4]));
    }

    @Test
    void theSpriteCoveringMostTexelsDecidesTheStrategy() {
        assertEquals(new FaceMip(MipStrategy.CUTOUT, true),
                FaceMip.drawnBy(new Sprite[] {MEAN, CUTOUT, null, CUTOUT}));
    }

    @Test
    void theSpriteCoveringMostTexelsDecidesWhetherTheFaceIsMipped() {
        assertEquals(new FaceMip(MipStrategy.CUTOUT, false),
                FaceMip.drawnBy(new Sprite[] {CUTOUT, UNMIPPED_CUTOUT, null, UNMIPPED_CUTOUT}));
    }

    private record MipSprite(MipStrategy mipStrategy, boolean mipped) implements Sprite {
        @Override
        public int width() {
            return 1;
        }

        @Override
        public int height() {
            return 1;
        }

        @Override
        public float u0() {
            return 0.0F;
        }

        @Override
        public float u1() {
            return 1.0F;
        }

        @Override
        public float v0() {
            return 0.0F;
        }

        @Override
        public float v1() {
            return 1.0F;
        }

        @Override
        public int[] argb() {
            return new int[1];
        }
    }
}
