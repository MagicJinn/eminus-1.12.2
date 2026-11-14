package com.eminus.vetus.ingest;

import com.eminus.Eminus;
import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.port.ChunkColumnSink;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;

/**
 * Placeholder sink until column voxels are converted into {@code SectionPyramid} on 1.12.2.
 */
public final class VetusChunkColumnSink implements ChunkColumnSink {
    private static final int LOG_EVERY = 64;
    private int ingested;

    @Override
    public void onColumnAvailable(ColumnRef column, IngestTrigger trigger) {
        ingested++;
        if (ingested == 1 || ingested % LOG_EVERY == 0) {
            EminusVetus.LOGGER.debug(
                    "[{}/ingest] column {},{} dim {} trigger {} (count {})",
                    Eminus.MODID,
                    column.chunkX(),
                    column.chunkZ(),
                    column.dimensionId(),
                    trigger,
                    ingested);
        }
    }

    @Override
    public void onColumnUnavailable(ColumnRef column) {
        EminusVetus.LOGGER.debug(
                "[{}/ingest] column {},{} dim {} unavailable",
                Eminus.MODID,
                column.chunkX(),
                column.chunkZ(),
                column.dimensionId());
    }
}