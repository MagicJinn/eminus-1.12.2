package com.eminus.client.render.far;

import java.util.EnumSet;
import java.util.Set;

import com.eminus.Eminus;
import com.eminus.gpu.Format;
import com.eminus.gpu.Gpu;
import com.eminus.gpu.Location;
import com.eminus.gpu.pass.Pass;
import com.eminus.gpu.pass.PassSpec;
import com.eminus.gpu.pipeline.Binding;
import com.eminus.gpu.pipeline.Pipeline;
import com.eminus.gpu.pipeline.PipelineSpec;
import com.eminus.gpu.texture.Sampler;
import com.eminus.gpu.texture.Texture;
import com.eminus.gpu.texture.TextureUsage;
import com.eminus.render.backend.DepthConvention;

import org.jspecify.annotations.Nullable;

public final class OcclusionPyramid implements AutoCloseable {
    public static final int BLOCK = 4;
    public static final int READ_BLOCK = BLOCK * BLOCK;
    public static final Format FORMAT = Format.R32_FLOAT;

    private static final Location PIPELINE = new Location(Eminus.MODID, "far_pyramid");
    private static final Location FRAGMENT_SHADER = new Location(Eminus.MODID, "core/far_pyramid");
    private static final String SOURCE = "Source";
    private static final String COARSE_LABEL = "eminus-pyramid-coarse";
    private static final String READ_LABEL = "eminus-pyramid-read";
    private static final Set<TextureUsage> USAGE = EnumSet.of(TextureUsage.ATTACHMENT, TextureUsage.SAMPLED,
            TextureUsage.COPY_SRC);
    private static final int MIPS = 1;
    private static final int VERTICES = 3;

    private final Gpu gpu;
    private final Pipeline pipeline;
    private @Nullable Texture coarse;
    private @Nullable Texture read;

    private OcclusionPyramid(Gpu gpu, Pipeline pipeline) {
        this.gpu = gpu;
        this.pipeline = pipeline;
    }

    static OcclusionPyramid create(Gpu gpu, DepthConvention depth) {
        return new OcclusionPyramid(gpu, gpu.pipeline(depth.define(
                        PipelineSpec.builder(PIPELINE, CompositePass.FULL_SCREEN_SHADER, FRAGMENT_SHADER)
                                .withBinding(Binding.sampled(SOURCE))
                                .withDefine("BLOCK", BLOCK))
                .withColorTarget(FORMAT, null, true)
                .build()));
    }

    Pipeline pipeline() {
        return pipeline;
    }

    public @Nullable Texture readLevel() {
        return read;
    }

    void build(Texture farDepth) {
        gpu.assertRenderThread();
        Texture coarseLevel = level(coarse, COARSE_LABEL, Math.ceilDiv(farDepth.width(), BLOCK),
                Math.ceilDiv(farDepth.height(), BLOCK));
        coarse = coarseLevel;
        Texture readLevel = level(read, READ_LABEL, Math.ceilDiv(farDepth.width(), READ_BLOCK),
                Math.ceilDiv(farDepth.height(), READ_BLOCK));
        read = readLevel;
        reduce(COARSE_LABEL, farDepth, coarseLevel);
        reduce(READ_LABEL, coarseLevel, readLevel);
    }

    private Texture level(@Nullable Texture held, String label, int width, int height) {
        if (held != null && held.width() == width && held.height() == height) {
            return held;
        }

        if (held != null) {
            held.close();
        }
        return gpu.texture(label, USAGE, FORMAT, width, height, MIPS);
    }

    private void reduce(String label, Texture source, Texture target) {
        try (Pass pass = gpu.pass(PassSpec.of(label, target, null))) {
            pass.pipeline(pipeline);
            pass.bind(SOURCE, source, Sampler.NEAREST);
            pass.draw(VERTICES);
        }
    }

    @Override
    public void close() {
        if (coarse != null) {
            coarse.close();
            coarse = null;
        }
        if (read != null) {
            read.close();
            read = null;
        }
    }
}
