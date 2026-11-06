package com.eminus.store;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import com.eminus.cell.Cell;
import com.eminus.cell.CellKey;
import com.eminus.cell.DetailLevel;
import com.eminus.cell.VoxelEntry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CellRecordTest {
    private static final long STONE = VoxelEntry.pack(17, 4, VoxelEntry.light(0, 0));
    private static final long GRASS = VoxelEntry.pack(9, 4, VoxelEntry.light(15, 0));
    private static final int VERSION_OFFSET = 0;
    private static final int LENGTH_OFFSET = Byte.BYTES;
    private static final int BIOMES = 64;
    private static final int LIGHT_LEVELS = 16;
    private static final int INDEX_STRIDE = 3;
    private static final byte FLIPPED_BITS = 0x5A;

    @Test
    void aBlankCellSurvivesTheRoundTripAtEveryLevel() {
        for (int level = DetailLevel.MIN; level <= DetailLevel.MAX; level++) {
            long key = CellKey.pack(level, -3, 1, 7);
            Cell cell = Cell.blank(key);

            assertSameCell(cell, CellRecord.decode(key, CellRecord.encode(cell)));
        }
    }

    @Test
    void aCellOfAFewEntriesSurvivesTheRoundTripAtEveryLevel() {
        for (int level = DetailLevel.MIN; level <= DetailLevel.MAX; level++) {
            long key = CellKey.pack(level, 5, -2, -9);
            Cell cell = Cell.blank(key);
            cell.set(0, 0, 0, STONE);
            cell.set(31, 31, 31, GRASS);
            cell.set(16, 7, 3, STONE);

            Cell decoded = CellRecord.decode(key, CellRecord.encode(cell));

            assertSameCell(cell, decoded);
            assertEquals(3, decoded.paletteSize());
        }
    }

    @Test
    void aCellWithOneEntryPerVoxelSurvivesTheRoundTrip() {
        long key = CellKey.pack(DetailLevel.MAX, 0, 0, 0);
        Cell cell = distinctPerVoxel(key);

        Cell decoded = CellRecord.decode(key, CellRecord.encode(cell));

        assertSameCell(cell, decoded);
        assertEquals(DetailLevel.VOXELS_PER_CELL + 1, decoded.paletteSize());
    }

    @Test
    void aRecordIsSmallerThanTheCellItCarries() {
        long key = CellKey.pack(0, 0, 0, 0);
        Cell cell = Cell.blank(key);
        cell.set(1, 2, 3, STONE);

        assertTrue(CellRecord.encode(cell).length < DetailLevel.VOXELS_PER_CELL * Short.BYTES);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 200, 300, Cell.MAX_PALETTE})
    void aCellSurvivesTheRoundTripWhateverItsPaletteSize(int paletteSize) {
        long key = CellKey.pack(0, 1, -1, 2);
        long[] palette = new long[paletteSize];
        for (int entry = 0; entry < paletteSize; entry++) {
            palette[entry] = VoxelEntry.pack(entry + 1, entry % BIOMES, VoxelEntry.light(entry % LIGHT_LEVELS, 0));
        }

        short[] indices = new short[DetailLevel.VOXELS_PER_CELL];
        for (int voxel = 0; voxel < indices.length; voxel++) {
            indices[voxel] = (short) ((voxel * INDEX_STRIDE + 1) % paletteSize);
        }

        Cell cell = Cell.of(key, palette, indices);

        Cell decoded = CellRecord.decode(key, CellRecord.encode(cell));

        assertArrayEquals(cell.copyPalette(), decoded.copyPalette());
        assertArrayEquals(cell.indices(), decoded.indices());
        assertEquals(cell.occupancy(), decoded.occupancy());
    }

    @Test
    void aNewRecordCarriesTheCurrentRecordVersion() {
        long key = CellKey.pack(0, 0, 0, 0);

        assertEquals(StoreFormat.RECORD_VERSION, CellRecord.encode(Cell.blank(key))[VERSION_OFFSET]);
    }

    @Test
    void aZstdRecordOfStoreFormatThreeStillDecodes() {
        long key = CellKey.pack(0, 2, 1, -4);
        Cell few = Cell.blank(key);
        few.set(16, 7, 3, STONE);
        few.set(0, 0, 0, GRASS);

        for (Cell cell : List.of(few, distinctPerVoxel(key))) {
            byte[] record = LegacyRecords.zstd(cell);

            assertEquals(StoreFormat.ZSTD_RECORD_VERSION, record[VERSION_OFFSET]);
            assertSameCell(cell, CellRecord.decode(key, record));
        }
    }

    @Test
    void aRecordWhoseBodyWasAlteredIsDamaged() {
        long key = CellKey.pack(1, 1, 1, 1);
        byte[] record = CellRecord.encode(distinctPerVoxel(key));
        record[record.length / 2] ^= FLIPPED_BITS;

        assertThrows(DamagedRecordException.class, () -> CellRecord.decode(key, record));
    }

    @Test
    void aRecordOfAnotherFormatVersionIsDamaged() {
        long key = CellKey.pack(1, 1, 1, 1);
        byte[] record = CellRecord.encode(Cell.blank(key));
        record[VERSION_OFFSET] = (byte) (StoreFormat.RECORD_VERSION + 1);

        DamagedRecordException damaged =
                assertThrows(DamagedRecordException.class, () -> CellRecord.decode(key, record));
        assertTrue(damaged.getMessage().contains(String.valueOf(StoreFormat.RECORD_VERSION + 1)));
    }

    @Test
    void aRecordWithoutABodyIsDamaged() {
        long key = CellKey.pack(1, 1, 1, 1);
        byte[] record = Arrays.copyOf(CellRecord.encode(Cell.blank(key)), LENGTH_OFFSET + Integer.BYTES);

        assertThrows(DamagedRecordException.class, () -> CellRecord.decode(key, record));
    }

    @Test
    void aRecordClaimingAnImpossiblePlainLengthIsDamaged() {
        long key = CellKey.pack(1, 1, 1, 1);
        byte[] record = CellRecord.encode(Cell.blank(key));
        ByteBuffer.wrap(record).putInt(LENGTH_OFFSET, Integer.MAX_VALUE);

        assertThrows(DamagedRecordException.class, () -> CellRecord.decode(key, record));
    }

    @Test
    void aTruncatedBodyIsRefusedRatherThanDecoded() {
        long key = CellKey.pack(1, 1, 1, 1);
        byte[] whole = CellRecord.encode(distinctPerVoxel(key));
        byte[] record = Arrays.copyOf(whole, whole.length / 2);

        assertThrows(RuntimeException.class, () -> CellRecord.decode(key, record));
    }

    private static Cell distinctPerVoxel(long key) {
        Cell cell = Cell.blank(key);
        int voxel = 0;
        for (int y = 0; y < DetailLevel.VOXELS_PER_SIDE; y++) {
            for (int z = 0; z < DetailLevel.VOXELS_PER_SIDE; z++) {
                for (int x = 0; x < DetailLevel.VOXELS_PER_SIDE; x++) {
                    cell.set(x, y, z, VoxelEntry.pack(++voxel, 4, VoxelEntry.light(0, 0)));
                }
            }
        }

        return cell;
    }

    private static void assertSameCell(Cell expected, Cell actual) {
        assertEquals(expected.key(), actual.key());
        assertEquals(expected.occupancy(), actual.occupancy());
        assertEquals(expected.paletteSize(), actual.paletteSize());

        for (int y = 0; y < DetailLevel.VOXELS_PER_SIDE; y++) {
            for (int z = 0; z < DetailLevel.VOXELS_PER_SIDE; z++) {
                for (int x = 0; x < DetailLevel.VOXELS_PER_SIDE; x++) {
                    assertEquals(expected.get(x, y, z), actual.get(x, y, z));
                }
            }
        }
    }
}
