package com.eminus.render.far;

import com.eminus.platform.math.Floors;

public record CameraOrigin(int blockX, int blockY, int blockZ, float offsetX, float offsetY, float offsetZ) {
    public static CameraOrigin of(double x, double y, double z) {
        int blockX = Floors.floor(x);
        int blockY = Floors.floor(y);
        int blockZ = Floors.floor(z);
        return new CameraOrigin(blockX, blockY, blockZ, (float) (blockX - x), (float) (blockY - y),
                (float) (blockZ - z));
    }
}
