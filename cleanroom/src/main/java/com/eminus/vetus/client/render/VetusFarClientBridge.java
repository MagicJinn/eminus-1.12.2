package com.eminus.vetus.client.render;

import com.eminus.client.frame.GameFrame;
import com.eminus.vetus.client.frame.VetusGameFrames;

import net.minecraft.client.Minecraft;

/**
 * Vetus far bridge shell until {@code FarClientBridge} and GPU far types bundle on {@code :core} for Cleanroom.
 * {@link VetusGameFrames} supplies {@link GameFrame} for the next wiring step.
 */
public final class VetusFarClientBridge {
    private final Minecraft client;

    public VetusFarClientBridge(Minecraft client) {
        this.client = client;
    }

    public GameFrame readFrame() {
        return VetusGameFrames.read(client);
    }

    public int renderDistanceChunks() {
        return client.gameSettings.renderDistanceChunks;
    }

    public float settingsFovDegrees() {
        return client.gameSettings.fovSetting;
    }

}