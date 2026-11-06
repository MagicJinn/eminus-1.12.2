package com.eminus.client.gpu.game;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.eminus.client.gpu.ReadbackRing;
import com.eminus.gpu.buffer.Readback;
import com.eminus.gpu.texture.Texture;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.GpuFence;
import com.mojang.renderpearl.api.device.GpuDevice;

import org.jspecify.annotations.Nullable;

final class GameReadback extends ReadbackRing<GameReadback.Slot> {
    private static final long NO_WAIT = 0L;
    private static final long START = 0L;

    private final GpuDevice device;

    static final class Slot {
        private final GpuBuffer buffer;
        private @Nullable GpuFence fence;

        private Slot(GpuBuffer buffer) {
            this.buffer = buffer;
        }
    }

    private GameReadback(GpuDevice device, int maxBytes, List<Slot> slots) {
        super(maxBytes, slots);
        this.device = device;
    }

    static GameReadback create(GpuDevice device, String label, int maxBytes) {
        List<Slot> slots = new ArrayList<>();
        for (int slot = 0; slot < Readback.SLOTS; slot++) {
            String name = label + "-" + slot;
            slots.add(new Slot(device.createBuffer(() -> name, GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST,
                    maxBytes)));
        }
        return new GameReadback(device, maxBytes, slots);
    }

    @Override
    protected void copy(Slot slot, Texture source, int mip, int x, int y, int width, int height) {
        CommandEncoder encoder = device.createCommandEncoder();
        encoder.copyTextureToBuffer(((GameTexture) source).texture(), slot.buffer, START, GameReadback::copied, mip, x, y, width,
                height);
        slot.fence = encoder.createFence();
    }

    @Override
    protected boolean signalled(Slot slot) {
        GpuFence fence = slot.fence;
        return fence != null && fence.awaitCompletion(NO_WAIT);
    }

    @Override
    protected void map(Slot slot, long bytes, Consumer<ByteBuffer> texels) {
        try (GpuBufferSlice.MappedView view = slot.buffer.map(START, bytes, true, false)) {
            texels.accept(view.data());
        }
    }

    @Override
    protected void forget(Slot slot) {
        GpuFence fence = slot.fence;
        if (fence != null) {
            fence.close();
            slot.fence = null;
        }
    }

    @Override
    protected void destroy(Slot slot) {
        slot.buffer.close();
    }

    private static void copied() {
    }
}
