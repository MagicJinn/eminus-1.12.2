package com.eminus.client.handoff;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Loader-neutral view of which chunk sections vanilla drew this frame. Null on vetus (all sections treated as owned).
 */
public interface DrawnSections {
    void collectDrawn(LongSet into);

    boolean sectionDrawn(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis);

    boolean sectionEmpty(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis);
}