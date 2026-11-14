package com.eminus.ingest.port;

import com.eminus.ingest.IngestTrigger;

/**
 * Loader-neutral entry for chunk column ingest. See {@link IngestSidePolicy} for client-only rules.
 *
 * <p>Client-only installs on a remote server must implement this through client chunk load or packet
 * paths only, not logical-server chunk generation. A 1.12 adapter maps client {@code ChunkEvent.Load}
 * on {@code WorldClient} into {@link ColumnRef} plus voxel payloads.
 */
public interface ChunkColumnSink {
    void onColumnAvailable(ColumnRef column, IngestTrigger trigger);

    default void onColumnUnavailable(ColumnRef column) {
    }
}