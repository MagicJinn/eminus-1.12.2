package com.eminus.ingest;

import com.eminus.cell.DetailLevel;
import com.eminus.cell.Dictionary;
import com.eminus.cell.VoxelEntry;
import com.eminus.ingest.port.SectionSlice;
import com.eminus.ingest.port.VoxelStateLookup;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;

public final class SectionSliceConverter {
    private SectionSliceConverter() {
    }

    public static void convert(SectionSlice slice, VoxelStateLookup states, Dictionary<String> biomes,
            SectionPyramid into) {
        into.reset();
        long[] level = into.level(DetailLevel.MIN);
        Reference2IntMap<Object> known = into.stateIds();

        for (int y = 0; y < SectionPyramid.SECTION_SIDE; y++) {
            for (int z = 0; z < SectionPyramid.SECTION_SIDE; z++) {
                for (int x = 0; x < SectionPyramid.SECTION_SIDE; x++) {
                    Object stateKey = slice.blockStateKey(x, y, z);
                    int stateId = known.getInt(stateKey);
                    if (stateId == SectionPyramid.ABSENT) {
                        stateId = states.idOf(stateKey);
                        known.put(stateKey, stateId);
                    }

                    int biomeId = biomeKeyId(slice.biomeKey(x, y, z), biomes, into);
                    int light = VoxelEntry.light(slice.skyLight(x, y, z), slice.blockLight(x, y, z));
                    level[SectionPyramid.indexAt(SectionPyramid.SECTION_SIDE, x, y, z)] =
                            VoxelEntry.pack(stateId, biomeId, light);
                }
            }
        }
    }

    private static int biomeKeyId(String key, Dictionary<String> biomes, SectionPyramid into) {
        if (key == null || key.isEmpty()) {
            return VoxelEntry.KEPT_BIOME;
        }

        Reference2IntMap<Object> known = into.biomeIds();
        int id = known.getInt(key);
        if (id == SectionPyramid.ABSENT) {
            int registered = biomes.id(key);
            if (registered == Dictionary.MISSING) {
                registered = biomes.register(key);
            }
            known.put(key, registered);
            id = registered;
        }

        return id;
    }
}