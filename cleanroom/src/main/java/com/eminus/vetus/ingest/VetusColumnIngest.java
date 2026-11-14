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
import com.eminus.ingest.port.ColumnIngestReadiness;
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
    private final VetusStateLookup stateLookup = new VetusStateLookup();
    private final Dictionary<String> biomes = new Dictionary<>((id, value) -> { });
    private final VetusStubStateTraits stateTraits = new VetusStubStateTraits();
    public void onColumnAvailable(World world, Chunk chunk, ColumnRef column, IngestTrigger trigger) {
        if (!hasReceivedPacketData(chunk)) {
            VetusIngestScheduler.schedule(column);
            return;
        }
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

        VetusIngestSummary.record(converted);
        EminusVetus.LOGGER.debug(
                "[{}/ingest] column {},{} dim {} trigger {} sections {}",
                Eminus.MODID,
                column.chunkX(),
                column.chunkZ(),
                column.dimensionId(),
                trigger,
                converted);
    }

    /**
     * Client chunks exist before {@code SPacketChunkData} applies {@code Chunk.read}. All-null storage means wait.
     */
    public static boolean hasReceivedPacketData(Chunk chunk) {
        return ColumnIngestReadiness.hasAllocatedSection(wrapSections(chunk.getBlockStorageArray()));
    }

    public static int countNonemptySections(Chunk chunk) {
        return ColumnIngestReadiness.countNonemptySections(wrapSections(chunk.getBlockStorageArray()));
    }

    private static ColumnIngestReadiness.SectionSlot[] wrapSections(ExtendedBlockStorage[] sections) {
        if (sections == null) {
            return new ColumnIngestReadiness.SectionSlot[0];
        }
        ColumnIngestReadiness.SectionSlot[] wrapped = new ColumnIngestReadiness.SectionSlot[sections.length];
        for (int i = 0; i < sections.length; i++) {
            final int idx = i;
            wrapped[i] = new ColumnIngestReadiness.SectionSlot() {
                @Override
                public boolean allocated() {
                    return sections[idx] != null;
                }

                @Override
                public boolean empty() {
                    ExtendedBlockStorage slot = sections[idx];
                    return slot == null || slot.isEmpty();
                }
            };
        }
        return wrapped;
    }

    public void onColumnUnavailable(ColumnRef column) {
        VetusIngestScheduler.drop(column);
        com.eminus.vetus.client.VetusClientSession.dropColumn(column);
        EminusVetus.LOGGER.debug(
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