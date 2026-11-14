package com.eminus.vetus.ingest;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.port.ColumnRef;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Entry points for ingest after client chunk storage is populated.
 */
public final class VetusIngestHooks {
    private static final VetusChunkColumnSink SINK = new VetusChunkColumnSink();

    private VetusIngestHooks() {
    }

    public static void onChunkDataApplied(int chunkX, int chunkZ) {
        Minecraft client = Minecraft.getMinecraft();
        World world = client.world;
        if (world == null || !world.isRemote) {
            return;
        }
        Chunk chunk = world.getChunk(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        int dimension = world.provider.getDimension();
        SINK.onColumnAvailable(new ColumnRef(chunkX, chunkZ, dimension), IngestTrigger.PACKET, chunk);
    }

    static VetusChunkColumnSink sink() {
        return SINK;
    }
}