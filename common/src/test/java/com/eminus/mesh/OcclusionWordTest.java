package com.eminus.mesh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OcclusionWordTest {
    private static final int WALL_BELOW = OcclusionWord.LOW_V | OcclusionWord.LOW_U_LOW_V
            | OcclusionWord.HIGH_U_LOW_V;
    private static final int BLOCK_ON_THE_RIGHT = OcclusionWord.HIGH_U | OcclusionWord.HIGH_U_LOW_V
            | OcclusionWord.HIGH_U_HIGH_V;
    private static final int BURIED = OcclusionWord.FRONT | OcclusionWord.LOW_U | OcclusionWord.HIGH_U
            | OcclusionWord.LOW_V | OcclusionWord.HIGH_V | OcclusionWord.LOW_U_LOW_V | OcclusionWord.HIGH_U_LOW_V
            | OcclusionWord.LOW_U_HIGH_V | OcclusionWord.HIGH_U_HIGH_V;
    private static final int[] WORDS = {1, 0x1FF, 0x1F0, 0x155, 7, 0x0AA, 0x101, 0x1FE};

    @Test
    void sevenWordsShareOneSlotAndTheFourthCrossesItsHalves() {
        long[] slots = OcclusionWord.pack(WORDS, WORDS.length);

        assertEquals(7, OcclusionWord.PER_SLOT);
        assertEquals(2, slots.length);
        assertEquals(0x155L, slots[0] >>> 27 & 0x1FF);
        for (int index = 0; index < WORDS.length; index++) {
            assertEquals(WORDS[index], OcclusionWord.unpack(slots, WORDS.length, index), "word " + index);
        }
    }

    @Test
    void wordsThatAreAllNoneLeaveNoStreamAndReadBackAsNone() {
        long[] slots = OcclusionWord.pack(new int[OcclusionWord.PER_SLOT + 1], OcclusionWord.PER_SLOT + 1);

        assertEquals(0, slots.length);
        assertEquals(OcclusionWord.NONE, OcclusionWord.unpack(slots, OcclusionWord.PER_SLOT + 1,
                OcclusionWord.PER_SLOT));
    }

    @Test
    void anInnerHalfAddsASecondRegionOfTheSameLayout() {
        int[] words = WORDS.clone();
        words[3] = OcclusionWord.of(words[3], BLOCK_ON_THE_RIGHT);
        long[] slots = OcclusionWord.pack(words, words.length);

        assertEquals(2 * OcclusionWord.slots(words.length), slots.length);
        assertTrue(OcclusionWord.hasInner(slots, words.length));
        for (int index = 0; index < words.length; index++) {
            assertEquals(words[index], OcclusionWord.unpack(slots, words.length, index), "word " + index);
        }
    }

    @Test
    void anInnerHalfAloneStillKeepsBothRegions() {
        long[] slots = OcclusionWord.pack(new int[] {OcclusionWord.of(OcclusionWord.NONE, WALL_BELOW)}, 1);

        assertEquals(2, slots.length);
        assertEquals(WALL_BELOW, OcclusionWord.inner(OcclusionWord.unpack(slots, 1, 0)));
    }

    @Test
    void wordsWithoutAnInnerHalfKeepOneRegion() {
        assertFalse(OcclusionWord.hasInner(OcclusionWord.pack(WORDS, WORDS.length), WORDS.length));
    }

    @Test
    void anInnerHalfThatSplitsTheRunSplitsTheWholeWord() {
        int beside = OcclusionWord.of(WALL_BELOW, OcclusionWord.NONE);

        assertFalse(OcclusionWord.joinsAlongU(OcclusionWord.of(WALL_BELOW, BLOCK_ON_THE_RIGHT), beside));
        assertTrue(OcclusionWord.joinsAlongU(OcclusionWord.of(WALL_BELOW, WALL_BELOW),
                OcclusionWord.of(WALL_BELOW, WALL_BELOW)));
    }

    @Test
    void aJoinKeepsEachEndsOwnEdgesInBothHalves() {
        int low = OcclusionWord.of(WALL_BELOW, WALL_BELOW);
        int high = OcclusionWord.of(WALL_BELOW | BLOCK_ON_THE_RIGHT, WALL_BELOW | BLOCK_ON_THE_RIGHT);

        assertEquals(high, OcclusionWord.joinAlongU(low, high));
    }

    @Test
    void aFloorBesideAWallJoinsAlongTheWall() {
        assertTrue(OcclusionWord.joinsAlongU(WALL_BELOW, WALL_BELOW));
        assertEquals(WALL_BELOW, OcclusionWord.joinAlongU(WALL_BELOW, WALL_BELOW));
    }

    @Test
    void aWallEndingInsideTheRunSplitsIt() {
        assertFalse(OcclusionWord.joinsAlongU(WALL_BELOW, OcclusionWord.NONE));
    }

    @Test
    void aBlockInFrontOfTheSharedEdgeSplitsTheRun() {
        assertFalse(OcclusionWord.joinsAlongU(BLOCK_ON_THE_RIGHT, OcclusionWord.NONE));
    }

    @Test
    void theRowAwayFromTheWallStacksOnTheRowBesideIt() {
        assertTrue(OcclusionWord.joinsAlongV(WALL_BELOW, OcclusionWord.NONE));
        assertEquals(WALL_BELOW, OcclusionWord.joinAlongV(WALL_BELOW, OcclusionWord.NONE));
    }

    @Test
    void aRowUnderTheWallDoesNotStackBelowIt() {
        assertFalse(OcclusionWord.joinsAlongV(OcclusionWord.NONE, WALL_BELOW));
    }

    @Test
    void facesBuriedInDarkBlocksJoinBothWays() {
        assertTrue(OcclusionWord.joinsAlongU(BURIED, BURIED));
        assertTrue(OcclusionWord.joinsAlongV(BURIED, BURIED));
    }
}
