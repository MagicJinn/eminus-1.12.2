package com.eminus.render.port;

/**
 * Client environment checks for far LOD. Vetus uses this to refuse far rendering when external shader
 * packs are active.
 */
public interface FarLayerEnvironment {
    boolean externalShadersActive();

    default String externalShaderLabel() {
        return "external shaders";
    }
}