package com.eminus.vetus.client;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;

/**
 * Saves and restores GL state around the upstream far pass on 1.12.2 Forge.
 */
public final class VetusGlStateGuard implements AutoCloseable {
    private static final int TRACKED_TEXTURE_UNITS = 12;
    private static final int VAO_BINDING = 0x85B5;

    private final int framebuffer;
    private final int[] viewport = new int[4];
    private final int program;
    private final int vao;
    private final int arrayBuffer;
    private final int elementBuffer;
    private final int[] boundTextures = new int[TRACKED_TEXTURE_UNITS];
    private final int activeTexture;

    private VetusGlStateGuard() {
        Framebuffer main = Minecraft.getMinecraft().getFramebuffer();
        framebuffer = main.framebufferObject;
        IntBuffer viewportBuffer = BufferUtils.createIntBuffer(4);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewportBuffer);
        viewportBuffer.get(viewport);
        activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        vao = GL11.glGetInteger(VAO_BINDING);
        arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        elementBuffer = GL11.glGetInteger(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        for (int unit = 0; unit < TRACKED_TEXTURE_UNITS; unit++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            boundTextures[unit] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }
        GL13.glActiveTexture(activeTexture);
    }

    public static VetusGlStateGuard capture() {
        return new VetusGlStateGuard();
    }

    @Override
    public void close() {
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebuffer);
        GlStateManager.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        GL20.glUseProgram(program);
        if (OpenGlHelper.vboSupported) {
            GL30.glBindVertexArray(vao);
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBuffer);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer);
        for (int unit = 0; unit < TRACKED_TEXTURE_UNITS; unit++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + unit);
            GlStateManager.bindTexture(boundTextures[unit]);
        }
        GL13.glActiveTexture(activeTexture);
        if (OpenGlHelper.vboSupported) {
            GL30.glBindVertexArray(0);
        }
    }
}