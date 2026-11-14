package com.eminus.client.render.far;

import java.util.List;

import com.eminus.cell.CellFrame;
import com.eminus.client.frame.GameFrame;
import com.eminus.client.frame.GameFrames;
import com.eminus.client.frame.ModernDrawnSections;
import com.eminus.client.handoff.NearSectionTable;
import com.eminus.client.model.ClientBakery;
import com.eminus.client.model.ModernClientBakery;
import com.eminus.handoff.NearSections;
import com.eminus.mesh.MeshSummary;
import com.eminus.model.PackBlocks;
import com.eminus.session.DimensionRuntime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

public final class ModernFarClientBridge implements FarClientBridge {
    private final Minecraft client;

    public ModernFarClientBridge(Minecraft client) {
        this.client = client;
    }

    public Minecraft minecraft() {
        return client;
    }

    @Override
    public GameFrame readFrame() {
        return GameFrames.read(client);
    }

    @Override
    public float settingsFovDegrees() {
        return client.options.fov().get();
    }

    @Override
    public int renderDistanceChunks() {
        return client.options.renderDistance().get();
    }

    @Override
    public LevelExtents levelExtents() {
        ClientLevel level = client.level;
        if (level == null) {
            return new LevelExtents(0, 256, 0, 16);
        }
        return new LevelExtents(level.getMinY(), level.getHeight(), level.getMinSectionY(), level.getSectionsCount());
    }

    @Override
    public void fillNearSections(NearSectionTable table, GameFrame game, CellFrame frame, int extraChunks,
            List<MeshSummary> meshes) {
        ClientLevel level = client.level;
        if (level == null) {
            return;
        }
        table.fill(new ModernDrawnSections(client.levelRenderer), game.sectionFadeMillis(), meshes, frame,
                NearSections.section(Mth.floor(game.eyeX())), NearSections.section(Mth.floor(game.eyeY())),
                NearSections.section(Mth.floor(game.eyeZ())), game.renderDistance(),
                game.renderDistance() + extraChunks, level.getMinSectionY(), level.getSectionsCount());
    }

    @Override
    public void updateOverlays(FarOverlays overlays, DimensionRuntime runtime, GameFrame game) {
        overlays.update(runtime, game);
        ClientLevel level = client.level;
        if (level != null) {
            overlays.beams().update(runtime.beams(), game, (sectionX, sectionZ) -> level.getChunkSource()
                    .hasChunk(sectionX, sectionZ));
        }
    }

    @Override
    public int biomeBlendRadius() {
        return client.options.biomeBlendRadius().get();
    }

    @Override
    public ClientBakery createBakery(PackBlocks packBlocks) {
        return ModernClientBakery.start(client, packBlocks);
    }
}