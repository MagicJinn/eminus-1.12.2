package com.eminus.platform.minecraft;

import com.eminus.geom.Face;

import net.minecraft.core.Direction;

public final class ModernFaces {
    public static Face of(Direction face) {
        return Face.fromVanillaIndex(face.get3DDataValue());
    }

    public static Direction toModern(Face face) {
        return Direction.from3DDataValue(face.vanillaIndex());
    }

    private ModernFaces() {
    }
}