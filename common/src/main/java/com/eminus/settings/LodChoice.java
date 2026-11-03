package com.eminus.settings;

import java.util.Optional;

public enum LodChoice {
    ULTRA("ultra", true, LeafMode.SEE_THROUGH, GrassAndFlowers.FULL, true, CornerOcclusion.FINEST_LEVEL),
    HIGH("high", true, LeafMode.OPAQUE, GrassAndFlowers.SIMPLIFIED, true, CornerOcclusion.FINEST_LEVEL),
    MEDIUM("medium", true, LeafMode.OPAQUE, GrassAndFlowers.SIMPLIFIED, false, CornerOcclusion.FINEST_LEVEL),
    LOW("low", true, LeafMode.OPAQUE, GrassAndFlowers.OFF, false, CornerOcclusion.OFF),
    CUSTOM("custom", false, LeafMode.OPAQUE, GrassAndFlowers.OFF, false, CornerOcclusion.OFF);

    private static final int PRESET_STORED_LEVEL = LodQuality.MIN_STORED_LEVEL;
    private static final int FORMER_ULTRA_LEVEL = 0;
    private static final int FORMER_MEDIUM_LEVEL = 1;

    private final String key;
    private final boolean preset;
    private final LeafMode leaves;
    private final GrassAndFlowers grassAndFlowers;
    private final boolean fullTextures;
    private final CornerOcclusion cornerOcclusion;

    LodChoice(String key, boolean preset, LeafMode leaves, GrassAndFlowers grassAndFlowers, boolean fullTextures,
            CornerOcclusion cornerOcclusion) {
        this.key = key;
        this.preset = preset;
        this.leaves = leaves;
        this.grassAndFlowers = grassAndFlowers;
        this.fullTextures = fullTextures;
        this.cornerOcclusion = cornerOcclusion;
    }

    public String key() {
        return key;
    }

    public Optional<LodQuality> presetQuality() {
        return preset
                ? Optional.of(new LodQuality(PRESET_STORED_LEVEL, leaves, grassAndFlowers, fullTextures,
                        cornerOcclusion))
                : Optional.empty();
    }

    public LodQuality quality(LodQuality custom) {
        return presetQuality().orElse(custom);
    }

    public static Optional<LodChoice> fromKey(String key) {
        for (LodChoice choice : values()) {
            if (choice.key.equals(key)) {
                return Optional.of(choice);
            }
        }

        return Optional.empty();
    }

    public static LodChoice fromLowestStoredLevel(int level) {
        return switch (level) {
            case FORMER_ULTRA_LEVEL -> ULTRA;
            case FORMER_MEDIUM_LEVEL -> MEDIUM;
            default -> LOW;
        };
    }
}
