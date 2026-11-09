package com.eminus.mixin;

import java.util.BitSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.eminus.client.session.ClientSession;
import com.eminus.ingest.IngestTrigger;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacketData;
import net.minecraft.world.level.chunk.LevelChunk;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
    @Shadow
    private ClientLevel level;

    @Inject(method = "enableChunkLight", at = @At("RETURN"))
    private void eminus$ingestLitChunk(LevelChunk chunk, int x, int z, CallbackInfo callback) {
        ClientSession.submitChunk(chunk, IngestTrigger.PACKET);
    }

    @Inject(method = "handleChunksBiomes", at = @At("RETURN"))
    private void eminus$ingestReplacedBiomes(ClientboundChunksBiomesPacket packet, CallbackInfo callback) {
        ClientSession.biomesReplaced(level, packet.chunkBiomeData().stream()
                .map(ClientboundChunksBiomesPacket.ChunkBiomeData::pos).toList());
    }

    @Inject(method = "applyLightData", at = @At("RETURN"))
    private void eminus$markLightPacket(int x, int z, ClientboundLightUpdatePacketData lightData,
            boolean lightUpdatePacket, CallbackInfo callback) {
        if (!lightUpdatePacket) {
            return;
        }

        BitSet sections = new BitSet();
        sections.or(lightData.getSkyYMask());
        sections.or(lightData.getEmptySkyYMask());
        sections.or(lightData.getBlockYMask());
        sections.or(lightData.getEmptyBlockYMask());
        ClientSession.lightPacketApplied(level, x, z, sections);
    }
}
