package com.eminus.model;

import java.util.Arrays;

import org.jspecify.annotations.Nullable;

@SuppressWarnings("ArrayRecordComponent")
public record FaceOverlays(int[] under, @Nullable FaceMip[] mips) {
    public static final FaceOverlays NONE = new FaceOverlays(new int[0], new FaceMip[0]);

    public boolean layered(int face) {
        return face < mips.length && mips[face] != null;
    }

    public FaceMip mip(int face) {
        FaceMip mip = mips[face];
        if (mip == null) {
            throw new IllegalStateException("Face " + face + " carries no overlay.");
        }
        return mip;
    }

    public int under(int face, int texel) {
        return under[face * BakedModel.FACE_TEXELS + texel];
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FaceOverlays overlays && Arrays.equals(under, overlays.under)
                && Arrays.equals(mips, overlays.mips);
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(under) + Arrays.hashCode(mips);
    }

    @Override
    public String toString() {
        return "FaceOverlays[layered=" + Arrays.stream(mips).filter(mip -> mip != null).count() + "]";
    }
}
