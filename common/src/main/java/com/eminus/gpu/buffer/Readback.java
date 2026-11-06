package com.eminus.gpu.buffer;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import com.eminus.gpu.Format;
import com.eminus.gpu.texture.Texture;

public interface Readback extends AutoCloseable {
    int SLOTS = 3;
    long REFUSED = -1L;

    long request(Texture source, int mip, int x, int y, int width, int height);

    boolean poll(Consumer<Read> reader);

    @Override
    void close();

    record Read(long sequence, int width, int height, Format format, ByteBuffer texels) {
    }
}
