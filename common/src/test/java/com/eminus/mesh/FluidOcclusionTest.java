package com.eminus.mesh;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.eminus.VanillaBootstrap;
import com.eminus.cell.Cell;
import com.eminus.cell.CellKey;
import com.eminus.cell.DetailLevel;
import com.eminus.cell.VoxelEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FluidOcclusionTest {
    private static final int BIOME = 0;
    private static final int STONE = 1;
    private static final int WATER = 2;
    private static final int LAVA = 3;
    private static final int LAVA_KIND = 1;
    private static final float SOURCE_HEIGHT = 8.0F / 9.0F;
    private static final float SLOPED_HEIGHT = 0.5F;
    private static final BlockPos CENTER = new BlockPos(8, 8, 8);

    private final FakeModels models = new FakeModels();
    private final Cell cell = Cell.blank(CellKey.pack(DetailLevel.MIN, 0, 0, 0));

    @BeforeAll
    static void bootstrap() {
        VanillaBootstrap.ensure();
    }

    @Test
    void aWaterTopByTheShoreTakesTheLayerAboveAndItsOwn() {
        models.water(WATER, SOURCE_HEIGHT);
        int word = shore(WATER).fluid(Direction.UP, CENTER.getX(), CENTER.getY(), CENTER.getZ(), WATER);

        assertEquals(OcclusionWord.LOW_U, OcclusionWord.outer(word));
        assertEquals(OcclusionWord.HIGH_U, OcclusionWord.inner(word));
    }

    @Test
    void aSlopedWaterTopTakesTheSameWords() {
        models.water(WATER, SLOPED_HEIGHT);
        int word = shore(WATER).fluid(Direction.UP, CENTER.getX(), CENTER.getY(), CENTER.getZ(), WATER);

        assertEquals(OcclusionWord.of(OcclusionWord.LOW_U, OcclusionWord.HIGH_U), word);
    }

    @Test
    void aWaterSideTakesTheLayerInFrontAlone() {
        models.water(WATER, SOURCE_HEIGHT);
        place(CENTER.north().east(), STONE);
        int word = shore(WATER).fluid(Direction.NORTH, CENTER.getX(), CENTER.getY(), CENTER.getZ(), WATER);

        assertEquals(OcclusionWord.HIGH_U, word);
    }

    @Test
    void waterTakesNoWordWithoutSodium() {
        models.water(WATER, SOURCE_HEIGHT);
        int word = shore(WATER, CornerRule.OWN_SIDE, false)
                .fluid(Direction.UP, CENTER.getX(), CENTER.getY(), CENTER.getZ(), WATER);

        assertEquals(OcclusionWord.NONE, word);
    }

    @Test
    void waterTakesNoWordOnALevelWithoutCornerOcclusion() {
        models.water(WATER, SOURCE_HEIGHT);
        int word = shore(WATER, CornerRule.NONE, true)
                .fluid(Direction.UP, CENTER.getX(), CENTER.getY(), CENTER.getZ(), WATER);

        assertEquals(OcclusionWord.NONE, word);
    }

    @Test
    void lavaTakesNoWord() {
        models.holds(LAVA, LAVA_KIND, SOURCE_HEIGHT);
        int word = shore(LAVA).fluid(Direction.UP, CENTER.getX(), CENTER.getY(), CENTER.getZ(), LAVA);

        assertEquals(OcclusionWord.NONE, word);
    }

    private FaceOcclusion shore(int fluid) {
        return shore(fluid, CornerRule.OWN_SIDE, true);
    }

    private FaceOcclusion shore(int fluid, CornerRule rule, boolean water) {
        models.shadeAs(STONE, StateShade.of(Blocks.STONE.defaultBlockState()));
        models.shadeAs(WATER, StateShade.of(Blocks.WATER.defaultBlockState()));
        models.shadeAs(LAVA, StateShade.of(Blocks.LAVA.defaultBlockState()));
        place(CENTER, fluid);
        place(CENTER.east(), STONE);
        place(CENTER.above().west(), STONE);

        CellVoxels voxels = new CellVoxels();
        voxels.load(cell);
        for (int stepY = -1; stepY <= 1; stepY++) {
            for (int stepZ = -1; stepZ <= 1; stepZ++) {
                for (int stepX = -1; stepX <= 1; stepX++) {
                    if (stepX != 0 || stepY != 0 || stepZ != 0) {
                        voxels.loadRegion(stepX, stepY, stepZ, Cell.blank(CellKey.pack(DetailLevel.MIN, stepX, stepY,
                                stepZ)));
                    }
                }
            }
        }

        return new FaceOcclusion(voxels, models, DetailLevel.MIN, rule, water);
    }

    private void place(BlockPos pos, int stateId) {
        cell.set(pos.getX(), pos.getY(), pos.getZ(),
                VoxelEntry.pack(stateId, BIOME, VoxelEntry.light(VoxelEntry.MAX_LIGHT, 0)));
    }
}
