package com.eminus.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.eminus.VanillaBootstrap;
import com.eminus.model.port.ModelQuad;
import com.eminus.settings.GrassAndFlowers;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ModelBakerTest {
    private static final float[] NO_UV = new float[ModelQuad.CORNERS * 2];
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float LITTER_HEIGHT = 0.25F * PIXEL;
    private static final float PETALS_HEIGHT = 2.99F * PIXEL;
    private static final float RAIL_HEIGHT = PIXEL;
    private static final float STEM_SIDE = PIXEL;
    private static final float HALF = 0.5F;
    private static final int FULL_BED = 4;

    @BeforeAll
    static void bootstrapVanilla() {
        VanillaBootstrap.ensure();
    }

    @Test
    void petalsAreAGroundCover() {
        assertTrue(groundCover(Blocks.PINK_PETALS, flowerBed()));
    }

    @Test
    void aFlatModelOutsideVegetationIsNoGroundCover() {
        assertFalse(groundCover(Blocks.RAIL, plane(RAIL_HEIGHT, 1.0F)));
    }

    @Test
    void aFlatVegetationWithCollisionIsNoGroundCover() {
        assertFalse(groundCover(Blocks.LILY_PAD, plane(LITTER_HEIGHT, 1.0F)));
    }

    @Test
    void aThinBoxIsNoGroundCover() {
        assertFalse(groundCover(Blocks.MOSS_CARPET, box(0.0F, 1.0F, PIXEL)));
        assertFalse(groundCover(Blocks.SNOW, box(0.0F, 1.0F, 2.0F * PIXEL)));
        assertFalse(groundCover(Blocks.STONE_PRESSURE_PLATE, box(PIXEL, 1.0F - PIXEL, PIXEL)));
        assertFalse(groundCover(Blocks.PINK_PETALS, box(0.0F, 1.0F, PIXEL)));
    }

    @Test
    void aCrossIsNoGroundCover() {
        assertFalse(groundCover(Blocks.SHORT_GRASS, List.of(stem(0.0F, 0.0F, 1.0F, 1.0F))));
    }

    @Test
    void simplifiedKeepsAFullFlowerBedWhole() {
        List<List<ModelQuad>> pieces = ModelBaker.pieces(fullPinkPetals(), FlowerBedQuads.bed(),
                GrassAndFlowers.SIMPLIFIED);

        assertEquals(1, pieces.size());
        assertEquals(FlowerBedQuads.bed().size(), pieces.getFirst().size());
    }

    @Test
    void fullSplitsAFullFlowerBedAsGroundCoverPiecesDo() {
        List<List<ModelQuad>> pieces = ModelBaker.pieces(fullPinkPetals(), FlowerBedQuads.bed(), GrassAndFlowers.FULL);

        assertEquals(GroundCoverPieces.split(FlowerBedQuads.bed()).size(), pieces.size());
        assertTrue(pieces.size() > 1);
    }

    private static BlockState fullPinkPetals() {
        return Blocks.PINK_PETALS.defaultBlockState().setValue(PinkPetalsBlock.AMOUNT, FULL_BED);
    }

    private static boolean groundCover(Block block, List<ModelQuad> quads) {
        return ModelBaker.groundCover(block.defaultBlockState(), quads);
    }

    private static List<ModelQuad> flowerBed() {
        List<ModelQuad> quads = new ArrayList<>(plane(PETALS_HEIGHT, HALF));
        quads.add(stem(HALF * HALF, HALF * HALF, STEM_SIDE, PETALS_HEIGHT));
        return quads;
    }

    private static List<ModelQuad> plane(float y, float side) {
        return List.of(
                quad(Direction.UP, new float[] {0, y, side, side, y, side, side, y, 0, 0, y, 0}),
                quad(Direction.DOWN, new float[] {0, y, 0, side, y, 0, side, y, side, 0, y, side}));
    }

    private static ModelQuad stem(float x, float z, float side, float top) {
        return quad(Direction.NORTH, new float[] {
            x, 0, z, x + side, 0, z + side, x + side, top, z + side, x, top, z});
    }

    private static List<ModelQuad> box(float from, float to, float top) {
        return List.of(
                quad(Direction.DOWN, new float[] {from, 0, from, to, 0, from, to, 0, to, from, 0, to}),
                quad(Direction.UP, new float[] {from, top, to, to, top, to, to, top, from, from, top, from}),
                quad(Direction.NORTH, new float[] {from, top, from, to, top, from, to, 0, from, from, 0, from}),
                quad(Direction.SOUTH, new float[] {from, 0, to, to, 0, to, to, top, to, from, top, to}),
                quad(Direction.WEST, new float[] {from, 0, from, from, 0, to, from, top, to, from, top, from}),
                quad(Direction.EAST, new float[] {to, top, from, to, top, to, to, 0, to, to, 0, from}));
    }

    private static ModelQuad quad(Direction direction, float[] positions) {
        return FaceRasterizerTest.quad(direction, direction, positions, NO_UV, ModelQuad.NO_TINT);
    }
}
