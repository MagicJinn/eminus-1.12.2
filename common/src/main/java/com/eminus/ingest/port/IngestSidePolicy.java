package com.eminus.ingest.port;

/**
 * Documents how loaders must wire chunk column ingest. Eminus Vetus is a client LOD mod.
 *
 * <p><b>Remote multiplayer (mod on client only, no integrated server):</b> ingest exclusively from
 * chunks the client receives and loads. Use client chunk cache or packet listener hooks. Do not apply
 * mixins to chunk generation or population on the logical server.
 *
 * <p><b>Integrated server (singleplayer or LAN host):</b> the same client receive path is sufficient
 * for far LOD on the hosting machine. Optional logical-server chunk generation hooks may exist only
 * when a server-capable artifact is installed and only on server-dist classes registered on the
 * dedicated server bus, not on a pure client connection to a remote world.
 *
 * <p><b>Dedicated server:</b> only when the mod is deliberately installed on the server jar. That is
 * outside the default client-only distribution.
 */
public final class IngestSidePolicy {
    private IngestSidePolicy() {
    }
}