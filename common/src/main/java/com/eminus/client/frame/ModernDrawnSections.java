package com.eminus.client.frame;

import com.eminus.client.handoff.DrawnSections;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;

public record ModernDrawnSections(LevelRenderer renderer) implements DrawnSections {
    @Override
    public void collectDrawn(LongSet into) {
        GameFrames.drawnSections(renderer, into);
    }

    @Override
    public boolean sectionDrawn(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                sectionX * 16, sectionY * 16, sectionZ * 16);
        return GameFrames.sectionDrawn(renderer, pos, sectionFadeMillis);
    }

    @Override
    public boolean sectionEmpty(int sectionX, int sectionY, int sectionZ, long sectionFadeMillis) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(
                sectionX * 16, sectionY * 16, sectionZ * 16);
        return GameFrames.sectionEmpty(renderer, pos);
    }
}