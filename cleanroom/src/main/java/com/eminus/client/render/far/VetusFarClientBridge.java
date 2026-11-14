package com.eminus.client.render.far;

import java.util.List;

import com.eminus.cell.CellFrame;
import com.eminus.client.frame.GameFrame;
import com.eminus.client.handoff.DrawnSections;
import com.eminus.client.handoff.NearSectionTable;
import com.eminus.client.model.ClientBakery;
import com.eminus.handoff.NearSections;
import com.eminus.mesh.MeshSummary;
import com.eminus.model.PackBlocks;
import com.eminus.session.DimensionRuntime;
import com.eminus.vetus.client.frame.VetusGameFrames;
import com.eminus.vetus.client.render.VetusDrawnSections;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

public final class VetusFarClientBridge implements FarClientBridge {
    private final Minecraft client;

    public VetusFarClientBridge(Minecraft client) {
        this.client = client;
    }

    @Override
    public GameFrame readFrame() {
        return VetusGameFrames.read(client);
    }

    @Override
    public float settingsFovDegrees() {
        return client.gameSettings.fovSetting;
    }

    @Override
    public int renderDistanceChunks() {
        return client.gameSettings.renderDistanceChunks;
    }

    @Override
    public LevelExtents levelExtents() {
        WorldClient level = client.world;
        if (level == null) {
            return new LevelExtents(0, 256, 0, 16);
        }
        return new LevelExtents(0, 256, 0, 16);
    }

    @Override
    public void fillNearSections(NearSectionTable table, GameFrame game, CellFrame frame, int extraChunks,
            List<MeshSummary> meshes) {
        if (client.world == null) {
            return;
        }
        DrawnSections drawn = new VetusDrawnSections();
        table.fill(drawn, game.sectionFadeMillis(), meshes, frame, NearSections.section((int) Math.floor(game.eyeX())),
                NearSections.section((int) Math.floor(game.eyeY())), NearSections.section((int) Math.floor(game.eyeZ())),
                game.renderDistance(), game.renderDistance() + extraChunks, 0, 16);
    }

    @Override
    public void updateOverlays(FarOverlays overlays, DimensionRuntime runtime, GameFrame game) {
        overlays.update(runtime, game);
        if (client.world != null) {
            overlays.beams().update(runtime.beams(), game, (sectionX, sectionZ) -> client.world
                    .getChunkProvider().getLoadedChunk(sectionX, sectionZ) != null);
        }
    }

    @Override
    public int biomeBlendRadius() {
        return 0;
    }

    @Override
    public ClientBakery createBakery(PackBlocks packBlocks) {
        return ClientBakery.placeholder(packBlocks);
    }
}