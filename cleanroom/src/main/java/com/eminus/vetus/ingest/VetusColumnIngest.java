package com.eminus.vetus.ingest;

import com.eminus.Eminus;
import com.eminus.cell.Dictionary;
import com.eminus.cell.DetailLevel;
import com.eminus.cell.StateLayers;
import com.eminus.cell.StateOpacity;
import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.PyramidDownsampler;
import com.eminus.ingest.SectionPyramid;
import com.eminus.ingest.SectionSliceConverter;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Client column ingest: chunk to {@link SectionPyramid} through upstream {@link SectionSliceConverter} and
 * {@link PyramidDownsampler}. Cell merge and mesh build wire in {@link com.eminus.vetus.client.VetusClientSession}.
 */
public final class VetusColumnIngest {
    private static final int LOG_EVERY = 16;
    private final VetusStateLookup stateLookup = new VetusStateLookup();
    private final Dictionary<String> biomes = new Dictionary<>((id, value) -> { });
    private final VetusStubStateTraits stateTraits = new VetusStubStateTraits();
    private int ingestedColumns;

    public void onColumnAvailable(World world, Chunk chunk, ColumnRef column, IngestTrigger trigger) {
        ExtendedBlockStorage[] sections = chunk.getBlockStorageArray();
        int converted = 0;
        for (int index = 0; index < sections.length; index++) {
            ExtendedBlockStorage storage = sections[index];
            if (storage == null || storage.isEmpty()) {
                continue;
            }
            int baseY = index << 4;
            SectionPyramid pyramid = new SectionPyramid();
            SectionSliceConverter.convert(new VetusChunkSectionSlice(chunk, storage, baseY), stateLookup, biomes,
                    pyramid);
            PyramidDownsampler.build(pyramid, stateTraits, stateTraits);
            converted++;
            com.eminus.vetus.client.VetusClientSession.ingestSection(column, index, pyramid, trigger);
        }

        ingestedColumns++;
        if (ingestedColumns == 1 || ingestedColumns % LOG_EVERY == 0) {
            EminusVetus.LOGGER.info(
                    "[{}/ingest] column {},{} dim {} trigger {} sections {} (count {})",
                    Eminus.MODID,
                    column.chunkX(),
                    column.chunkZ(),
                    column.dimensionId(),
                    trigger,
                    converted,
                    ingestedColumns);
        }
    }

    public void onColumnUnavailable(ColumnRef column) {
        com.eminus.vetus.client.VetusClientSession.dropColumn(column);
        EminusVetus.LOGGER.info(
                "[{}/ingest] column {},{} dim {} unavailable",
                Eminus.MODID,
                column.chunkX(),
                column.chunkZ(),
                column.dimensionId());
    }

    private static final class VetusStubStateTraits implements StateOpacity, StateLayers {
        @Override
        public int opacity(int stateId) {
            return stateId == com.eminus.cell.VoxelEntry.AIR_STATE_ID ? 0 : 15;
        }

        @Override
        public int layered(int blockStateId, int fluidStateId, int blocks) {
            return blockStateId;
        }

        @Override
        public int layerBlocks(int stateId) {
            return DetailLevel.VOXELS_PER_SIDE;
        }

        @Override
        public int layerBlock(int stateId) {
            return stateId;
        }

        @Override
        public int layerFluid(int stateId) {
            return -2;
        }

        @Override
        public boolean holdsFluid(int stateId) {
            return false;
        }
    }
}