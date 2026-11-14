package com.eminus.vetus.ingest;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.port.ChunkColumnSink;
import com.eminus.ingest.port.ColumnRef;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;

/**
 * Forwards loaded client columns into upstream ingest ({@link SectionSliceConverter} then {@link PyramidDownsampler}).
 */
public final class VetusChunkColumnSink implements ChunkColumnSink {
    private final VetusColumnIngest ingest = new VetusColumnIngest();

    @Override
    public void onColumnAvailable(ColumnRef column, IngestTrigger trigger) {
        Minecraft client = Minecraft.getMinecraft();
        if (client.world == null) {
            return;
        }
        Chunk chunk = client.world.getChunk(column.chunkX(), column.chunkZ());
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        ingest.onColumnAvailable(client.world, chunk, column, trigger);
    }

    @Override
    public void onColumnUnavailable(ColumnRef column) {
        ingest.onColumnUnavailable(column);
    }
}