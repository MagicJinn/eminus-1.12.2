package com.eminus.vetus.client;

import com.eminus.render.port.FarLayerEnvironment;

/**
 * Detects OptiFine and legacy Shaders Mod shader packs without a hard OptiFine dependency.
 */
public final class OptiFineShaderEnvironment implements FarLayerEnvironment {
    private static final String OPTIFINE_SHADERS = "net.optifine.shaders.Shaders";
    private static final String SHADERS_MOD_CORE = "shadersmodcore.client.Shaders";

    @Override
    public boolean externalShadersActive() {
        return optifinePackLoaded() || shadersModActive();
    }

    @Override
    public String externalShaderLabel() {
        if (optifinePackLoaded()) {
            return "OptiFine shaders";
        }
        if (shadersModActive()) {
            return "Shaders Mod";
        }
        return "external shaders";
    }

    private static boolean optifinePackLoaded() {
        try {
            Class<?> shaders = Class.forName(OPTIFINE_SHADERS);
            Object loaded = shaders.getMethod("isShaderPackLoaded").invoke(null);
            return loaded instanceof Boolean value && value;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean shadersModActive() {
        try {
            Class<?> core = Class.forName(SHADERS_MOD_CORE);
            Object loaded = core.getMethod("isShaderPackLoaded").invoke(null);
            return loaded instanceof Boolean value && value;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }
}