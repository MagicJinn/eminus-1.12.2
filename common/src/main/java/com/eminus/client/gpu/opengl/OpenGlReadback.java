package com.eminus.client.gpu.opengl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.eminus.client.gpu.ReadbackRing;
import com.eminus.gpu.Format;
import com.eminus.gpu.buffer.Readback;
import com.eminus.gpu.texture.Texture;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL21C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL32C;

final class OpenGlReadback extends ReadbackRing<OpenGlReadback.Slot> {
    private static final int NO_TEXTURE = 0;
    private static final int BASE_MIP = 0;
    private static final int NO_SKIP = 0;
    private static final long NO_FENCE = 0L;
    private static final long NO_WAIT = 0L;
    private static final long START = 0L;
    private static final String FRAMEBUFFER_SUFFIX = "-framebuffer";

    private final OpenGlObjects objects;
    private final int framebuffer;

    static final class Slot {
        private final int buffer;
        private long fence = NO_FENCE;

        private Slot(int buffer) {
            this.buffer = buffer;
        }
    }

    private record PackState(int buffer, int rowLength, int alignment, int skipPixels, int skipRows) {
        static PackState now() {
            return new PackState(GL11C.glGetInteger(GL21C.GL_PIXEL_PACK_BUFFER_BINDING),
                    GL11C.glGetInteger(GL11C.GL_PACK_ROW_LENGTH), GL11C.glGetInteger(GL11C.GL_PACK_ALIGNMENT),
                    GL11C.glGetInteger(GL11C.GL_PACK_SKIP_PIXELS), GL11C.glGetInteger(GL11C.GL_PACK_SKIP_ROWS));
        }

        void restore() {
            GL15C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, buffer);
            GL11C.glPixelStorei(GL11C.GL_PACK_ROW_LENGTH, rowLength);
            GL11C.glPixelStorei(GL11C.GL_PACK_ALIGNMENT, alignment);
            GL11C.glPixelStorei(GL11C.GL_PACK_SKIP_PIXELS, skipPixels);
            GL11C.glPixelStorei(GL11C.GL_PACK_SKIP_ROWS, skipRows);
        }
    }

    private OpenGlReadback(OpenGlObjects objects, int framebuffer, int maxBytes, List<Slot> slots) {
        super(maxBytes, slots);
        this.objects = objects;
        this.framebuffer = framebuffer;
    }

    static OpenGlReadback create(OpenGlObjects objects, String label, int maxBytes) {
        OpenGlErrors.clear();
        PackState before = PackState.now();
        List<Slot> slots = new ArrayList<>();
        for (int slot = 0; slot < Readback.SLOTS; slot++) {
            int buffer = GL15C.glGenBuffers();
            GL15C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, buffer);
            GL15C.glBufferData(GL21C.GL_PIXEL_PACK_BUFFER, maxBytes, GL15C.GL_STREAM_READ);
            objects.created(OpenGlObjects.Kind.BUFFER, buffer, label + "-" + slot);
            slots.add(new Slot(buffer));
        }
        before.restore();

        int framebuffer = GL30C.glGenFramebuffers();
        GameHandles.Bindings game = GameHandles.bindings();
        GameHandles.bindFramebuffer(framebuffer);
        GL11C.glDrawBuffer(GL11C.GL_NONE);
        GameHandles.restore(game);
        objects.created(OpenGlObjects.Kind.FRAMEBUFFER, framebuffer, label + FRAMEBUFFER_SUFFIX);
        OpenGlErrors.check("readback " + label);
        return new OpenGlReadback(objects, framebuffer, maxBytes, slots);
    }

    @Override
    protected void copy(Slot slot, Texture source, int mip, int x, int y, int width, int height) {
        OpenGlTexture texture = (OpenGlTexture) source;
        Format format = texture.format();
        boolean depth = format == Format.D32_FLOAT;
        int attachment = depth ? GL30C.GL_DEPTH_ATTACHMENT : GL30C.GL_COLOR_ATTACHMENT0;
        OpenGlErrors.clear();
        GameHandles.Bindings game = GameHandles.bindings();
        GameHandles.bindFramebuffer(framebuffer);
        GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, attachment, GL11C.GL_TEXTURE_2D, texture.id(), mip);
        GL11C.glReadBuffer(depth ? GL11C.GL_NONE : GL30C.GL_COLOR_ATTACHMENT0);
        PackState before = PackState.now();
        GL15C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, slot.buffer);
        GL11C.glPixelStorei(GL11C.GL_PACK_ROW_LENGTH, width);
        GL11C.glPixelStorei(GL11C.GL_PACK_ALIGNMENT, OpenGlTypes.pixelAlignment(format));
        GL11C.glPixelStorei(GL11C.GL_PACK_SKIP_PIXELS, NO_SKIP);
        GL11C.glPixelStorei(GL11C.GL_PACK_SKIP_ROWS, NO_SKIP);
        GL11C.glReadPixels(x, y, width, height, OpenGlTypes.externalFormat(format), OpenGlTypes.componentType(format),
                START);
        before.restore();
        GL30C.glFramebufferTexture2D(GL30C.GL_FRAMEBUFFER, attachment, GL11C.GL_TEXTURE_2D, NO_TEXTURE, BASE_MIP);
        GameHandles.restore(game);
        slot.fence = GL32C.glFenceSync(GL32C.GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
        OpenGlErrors.check("readback of " + width + "x" + height + " " + format + " at mip " + mip);
    }

    @Override
    protected boolean signalled(Slot slot) {
        if (slot.fence == NO_FENCE) {
            return false;
        }

        int state = GL32C.glClientWaitSync(slot.fence, GL32C.GL_SYNC_FLUSH_COMMANDS_BIT, NO_WAIT);
        return state == GL32C.GL_ALREADY_SIGNALED || state == GL32C.GL_CONDITION_SATISFIED;
    }

    @Override
    protected void map(Slot slot, long bytes, Consumer<ByteBuffer> texels) {
        PackState before = PackState.now();
        GL15C.glBindBuffer(GL21C.GL_PIXEL_PACK_BUFFER, slot.buffer);
        try {
            ByteBuffer view = GL30C.glMapBufferRange(GL21C.GL_PIXEL_PACK_BUFFER, START, bytes, GL30C.GL_MAP_READ_BIT);
            if (view == null) {
                throw new IllegalStateException("The readback buffer " + slot.buffer + " could not be mapped");
            }

            try {
                texels.accept(view);
            } finally {
                GL15C.glUnmapBuffer(GL21C.GL_PIXEL_PACK_BUFFER);
            }
        } finally {
            before.restore();
        }
    }

    @Override
    protected void forget(Slot slot) {
        if (slot.fence != NO_FENCE) {
            GL32C.glDeleteSync(slot.fence);
            slot.fence = NO_FENCE;
        }
    }

    @Override
    protected void destroy(Slot slot) {
        GL15C.glDeleteBuffers(slot.buffer);
        objects.deleted(OpenGlObjects.Kind.BUFFER);
    }

    @Override
    protected void closed() {
        GameHandles.deleteFramebuffer(framebuffer);
        objects.deleted(OpenGlObjects.Kind.FRAMEBUFFER);
    }
}
