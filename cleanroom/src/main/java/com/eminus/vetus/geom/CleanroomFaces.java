package com.eminus.vetus.geom;

import com.eminus.geom.Face;

import net.minecraft.util.EnumFacing;

public final class CleanroomFaces {
    public static Face of(EnumFacing face) {
        return Face.fromVanillaIndex(face.getIndex());
    }

    public static EnumFacing toCleanroom(Face face) {
        return EnumFacing.byIndex(face.vanillaIndex());
    }

    private CleanroomFaces() {
    }
}