package com.eminus.ingest.port;

/**
 * Identifies a chunk column available for far LOD ingest on the client.
 */
public record ColumnRef(int chunkX, int chunkZ, int dimensionId) {
}