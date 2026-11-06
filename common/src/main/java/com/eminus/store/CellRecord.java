package com.eminus.store;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import com.eminus.cell.Cell;
import com.eminus.cell.DetailLevel;

import io.airlift.compress.zstd.ZstdDecompressor;

public final class CellRecord {
    private static final int VERSION_BYTES = Byte.BYTES;
    private static final int LENGTH_BYTES = Integer.BYTES;
    private static final int HEADER_BYTES = VERSION_BYTES + LENGTH_BYTES;

    private static final int VOXELS = DetailLevel.VOXELS_PER_CELL;
    private static final int INDEX_BYTES = VOXELS * Short.BYTES;
    private static final int PALETTE_SIZE_BYTES = Integer.BYTES;
    private static final int MIN_PLAIN_BYTES = PALETTE_SIZE_BYTES + Long.BYTES + INDEX_BYTES;
    private static final int MAX_PLAIN_BYTES = PALETTE_SIZE_BYTES + Cell.MAX_PALETTE * Long.BYTES + INDEX_BYTES;

    private static final int DEFLATE_LEVEL = 6;
    private static final int FIRST_GUESS_SHIFT = 2;
    private static final int GROWTH = 2;
    private static final int OVERRUN_BYTES = 1;
    private static final int BYTE_MASK = 0xFF;

    private static final ThreadLocal<Deflater> DEFLATERS = ThreadLocal.withInitial(() -> new Deflater(DEFLATE_LEVEL));
    private static final ThreadLocal<Inflater> INFLATERS = ThreadLocal.withInitial(Inflater::new);

    public static byte[] encode(Cell cell) {
        return deflated(plain(cell));
    }

    private static byte[] plain(Cell cell) {
        long[] palette = cell.copyPalette();
        short[] indices = cell.indices();

        int highStart = PALETTE_SIZE_BYTES + palette.length * Long.BYTES;
        ByteBuffer plain = ByteBuffer.allocate(highStart + INDEX_BYTES);
        plain.putInt(palette.length);
        for (long entry : palette) {
            plain.putLong(entry);
        }

        byte[] bytes = plain.array();
        int lowStart = highStart + VOXELS;
        for (int voxel = 0; voxel < VOXELS; voxel++) {
            short index = indices[voxel];
            bytes[highStart + voxel] = (byte) (index >> Byte.SIZE);
            bytes[lowStart + voxel] = (byte) index;
        }

        return bytes;
    }

    private static byte[] deflated(byte[] plain) {
        Deflater deflater = DEFLATERS.get();
        try {
            deflater.setInput(plain);
            deflater.finish();
            byte[] record = new byte[HEADER_BYTES + (plain.length >> FIRST_GUESS_SHIFT)];
            int length = HEADER_BYTES;
            while (!deflater.finished()) {
                if (length == record.length) {
                    record = Arrays.copyOf(record, record.length * GROWTH);
                }

                length += deflater.deflate(record, length, record.length - length);
            }

            ByteBuffer header = ByteBuffer.wrap(record);
            header.put((byte) StoreFormat.RECORD_VERSION);
            header.putInt(plain.length);
            return Arrays.copyOf(record, length);
        } finally {
            deflater.reset();
        }
    }

    public static Cell decode(long key, byte[] record) {
        if (record.length <= HEADER_BYTES) {
            throw new DamagedRecordException("record of " + record.length + " bytes carries no body");
        }

        ByteBuffer header = ByteBuffer.wrap(record);
        int version = header.get();
        if (version != StoreFormat.RECORD_VERSION && version != StoreFormat.ZSTD_RECORD_VERSION) {
            throw new DamagedRecordException("record format version " + version + ", expected "
                    + StoreFormat.RECORD_VERSION + " or " + StoreFormat.ZSTD_RECORD_VERSION);
        }

        int plainLength = header.getInt();
        if (plainLength < MIN_PLAIN_BYTES || plainLength > MAX_PLAIN_BYTES) {
            throw new DamagedRecordException("record claims " + plainLength + " plain bytes");
        }

        boolean planes = version == StoreFormat.RECORD_VERSION;
        byte[] plainBytes = planes ? inflated(record, plainLength) : zstdDecompressed(record, plainLength);

        ByteBuffer plain = ByteBuffer.wrap(plainBytes, 0, plainLength);
        int paletteSize = plain.getInt();
        if (paletteSize < 1 || paletteSize > Cell.MAX_PALETTE
                || plainLength != PALETTE_SIZE_BYTES + paletteSize * Long.BYTES + INDEX_BYTES) {
            throw new DamagedRecordException("record claims a palette of " + paletteSize + " entries");
        }

        long[] palette = new long[paletteSize];
        for (int entry = 0; entry < paletteSize; entry++) {
            palette[entry] = plain.getLong();
        }

        short[] indices = planes ? fromPlanes(plainBytes, plain.position()) : interleaved(plain);
        return Cell.of(key, palette, indices);
    }

    private static byte[] inflated(byte[] record, int plainLength) {
        Inflater inflater = INFLATERS.get();
        try {
            inflater.setInput(record, HEADER_BYTES, record.length - HEADER_BYTES);
            byte[] plain = new byte[plainLength + OVERRUN_BYTES];
            int filled = 0;
            while (!inflater.finished() && filled < plain.length) {
                int remainingBefore = inflater.getRemaining();
                int inflatedNow = inflater.inflate(plain, filled, plain.length - filled);
                if (inflatedNow == 0 && inflater.getRemaining() == remainingBefore) {
                    break;
                }

                filled += inflatedNow;
            }

            if (!inflater.finished() || filled != plainLength) {
                throw new DamagedRecordException("record body inflates to " + filled + " bytes"
                        + (inflater.finished() ? "" : " and more") + ", its header claims " + plainLength);
            }

            return plain;
        } catch (DataFormatException damaged) {
            throw new DamagedRecordException("record body is not Deflate data: " + damaged.getMessage());
        } finally {
            inflater.reset();
        }
    }

    private static byte[] zstdDecompressed(byte[] record, int plainLength) {
        byte[] plain = new byte[plainLength];
        new ZstdDecompressor().decompress(record, HEADER_BYTES, record.length - HEADER_BYTES, plain, 0, plainLength);
        return plain;
    }

    private static short[] fromPlanes(byte[] plain, int highStart) {
        short[] indices = new short[VOXELS];
        int lowStart = highStart + VOXELS;
        for (int voxel = 0; voxel < VOXELS; voxel++) {
            indices[voxel] = (short) ((plain[highStart + voxel] & BYTE_MASK) << Byte.SIZE
                    | (plain[lowStart + voxel] & BYTE_MASK));
        }

        return indices;
    }

    private static short[] interleaved(ByteBuffer plain) {
        short[] indices = new short[VOXELS];
        plain.asShortBuffer().get(indices);
        return indices;
    }

    private CellRecord() {
    }
}
