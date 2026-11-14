package com.eminus.beam;

import java.nio.ByteBuffer;
import java.util.List;

import com.eminus.platform.math.PackedBlockPos;

public final class BeamTexels {
    public static final int TEXELS = 2;
    public static final int BYTES = TEXELS * 4 * Integer.BYTES;
    public static final int VERTICES_PER_PRISM = 24;

    private static final int UNUSED = 0;

    public static int sections(List<Beam> beams) {
        int count = 0;
        for (Beam beam : beams) {
            count += beam.sections().size();
        }

        return count;
    }

    public static long bytes(int sections) {
        return (long) sections * BYTES;
    }

    public static void write(List<Beam> beams, int lastSectionHeight, ByteBuffer target) {
        for (Beam beam : beams) {
            int x = PackedBlockPos.getX(beam.pos());
            int z = PackedBlockPos.getZ(beam.pos());
            int bottom = PackedBlockPos.getY(beam.pos());
            List<BeamSection> sections = beam.sections();
            for (int index = 0; index < sections.size(); index++) {
                BeamSection section = sections.get(index);
                int top = bottom + (index == sections.size() - 1 ? lastSectionHeight : section.height());
                target.putInt(x).putInt(bottom).putInt(z).putInt(section.argb())
                        .putInt(top).putInt(UNUSED).putInt(UNUSED).putInt(UNUSED);
                bottom += section.height();
            }
        }
    }

    private BeamTexels() {
    }
}
