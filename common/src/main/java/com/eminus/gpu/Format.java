package com.eminus.gpu;

public enum Format {
    RGBA8_UNORM(4),
    R8_UNORM(1),
    D32_FLOAT(4),
    D32_UNORM(4),
    R32_UINT(4),
    RG32_UINT(8),
    RGBA32_UINT(16),
    RGBA32_FLOAT(16),
    R32_FLOAT(4);

    private final int bytesPerTexel;

    Format(int bytesPerTexel) {
        this.bytesPerTexel = bytesPerTexel;
    }

    public int bytesPerTexel() {
        return bytesPerTexel;
    }
}
