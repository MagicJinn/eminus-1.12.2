package com.eminus.vetus.mixin;

import com.eminus.vetus.client.VetusClientSession;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Production far hook: after opaque terrain (render world pass 0), invoke upstream far draw via vetus session.
 */
@Mixin(EntityRenderer.class)
public class EntityRendererFarMixin {
    @Inject(
            method = "renderWorldPass",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/BlockRenderLayer;DFLnet/minecraft/entity/Entity;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    private void eminusVetus$afterOpaqueTerrain(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        if (pass == 0) {
            VetusClientSession.drawFarLayer();
        }
    }
}