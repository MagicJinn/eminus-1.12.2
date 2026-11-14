package com.eminus.vetus.ingest;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * Defers ingest when {@code ChunkEvent.Load} fires before {@code SPacketChunkData} applies section storage.
 */
public final class VetusIngestScheduler {
    private static final int MAX_TICK_RETRIES = 200;
    private static final Map<ColumnRef, Integer> PENDING_TICKS = new LinkedHashMap<>();

    private VetusIngestScheduler() {
    }

    public static void schedule(ColumnRef column) {
        PENDING_TICKS.putIfAbsent(column, 0);
    }

    public static void drop(ColumnRef column) {
        PENDING_TICKS.remove(column);
    }

    public static void clientTick() {
        if (PENDING_TICKS.isEmpty()) {
            return;
        }
        Minecraft client = Minecraft.getMinecraft();
        World world = client.world;
        if (world == null || !world.isRemote) {
            PENDING_TICKS.clear();
            return;
        }
        Iterator<Map.Entry<ColumnRef, Integer>> iterator = PENDING_TICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<ColumnRef, Integer> entry = iterator.next();
            ColumnRef column = entry.getKey();
            int ticks = entry.getValue() + 1;
            entry.setValue(ticks);

            Chunk chunk = world.getChunk(column.chunkX(), column.chunkZ());
            if (chunk == null) {
                if (ticks >= MAX_TICK_RETRIES) {
                    iterator.remove();
                    EminusVetus.LOGGER.debug(
                            "Dropping pending ingest for {},{} dim {} after {} ticks (no chunk)",
                            column.chunkX(), column.chunkZ(), column.dimensionId(), ticks);
                }
                continue;
            }
            if (!VetusColumnIngest.hasReceivedPacketData(chunk) && ticks < MAX_TICK_RETRIES) {
                continue;
            }
            VetusIngestHooks.sink().onColumnAvailable(column, IngestTrigger.PACKET, chunk);
            iterator.remove();
        }
    }
}