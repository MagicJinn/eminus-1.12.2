package com.eminus.vetus.client;

import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.vetus.EminusVetus;
import com.eminus.vetus.ingest.VetusIngestPolicy;

import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.event.world.ChunkEvent;

/**
 * Client hooks for chunk ingest and far LOD. Render hook is a stub until {@code FarRenderer} exists on 1.12.2.
 *
 * <p>Ingest is client-bus only. On remote multiplayer ingest uses client world chunk load (packets into
 * {@code WorldClient}). No server chunk generation hooks register from the client-only jar.
 */
@Mod.EventBusSubscriber(modid = EminusVetus.MODID, value = Side.CLIENT)
public final class VetusClientEvents {
    private static boolean renderLogged;
    private static boolean ingestPathLogged;

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getWorld().isRemote == false) {
            return;
        }

        Minecraft client = Minecraft.getMinecraft();
        if (!ingestPathLogged) {
            ingestPathLogged = true;
            if (VetusIngestPolicy.forbidServerChunkgenHooks(client)) {
                EminusVetus.LOGGER.debug(
                        "Ingest uses client chunk receive path only (remote server, no chunkgen hooks)");
            } else {
                EminusVetus.LOGGER.debug(
                        "Ingest uses client chunk receive path (integrated server, no chunkgen hooks in client jar)");
            }
        }

        Chunk chunk = event.getChunk();
        int dimension = event.getWorld().provider.getDimension();
        ColumnRef column = new ColumnRef(chunk.x, chunk.z, dimension);
        VetusClientState.chunkIngest().onColumnAvailable(column, IngestTrigger.PACKET);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getWorld().isRemote == false) {
            return;
        }

        Chunk chunk = event.getChunk();
        int dimension = event.getWorld().provider.getDimension();
        VetusClientState.chunkIngest().onColumnUnavailable(new ColumnRef(chunk.x, chunk.z, dimension));
    }

    /**
     * Stub far pass. Production hook should move into {@code EntityRenderer#renderWorldPass} after opaque terrain
     * (Mixin at the end of pass 0). {@code RenderWorldLastEvent} stays coarse but needs no Mixin bootstrap yet.
     */
    @SubscribeEvent
    public static void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!VetusClientState.farLayerAllowed()) {
            return;
        }

        if (Minecraft.getMinecraft().world == null) {
            return;
        }

        if (!renderLogged) {
            renderLogged = true;
            EminusVetus.LOGGER.info("Eminus Vetus far LOD render hook placeholder (RenderWorldLastEvent)");
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        VetusClientState.farLayerAllowed();
    }

    private VetusClientEvents() {
    }
}