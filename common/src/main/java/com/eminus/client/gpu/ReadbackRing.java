package com.eminus.client.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import com.eminus.gpu.Format;
import com.eminus.gpu.buffer.Readback;
import com.eminus.gpu.texture.Texture;

public abstract class ReadbackRing<S> implements Readback {
    private final int maxBytes;
    private final List<S> slots;
    private final Deque<S> free;
    private final Deque<Pending<S>> pending = new ArrayDeque<>();
    private long nextSequence;
    private boolean closed;

    private record Pending<S>(S slot, long sequence, int width, int height, Format format) {
        long bytes() {
            return (long) width * height * format.bytesPerTexel();
        }
    }

    protected ReadbackRing(int maxBytes, List<S> slots) {
        this.maxBytes = maxBytes;
        this.slots = List.copyOf(slots);
        this.free = new ArrayDeque<>(slots);
    }

    @Override
    public final long request(Texture source, int mip, int x, int y, int width, int height) {
        long bytes = (long) width * height * source.format().bytesPerTexel();
        if (bytes > maxBytes) {
            throw new IllegalArgumentException("A read of " + width + "x" + height + " " + source.format()
                    + " texels takes " + bytes + " bytes, past the " + maxBytes + " a slot holds");
        }

        S slot = free.pollFirst();
        if (slot == null) {
            return REFUSED;
        }

        try {
            copy(slot, source, mip, x, y, width, height);
        } catch (RuntimeException refused) {
            free.addFirst(slot);
            throw refused;
        }
        long sequence = nextSequence++;
        pending.addLast(new Pending<>(slot, sequence, width, height, source.format()));
        return sequence;
    }

    @Override
    public final boolean poll(Consumer<Read> reader) {
        Pending<S> newest = null;
        while (!pending.isEmpty() && signalled(pending.peekFirst().slot())) {
            if (newest != null) {
                release(newest.slot());
            }
            newest = pending.pollFirst();
        }

        if (newest == null) {
            return false;
        }

        Pending<S> read = newest;
        try {
            map(read.slot(), read.bytes(), texels -> reader.accept(new Read(read.sequence(), read.width(),
                    read.height(), read.format(), texels.duplicate().order(ByteOrder.nativeOrder()))));
        } finally {
            release(read.slot());
        }
        return true;
    }

    @Override
    public final void close() {
        if (closed) {
            return;
        }

        closed = true;
        for (Pending<S> each : pending) {
            forget(each.slot());
        }
        pending.clear();
        free.clear();
        slots.forEach(this::destroy);
        closed();
    }

    private void release(S slot) {
        forget(slot);
        free.addLast(slot);
    }

    protected abstract void copy(S slot, Texture source, int mip, int x, int y, int width, int height);

    protected abstract boolean signalled(S slot);

    protected abstract void map(S slot, long bytes, Consumer<ByteBuffer> texels);

    protected abstract void forget(S slot);

    protected abstract void destroy(S slot);

    protected void closed() {
    }
}
