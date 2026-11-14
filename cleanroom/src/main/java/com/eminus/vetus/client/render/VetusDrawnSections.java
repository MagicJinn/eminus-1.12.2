package com.eminus.vetus.client.render;

import com.eminus.client.handoff.DrawnSections;

import it.unimi.dsi.fastutil.longs.LongSet;

/** Vetus has no stable section visibility API yet. Treat every section as available. */
public final class VetusDrawnSections implements DrawnSections {
    @Override
    public void collectDrawn(LongSet into) {
        into.clear();
    }

    @Override
    public boolean sectionDrawn(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis) {
        return true;
    }

    @Override
    public boolean sectionEmpty(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis) {
        return false;
    }
}