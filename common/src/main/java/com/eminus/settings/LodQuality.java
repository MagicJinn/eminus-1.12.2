package com.eminus.settings;

public record LodQuality(int storedLevel, LeafMode leaves, GrassAndFlowers grassAndFlowers, boolean fullTextures,
        CornerOcclusion cornerOcclusion) {
    public static final int MIN_STORED_LEVEL = 0;
    public static final int MAX_STORED_LEVEL = 3;

    private static final int FULL_ATLAS_MIP = 0;
    private static final int ECONOMY_ATLAS_MIP = 1;

    public LodQuality {
        if (storedLevel < MIN_STORED_LEVEL || storedLevel > MAX_STORED_LEVEL) {
            throw new IllegalArgumentException("Stored level " + storedLevel + " is outside "
                    + MIN_STORED_LEVEL + ".." + MAX_STORED_LEVEL);
        }
    }

    public boolean seeThroughLeaves() {
        return leaves.seeThrough();
    }

    public int atlasFirstMip() {
        return fullTextures ? FULL_ATLAS_MIP : ECONOMY_ATLAS_MIP;
    }

    public LodQuality withStoredLevel(int value) {
        return new LodQuality(value, leaves, grassAndFlowers, fullTextures, cornerOcclusion);
    }

    public LodQuality withLeaves(LeafMode value) {
        return new LodQuality(storedLevel, value, grassAndFlowers, fullTextures, cornerOcclusion);
    }

    public LodQuality withGrassAndFlowers(GrassAndFlowers value) {
        return new LodQuality(storedLevel, leaves, value, fullTextures, cornerOcclusion);
    }

    public LodQuality withFullTextures(boolean value) {
        return new LodQuality(storedLevel, leaves, grassAndFlowers, value, cornerOcclusion);
    }

    public LodQuality withCornerOcclusion(CornerOcclusion value) {
        return new LodQuality(storedLevel, leaves, grassAndFlowers, fullTextures, value);
    }

    public static int voxelBlocks(int storedLevel) {
        return 1 << storedLevel;
    }
}
