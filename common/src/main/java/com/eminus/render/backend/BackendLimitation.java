package com.eminus.render.backend;

import com.eminus.render.arena.ArenaSizing;

public enum BackendLimitation {
    DRAW_INDIRECT("the device cannot draw from an indirect buffer"),
    FRAGMENT_DEPTH("the fragment stage cannot write depth"),
    DEPTH_TARGET("no depth target format is accepted"),
    ARENA_MEMORY("the memory allocation limit is below the arena"),
    ARENA_MINIMUM("the arena would be below its " + ArenaSizing.MIN_BYTES / (1024 * 1024) + " MiB minimum");

    private final String reason;

    BackendLimitation(String reason) {
        this.reason = reason;
    }

    public String reason() {
        return reason;
    }
}
