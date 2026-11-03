package com.eminus.cell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.eminus.VanillaBootstrap;

import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StateTableTest {
    private static final String DAMAGED_STATE = "nosuchmod:nosuchblock";
    private static final int DAMAGED_ID = 1;
    private static final int UNKNOWN_ID = 99;
    private static final int ID_PAST_CAPACITY = 1000;

    @BeforeAll
    static void bootstrapVanilla() {
        VanillaBootstrap.ensure();
    }

    private final Dictionary<String> ids = new Dictionary<>((id, value) -> { });

    @Test
    void everyAirStateTakesTheReservedFirstId() {
        StateTable table = new StateTable(ids);

        assertEquals(VoxelEntry.AIR_STATE_ID, table.idOf(Blocks.AIR.defaultBlockState()));
        assertEquals(VoxelEntry.AIR_STATE_ID, table.idOf(Blocks.CAVE_AIR.defaultBlockState()));
        assertEquals(VoxelEntry.AIR_STATE_ID, table.idOf(Blocks.VOID_AIR.defaultBlockState()));
    }

    @Test
    void opacityFollowsTheStatesLightDampening() {
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        StateTable table = new StateTable(ids);

        assertEquals(StateTable.FULL_OPACITY, stone.getLightDampening());
        assertEquals(stone.getLightDampening(), table.opacity(table.idOf(stone)));
        assertEquals(glass.getLightDampening(), table.opacity(table.idOf(glass)));
    }

    @Test
    void leavesArePinnedToFullOpacityAboveTheirLightDampening() {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
        StateTable table = new StateTable(ids);

        assertTrue(leaves.getLightDampening() < StateTable.FULL_OPACITY);
        assertEquals(StateTable.FULL_OPACITY, table.opacity(table.idOf(leaves)));
    }

    @Test
    void theSeeThroughViewGivesLeavesTheirLightDampeningAndLeavesTheRestAlone() {
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        StateTable table = new StateTable(ids);

        assertEquals(leaves.getLightDampening(), table.seeThroughLeaves().opacity(table.idOf(leaves)));
        assertEquals(StateTable.FULL_OPACITY, table.seeThroughLeaves().opacity(table.idOf(stone)));
    }

    @Test
    void snowLayersAndCarpetsAreCoversAndFullOrRaisedBlocksAreNot() {
        StateTable table = new StateTable(ids);

        assertTrue(table.cover(table.idOf(Blocks.SNOW.defaultBlockState())));
        assertTrue(table.cover(table.idOf(Blocks.CARPET.pick(DyeColor.WHITE).defaultBlockState())));
        assertTrue(table.cover(table.idOf(Blocks.MOSS_CARPET.defaultBlockState())));
        assertFalse(table.cover(table.idOf(
                Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, SnowLayerBlock.MAX_HEIGHT))));
        assertFalse(table.cover(table.idOf(Blocks.STONE.defaultBlockState())));
        assertFalse(table.cover(table.idOf(Blocks.STONE_SLAB.defaultBlockState())));
    }

    @Test
    void aStoredStateComesBackAsItself() {
        BlockState stone = Blocks.STONE.defaultBlockState();
        ids.load(VoxelEntry.AIR_STATE_ID, BlockStateParser.serialize(Blocks.AIR.defaultBlockState()));
        ids.load(DAMAGED_ID, BlockStateParser.serialize(stone));

        assertSame(stone, new StateTable(ids).state(DAMAGED_ID));
    }

    @Test
    void aStoredStatePastTheInitialCapacityComesBackWithItsOpacity() {
        BlockState glass = Blocks.GLASS.defaultBlockState();
        ids.load(VoxelEntry.AIR_STATE_ID, BlockStateParser.serialize(Blocks.AIR.defaultBlockState()));
        ids.load(ID_PAST_CAPACITY, BlockStateParser.serialize(glass));
        StateTable table = new StateTable(ids);

        assertSame(glass, table.state(ID_PAST_CAPACITY));
        assertEquals(glass.getLightDampening(), table.opacity(ID_PAST_CAPACITY));
        assertEquals(glass.getLightDampening(), table.seeThroughLeaves().opacity(ID_PAST_CAPACITY));
    }

    @Test
    void aLayeredStateAnswersItsBlockAndTheSourceOfItsFluid() {
        BlockState ice = Blocks.ICE.defaultBlockState();
        StateTable table = new StateTable(ids);
        int iceId = table.idOf(ice);
        int layered = table.layered(iceId, table.idOf(Blocks.SEAGRASS.defaultBlockState()), 2);

        assertSame(ice, table.state(layered));
        assertEquals(table.opacity(iceId), table.opacity(layered));
        assertEquals(2, table.layerBlocks(layered));
        assertEquals(iceId, table.layerBlock(layered));
        assertSame(Blocks.WATER.defaultBlockState(), table.state(table.layerFluid(layered)));
        assertTrue(table.holdsFluid(layered));
        assertFalse(table.holdsFluid(iceId));
        assertEquals(StateLayers.NOT_LAYERED, table.layerBlocks(iceId));
    }

    @Test
    void theSameLayerTakesOneId() {
        StateTable table = new StateTable(ids);
        int iceId = table.idOf(Blocks.ICE.defaultBlockState());
        int waterId = table.idOf(Blocks.WATER.defaultBlockState());

        assertEquals(table.layered(iceId, waterId, 1), table.layered(iceId, waterId, 1));
        assertNotEquals(table.layered(iceId, waterId, 1), table.layered(iceId, waterId, 2));
    }

    @Test
    void aStoredLayeredStateComesBackWithItsLayer() {
        BlockState ice = Blocks.ICE.defaultBlockState();
        StateTable first = new StateTable(ids);
        int layered = first.layered(first.idOf(ice), first.idOf(Blocks.WATER.defaultBlockState()), 3);
        StateTable reopened = new StateTable(ids);

        assertSame(ice, reopened.state(layered));
        assertEquals(3, reopened.layerBlocks(layered));
        assertSame(ice, reopened.state(reopened.layerBlock(layered)));
        assertSame(Blocks.WATER.defaultBlockState(), reopened.state(reopened.layerFluid(layered)));
        assertEquals(layered, reopened.layered(reopened.idOf(ice),
                reopened.idOf(Blocks.WATER.defaultBlockState()), 3));
    }

    @Test
    void aStateTheDictionaryCannotResolveGivesThePlaceholder() {
        ids.load(VoxelEntry.AIR_STATE_ID, BlockStateParser.serialize(Blocks.AIR.defaultBlockState()));
        ids.load(DAMAGED_ID, DAMAGED_STATE);
        StateTable table = new StateTable(ids);

        assertSame(StateTable.PLACEHOLDER, table.state(DAMAGED_ID));
        assertSame(StateTable.PLACEHOLDER, table.state(UNKNOWN_ID));
        assertNotEquals(Blocks.AIR.defaultBlockState(), StateTable.PLACEHOLDER);
    }
}
