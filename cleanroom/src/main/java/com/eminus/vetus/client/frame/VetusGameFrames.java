package com.eminus.vetus.client.frame;

import com.eminus.client.frame.FaceShade;
import com.eminus.client.frame.GameFog;
import com.eminus.client.frame.GameFrame;
import com.eminus.client.frame.TerrainFilter;

import net.minecraft.client.Minecraft;

import org.joml.Matrix4f;
import org.joml.Vector4f;

/** 1.12.2 adapter for upstream {@link com.eminus.client.frame.GameFrames}. */
public final class VetusGameFrames {
    private VetusGameFrames() {
    }

    public static GameFrame read(Minecraft client) {
        if (client.player == null || client.world == null) {
            return empty(client);
        }
        double eyeX = client.player.posX;
        double eyeY = client.player.posY + client.player.getEyeHeight();
        double eyeZ = client.player.posZ;
        float fov = client.gameSettings.fovSetting;
        boolean inAir = !client.player.isInWater() && !client.player.onGround;
        int renderDistance = client.gameSettings.renderDistanceChunks;
        Matrix4f view = new Matrix4f();
        view.rotate((float) Math.toRadians(client.player.rotationPitch), 1.0F, 0.0F, 0.0F);
        view.rotate((float) Math.toRadians(client.player.rotationYaw + 180.0F), 0.0F, 1.0F, 0.0F);
        GameFog fog = new GameFog(0.0F, Float.MAX_VALUE, 0.0F, Float.MAX_VALUE, new Vector4f(0.0F, 0.0F, 0.0F, 1.0F));
        return new GameFrame(eyeX, eyeY, eyeZ, view, fov, inAir, fog, renderDistance, 0L, defaultShade(), true, false,
                1, TerrainFilter.LINEAR);
    }

    private static GameFrame empty(Minecraft client) {
        GameFog fog = new GameFog(0.0F, Float.MAX_VALUE, 0.0F, Float.MAX_VALUE, new Vector4f());
        return new GameFrame(0.0, 64.0, 0.0, new Matrix4f(), 70.0F, true, fog, client.gameSettings.renderDistanceChunks,
                0L, defaultShade(), true, false, 1, TerrainFilter.LINEAR);
    }

    private static FaceShade defaultShade() {
        return new FaceShade(0.5F, 1.0F, 0.8F, 0.8F, 0.6F, 0.6F);
    }
}