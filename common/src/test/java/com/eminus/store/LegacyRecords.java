package com.eminus.store;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.eminus.cell.Cell;
import com.eminus.cell.DetailLevel;

import io.airlift.compress.zstd.ZstdCompressor;

final class LegacyRecords {
    private static final int HEADER_BYTES = Byte.BYTES + Integer.BYTES;

    static byte[] zstd(Cell cell) {
        long[] palette = cell.copyPalette();
        ByteBuffer plain = ByteBuffer.allocate(Integer.BYTES + palette.length * Long.BYTES
                + DetailLevel.VOXELS_PER_CELL * Short.BYTES);
        plain.putInt(palette.length);
        for (long entry : palette) {
            plain.putLong(entry);
        }

        plain.asShortBuffer().put(cell.indices());
        byte[] plainBytes = plain.array();

        ZstdCompressor compressor = new ZstdCompressor();
        byte[] record = new byte[HEADER_BYTES + compressor.maxCompressedLength(plainBytes.length)];
        int packed = compressor.compress(plainBytes, 0, plainBytes.length, record, HEADER_BYTES,
                record.length - HEADER_BYTES);
        ByteBuffer header = ByteBuffer.wrap(record);
        header.put((byte) StoreFormat.ZSTD_RECORD_VERSION);
        header.putInt(plainBytes.length);
        return Arrays.copyOf(record, HEADER_BYTES + packed);
    }

    private LegacyRecords() {
    }
}
