package com.eminus.vetus.mixin;

import com.eminus.vetus.ingest.VetusIngestHooks;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Ingest after client chunk storage is filled by {@code Chunk.read}, not on empty {@code ChunkEvent.Load}.
 */
@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {
    @Inject(
            method = "handleChunkData",
            at = @At("TAIL"),
            remap = false)
    private void eminusVetus$afterChunkData(SPacketChunkData packet, CallbackInfo ci) {
        VetusIngestHooks.onChunkDataApplied(packet.getChunkX(), packet.getChunkZ());
    }
}