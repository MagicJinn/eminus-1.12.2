package com.eminus.client.gpu.opengl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import com.eminus.gpu.Location;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

/**
 * Cleanroom replacement for Render Pearl {@code GameHandles} (documented as VetusHandles in docs/PORTING.md).
 * Maps main target and GL state to 1.12.2 Forge.
 */
final class GameHandles {
    static final int GAME_TRACKED_TEXTURE_UNITS = 12;

    private static final boolean LIGHTMAP_HALF_TEXEL = true;

    private GameHandles() {
    }

    record Handle(Object owner, int id) {
    }

    static Handle mainColor() {
        Framebuffer target = target();
        return new Handle(target, target.framebufferTexture);
    }

    static Handle mainDepth() {
        Framebuffer target = target();
        return new Handle(target, target.depthBuffer);
    }

    static Handle lightmap() {
        ResourceLocation lightmap = new ResourceLocation("textures/misc/lightmap.png");
        int id = Minecraft.getMinecraft().getTextureManager().getTexture(lightmap).getGlTextureId();
        return new Handle(lightmap, id);
    }

    static boolean depthReversed() {
        return false;
    }

    static boolean lightmapHalfTexel() {
        return LIGHTMAP_HALF_TEXEL;
    }

    static Optional<String> resource(Location file) throws IOException {
        ResourceLocation id = new ResourceLocation(file.namespace(), file.path());
        try (InputStream stream = Minecraft.getMinecraft().getResourceManager().getResource(id).getInputStream()) {
            byte[] bytes = new byte[stream.available()];
            int read = stream.read(bytes);
            if (read < 0) {
                return Optional.empty();
            }
            return Optional.of(new String(bytes, 0, read, StandardCharsets.UTF_8));
        } catch (IOException missing) {
            return Optional.empty();
        }
    }

    static RuntimeException outOfMemory(String message) {
        return new RuntimeException(message);
    }

    private static Framebuffer target() {
        return Minecraft.getMinecraft().getFramebuffer();
    }

    static int genTexture() {
        return GL11.glGenTextures();
    }

    static void deleteTexture(int id) {
        GL11.glDeleteTextures(id);
    }

    static void activeTexture(int unit) {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit + unit);
    }

    static void bindTexture(int id) {
        GlStateManager.bindTexture(id);
    }

    static void bindFramebuffer(int framebuffer) {
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, framebuffer);
    }

    static void bindFramebuffers(int read, int draw) {
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, read);
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, draw);
    }

    static void deleteFramebuffer(int framebuffer) {
        OpenGlHelper.glDeleteFramebuffers(framebuffer);
    }

    static void viewport(int x, int y, int width, int height) {
        GlStateManager.viewport(x, y, width, height);
    }

    static void scissor(int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x, y, width, height);
    }

    static void scissor(boolean enabled, int x, int y, int width, int height) {
        GL11.glScissor(x, y, width, height);
        if (enabled) {
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
        } else {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    static void colorMask(int index, int mask) {
        GlStateManager.colorMask((mask & 1) != 0, (mask & 2) != 0, (mask & 4) != 0, (mask & 8) != 0);
    }

    static void depthTest(int function, boolean writes) {
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(function);
        GlStateManager.depthMask(writes);
    }

    static void noDepthTest() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
    }

    static void depthMask(boolean writes) {
        GlStateManager.depthMask(writes);
    }

    static void blend(int index, int sourceRgb, int destinationRgb, int sourceAlpha, int destinationAlpha) {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE,
                GL11.GL_ZERO);
    }

    static void noBlend(int index) {
        GlStateManager.disableBlend();
    }

    static void fillBothFaces() {
        GlStateManager.disableCull();
        GlStateManager.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    }
}