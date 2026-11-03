package com.eminus.settings;

import java.util.Optional;

public enum GrassAndFlowers {
    FULL("full"),
    SIMPLIFIED("simplified"),
    OFF("off");

    private final String key;

    GrassAndFlowers(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public boolean drawn() {
        return this != OFF;
    }

    public boolean splitsGroundCover() {
        return this == FULL;
    }

    public static GrassAndFlowers of(boolean drawn) {
        return drawn ? FULL : OFF;
    }

    public static Optional<GrassAndFlowers> fromKey(String key) {
        for (GrassAndFlowers value : values()) {
            if (value.key.equals(key)) {
                return Optional.of(value);
            }
        }

        return Optional.empty();
    }
}
