package com.eminus.client.gpu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.eminus.gpu.Format;
import com.eminus.gpu.buffer.Readback;
import com.eminus.gpu.texture.Texture;

import org.junit.jupiter.api.Test;

class ReadbackRingTest {
    private static final int SLOT_BYTES = 64;
    private static final int SIDE = 2;

    private record FakeTexture(Format format) implements Texture {
        @Override
        public int width() {
            return SIDE;
        }

        @Override
        public int height() {
            return SIDE;
        }

        @Override
        public void close() {
        }
    }

    private static final class Slot {
        private final int id;
        private boolean fenced;
        private boolean done;

        private Slot(int id) {
            this.id = id;
        }
    }

    private static final class FakeRing extends ReadbackRing<Slot> {
        private final List<Slot> slots;
        private final Set<Integer> destroyed = new HashSet<>();
        private boolean closedHook;

        private FakeRing(List<Slot> slots) {
            super(SLOT_BYTES, slots);
            this.slots = slots;
        }

        static FakeRing create() {
            List<Slot> slots = new ArrayList<>();
            for (int id = 0; id < Readback.SLOTS; id++) {
                slots.add(new Slot(id));
            }
            return new FakeRing(slots);
        }

        @Override
        protected void copy(Slot slot, Texture source, int mip, int x, int y, int width, int height) {
            slot.fenced = true;
            slot.done = false;
        }

        @Override
        protected boolean signalled(Slot slot) {
            return slot.done;
        }

        @Override
        protected void map(Slot slot, long bytes, Consumer<ByteBuffer> texels) {
            ByteBuffer data = ByteBuffer.allocate((int) bytes);
            data.put(0, (byte) slot.id);
            texels.accept(data);
        }

        @Override
        protected void forget(Slot slot) {
            slot.fenced = false;
        }

        @Override
        protected void destroy(Slot slot) {
            destroyed.add(slot.id);
        }

        @Override
        protected void closed() {
            closedHook = true;
        }

        void finishAll() {
            slots.forEach(slot -> slot.done = slot.fenced);
        }
    }

    private static final Texture TEXTURE = new FakeTexture(Format.R32_UINT);

    @Test
    void aRequestPastEveryFreeSlotIsRefusedAndSequencesCount() {
        FakeRing ring = FakeRing.create();

        assertEquals(0L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
        assertEquals(1L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
        assertEquals(2L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
        assertEquals(Readback.REFUSED, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
    }

    @Test
    void nothingIsReadBeforeItsFenceSignals() {
        FakeRing ring = FakeRing.create();
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);

        assertFalse(ring.poll(read -> { }));
    }

    @Test
    void aPollHandsOverTheNewestReadAndFreesEveryOlderSlot() {
        FakeRing ring = FakeRing.create();
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);
        ring.finishAll();
        List<Readback.Read> reads = new ArrayList<>();

        assertTrue(ring.poll(reads::add));
        assertEquals(1, reads.size());
        assertEquals(2L, reads.getFirst().sequence());
        assertEquals(SIDE * SIDE * Format.R32_UINT.bytesPerTexel(), reads.getFirst().texels().remaining());
        assertFalse(ring.poll(read -> { }));
        assertEquals(3L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
        assertEquals(4L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
        assertEquals(5L, ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE));
    }

    @Test
    void aSignalledReadWaitsBehindAnOlderOneStillInFlight() {
        FakeRing ring = FakeRing.create();
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);
        List<Readback.Read> reads = new ArrayList<>();
        ring.finishAll();
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);

        assertTrue(ring.poll(reads::add));
        assertEquals(1L, reads.getFirst().sequence());
        assertFalse(ring.poll(reads::add));
    }

    @Test
    void aReadLargerThanASlotIsNamed() {
        FakeRing ring = FakeRing.create();
        Texture wide = new FakeTexture(Format.RGBA32_FLOAT);

        assertThrows(IllegalArgumentException.class, () -> ring.request(wide, 0, 0, 0, SIDE * SIDE, SIDE));
    }

    @Test
    void closingForgetsWhatIsInFlightAndDestroysEverySlotOnce() {
        FakeRing ring = FakeRing.create();
        ring.request(TEXTURE, 0, 0, 0, SIDE, SIDE);

        ring.close();
        ring.close();

        assertEquals(Set.of(0, 1, 2), ring.destroyed);
        assertTrue(ring.closedHook);
        assertTrue(ring.slots.stream().noneMatch(slot -> slot.fenced));
    }
}
