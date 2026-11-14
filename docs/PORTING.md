# Eminus Vetus porting map (Cleanroom 1.12.2)

Working name **Eminus Vetus**. Unofficial port of [Eminus](https://github.com/anyttng/eminus) under the license in `LICENSE` (commit `b6ffaa6` on `dev`). Do not publish builds or change license text from this document alone.

This file is groundwork for Marijn's fork (`MagicJinn/eminus-1.12.2`). It describes the modern `dev` tree as of the vetus groundwork branch, not a finished 1.12.2 mod.

## Repository layout (modern)

| Module | Role |
|--------|------|
| `common` | Shared logic, mixins, assets. Compiled against NeoForm 26.3 (Mojmap vanilla, no loader). |
| `fabric` | Fabric Loader entry and jar naming. |
| `neoforge` | NeoForge entry and merged shipping jar via root `modJar`. |
| `core` | **New.** Subset of `common` that compiles on JDK 25 with no Minecraft on the classpath (see below). |
| `cleanroom` | **New.** RetroFuturaGradle 2.0.2, Minecraft 1.12.2, Java 25 bytecode. Stub client mod today. |

Counts below are `common/src/main/java` only (**420** `.java` files), unless noted.

### Minecraft-independent core (conceptual)

These areas are intended to stay free of `net.minecraft`, loader APIs, and optional mod APIs:

| Package / area | Files (main) | Notes |
|----------------|-------------|--------|
| `com.eminus.gpu` | 27 | `Gpu`, buffers, textures, pipelines, passes, `Std140`. Pure Java plus JOML. |
| `com.eminus.store` | 9 | SQLite cell store, zstd records, `SaveService`. Depends on `CellKey`, `ColumnCoverage`, `cell.cache` at runtime. |
| `com.eminus.render` | 47 | Tree, arena, far draw logic. **`GroupFacing`** uses **`Face`**. **`QuadGroups`** still uses modern **`Direction`** for mesh grouping. |
| `com.eminus.mesh` | 43 | Packed `Quad` format, greedy merge, mesh queues. **29** files have no Minecraft import. **14** tie to `Direction`, `BlockState`, or baking. |
| `com.eminus.geom` | 1 | **`Face`**: axis faces with vanilla 3D index order. Used by **`CellKey`**, **`FaceMask`**, tree culling. |
| `com.eminus.cell` | 19 | Voxel cell encoding. **`CellKey`** and **`FaceMask`** use **`Face`**. **`StateTable`** still uses Brigadier and registry blocks. |
| `com.eminus.settings` | 12 | JSON config on disk (Gson). |
| `com.eminus.session` | 5 | Dimension runtime wiring. No direct Minecraft imports, but types reference **`IngestService`**, **`StateTable`**, **`BeamSet`** (Minecraft-linked). |
| `com.eminus.handoff` | 5 | Near/far section handoff. No imports, used from client mixins. |
| `com.eminus.work` | 5 | Background work queues. |
| `com.eminus.box` | 3 | API far-box registry. |
| `com.eminus.beam` | 8 | Beacon column storage. **5** without Minecraft, **3** with `BlockPos` / beacon types. |
| `com.eminus.ingest` | 10 | Chunk section to cell pipeline. **All 10** use modern chunk/light/biome APIs. |
| `com.eminus.model` | 41 | Baking and ports. **19** without Minecraft import (`model.port` abstractions, metadata). **22** use block/fluid/state/sprites. |
| `com.eminus.platform` | 2 | Loader-neutral `Platform` interface. |
| `com.eminus.api` | 6 | Public API. **`EminusApi`** calls `ClientSession` and `FarRenderer`. |

**Coarse split**

- **~267** main sources have no `net.minecraft`, Fabric, NeoForge, Iris, Sodium, or Vitrail import lines (static scan).
- **~150** main sources import `net.minecraft` or `com.mojang` (mixins, client, ingest, model baking, compat).
- **Plus** `common/src/main/java/com/eminus/mixin` (**13** files), all Mixin plus Minecraft.
- **Plus** `common/src/main/java/com/eminus/compat/{iris,sodium,vitrail}` (**50** files), optional mod integration.

Greedy meshing and packed quads live in `com.eminus.mesh` (`Quad`, `GreedyMerger`, `MeshBuffer`, and others). GPU upload paths live under `com.eminus.client.gpu` and `com.eminus.client.render.far` on modern.

### Minecraft-dependent surface (modern 26.3)

#### Mixins (`com.eminus.mixin`, 13 files)

| Mixin | Purpose | 1.12.2 / Cleanroom hook target |
|-------|---------|-------------------------------|
| `LevelRendererMixin` | Inject far LOD draws between solid and translucent terrain (`RenderPass`, `ChunkSectionsToRender`). | `RenderGlobal` / terrain pass ordering. No Render Pearl. Likely `IRenderTickable` or Mixin into terrain loop. |
| `GameRendererMixin` | Session lifecycle around rendering. | `EntityRenderer` / `GameRenderer` equivalent. |
| `MinecraftMixin` | Client tick and screen hooks. | `Minecraft` (same name, obf `bib`). |
| `ClientLevelMixin` | Block change notifications for ingest. | `WorldClient`. |
| `ClientChunkCacheMixin` | Section load and light updates. | `ChunkProviderClient`, 16x16x16 sections not 16x16x16 **sections** same idea, verify cube size. |
| `ClientPacketListenerMixin` | Biome and light packets. | `NetHandlerPlayClient`. |
| `BeaconBlockEntityMixin` | Beam data for far beacons. | `TileEntityBeacon`. |
| Accessors (`BiomeManager`, `PipelineCache`, `RenderSystem`, `SpriteContents`, `StairBlock`, `WeightedVariants`) | Reach into vanilla internals. | Re-map to MCP names and fields. Several targets **do not exist** on 1.12.2 (e.g. `RenderSystem`, weighted model variants). |

#### Client bridge (`com.eminus.client`, 105 files)

| Area | Main APIs (modern) | 1.12.2 needs |
|------|-------------------|--------------|
| `client.session` | `Minecraft`, `ClientLevel`, `LevelChunk`, `Identifier`, save paths | `Minecraft`, `WorldClient`, `Chunk`, dimension IDs as int or RC, `File` saves layout. |
| `client.model.game` | `BlockStateModel`, `WeightedVariants`, `BlockAndTintGetter`, `BuiltInRegistries`, fluids | `IBakedModel`, `BlockStateContainer`, `IBlockAccess`, `ForgeRegistries`, meta and damage. |
| `client.mesh` | `Minecraft`, `BlockStateParser` | Chunk reread, block state text or registry IDs. |
| `client.gpu.game` | Render Pearl `GpuDevice`, `CommandEncoder`, `GlStateManager` from Render Pearl | LWJGL 3 on Cleanroom: map to `GlStateManager` (Forge) or direct GL with **state restore** around vanilla. |
| `client.render.far` | `FarRenderer`, `RenderSystem`, main render target, Iris slots | Custom framebuffer pass before/after terrain. No Iris contract on 1.12.2 in v1. |
| `client.settings` | `OptionInstance`, `Screen`, `Component`, ModMenu on Fabric | Forge `GuiScreen`, `GameSettings`, optional Forge config GUI or in-game screen. |
| `client.frame` | `RenderTarget`, frame graph | `Framebuffer` / GlStateManager framebuffer binding. |

#### Ingest (`com.eminus.ingest`, 10 files)

Uses **`ClientLevel`**, **`LevelChunkSection`**, **`LevelLightEngine`**, **`DataLayer`**, **`BiomeManager`**, **`Holder<Biome>`**, section **`SectionPos`**.

**Loader-neutral contract (in `core`):**

- `com.eminus.ingest.port.ColumnRef` (chunk X/Z plus dimension id)
- `com.eminus.ingest.port.ChunkColumnSink` with `onColumnAvailable` / `onColumnUnavailable`
- Reuses `com.eminus.ingest.IngestTrigger` for why a column was submitted

**1.12.2 strategy (column-first):** ingest entire client columns so data survives render distance changes. The vetus adapter (`VetusChunkColumnSink`) logs today. Next step maps each `Chunk` into voxel columns (16x256x16 blocks per column on 1.12.2) without mimicking 1.18 section packets.

**Client-only ingest policy (required):**

| Scenario | What may run |
|----------|----------------|
| Mod on client, connected to **remote** server (no integrated server) | **Only** client chunk receive and load paths: chunks entering `WorldClient` after network packets or client chunk provider. **No** mixins into chunk generation, population, or server-only `ChunkEvent` handlers on the logical server. |
| Mod on client, **integrated server** (singleplayer or LAN host) | Same client receive path is enough for far LOD on the machine that plays. Optional server-side generation hooks are allowed **only** in a future server-capable artifact and **only** on the dedicated server event bus, not as a substitute on remote play. |
| Mod on **dedicated server** | Only if a separate server distribution is installed deliberately. Out of scope for the current `clientSideOnly` jar (`eminus_vetus` @Mod). |

Registration rules for vetus:

- `@Mod(clientSideOnly = true)` on `EminusVetus` so the default jar does not load on a remote dedicated server.
- `@Mod.EventBusSubscriber(..., Side.CLIENT)` only for ingest today (`VetusClientEvents`). Handlers return early when `!world.isRemote`.
- `VetusIngestPolicy.forbidServerChunkgenHooks` is true on remote multiplayer. Generation hook types must not register in that mode.
- Production packet-level hook (optional refinement): tail `NetHandlerPlayClient` chunk data handling if `ChunkEvent.Load` is insufficient, still client-only.

Core documents the contract in `com.eminus.ingest.port.IngestSidePolicy`.

**1.12.2 still needs:**

- Block, light, and biome sampling per column (not necessarily 16x16x16 **section** parity with modern).
- A 1.12 `SectionConverter` equivalent writing into `SectionPyramid` or a simplified column pyramid.

#### Model (`com.eminus.model`, 41 files)

`model.port` is the abstraction boundary (`BlockModels`, `ModelQuad`, `Sprite`, `TintSource`). Implement **`Game*`** adapters for 1.12.2 in new packages (e.g. `com.eminus.vetus.client.model.game`), do not copy other mods.

Modern baking uses **`BlockState`**, **`FluidState`**, **`RandomSource`**, **`Direction`**, stair/leaf special cases in `ModelBaker` and `StateTable`.

1.12.2 needs:

- Block state to model ID and quads from `BlockModelShapes` / `ModelLoader`.
- Texture atlas (`TextureMap`), sprite UVs, mip rules (`MipStrategy`).
- Tint: grass/foliage/water via biome and meta, match `BlockColors` / `ItemColors`.

#### Optional compat (50 files under `compat/`)

| Target | Files | 1.12.2 note |
|--------|-------|-------------|
| Iris | 35 | Defer or replace with OptiFine shader contract only if desired. |
| Sodium | 11 | No Sodium on 1.12.2. Optional performance mods differ. |
| Vitrail | 4 | Not applicable. |

Keep compat gates in `common` for upstream merges. Vetus should ship separate mixin configs that do not load Iris/Sodium gates.

### `core` module (extraction attempt)

Gradle project **`core`** reuses selected paths under `common/src/main/java` without NeoForm.

**Compiles today (JDK 25, no Minecraft):** `geom.Face`, `CellKey`, `FaceMask`, ingest port types, `FarLayerEnvironment`, plus the earlier GPU/settings/box slice. Verify with `./gradlew :core:compileJava :core:test`.

**Loader face adapters (not in `core`):**

| Module | Type | Role |
|--------|------|------|
| `fabric` / `neoforge` | `com.eminus.platform.minecraft.ModernFaces` | `Direction` ↔ `Face` |
| `cleanroom` | `com.eminus.vetus.geom.CleanroomFaces` | `EnumFacing` ↔ `Face` |

**Not yet in `core` without further refactors:** `store` (needs `ColumnCoverage`, cache), `render` tree (many types, still MC at mesh boundaries), `StateTable`, `OpenGlGpu`, full `GreedyMerger` graph.

**Upstream tweaks on this fork:** `Eminus.LOGGER` uses SLF4J. **`CellKey`**, **`FaceMask`**, **`TreeBorders`**, **`TreeChanges`**, **`GroupFacing`**, and targeted **`FacePasses`** call sites use **`Face`** instead of pulling `Direction` into cell keys.

## Gradle: Cleanroom module

- **Plugin:** `com.gtnewhorizons.retrofuturagradle` **2.0.2** (settings). Template upstream often cites **2.0.3** on [ForgeDevEnv](https://github.com/CleanroomMC/ForgeDevEnv).
- **Mappings:** MCP stable **39** (default template).
- **Minecraft:** **1.12.2**, Forge **14.23.5.2847** (from dev env).
- **Runtime target:** [Cleanroom **0.7.14-alpha**](https://github.com/CleanroomMC/Cleanroom/releases/tag/0.7.14-alpha) (Java **25**, LWJGL **3**). Newer Cleanroom alphas should work but this is the pinned reference for vetus.
- **JDK:** **Java 25** for the mod toolchain and for `core`. Root Fabric/NeoForge builds already require **Gradle on JDK 25** (Loom 1.19.x).
- **Inclusion:** subproject `cleanroom` is added when Gradle runs on **JDK 25+**, or pass **`-Penable_eminus_vetus=true`**. On older JDKs only `core` may be omitted from CI if desired.
- **Error Prone:** disabled for `:cleanroom` so patched Minecraft sources compile.
- **Bytecode:** `cleanroom/gradle.properties` sets `use_java25_bytecode=true` so the stub can depend on `:core` (Java 25). Cleanroom itself must load Java 25 class files, which matches the stated target.

**Verify locally**

```bash
export JAVA_HOME=/path/to/jdk-25
./gradlew :core:compileJava :core:test
./gradlew :cleanroom:compileJava
```

`runClient` for vetus is not wired in this PR (stub only). Expect to add Cleanroom run configs after game hooks exist.

### Vetus client hooks (stub)

| Piece | Location | Behavior |
|-------|----------|----------|
| Chunk ingest | `VetusClientEvents.onChunkLoad` (client bus, `world.isRemote`) | `ChunkColumnSink` with `IngestTrigger.PACKET`. No server chunkgen hooks in client-only jar |
| Chunk unload | `onChunkUnload` | `onColumnUnavailable` |
| Far render placeholder | `RenderWorldLastEvent` | No geometry. Logs once if shaders are off |
| Shader guard | `OptiFineShaderEnvironment` | Reflective OptiFine / Shaders Mod detection. Disables far LOD and logs a warning |
| Ingest sink | `VetusChunkColumnSink` | Debug logging until column voxel conversion exists |

### Render hook choice (1.12.2)

Modern code injects between solid and translucent terrain in `LevelRendererMixin`. On 1.12.2 the closest stable anchor is **`EntityRenderer.renderWorldPass`**:

- **Pass 0** is opaque terrain (`RenderGlobal.renderBlockLayer` opaque).
- Injecting **after pass 0** keeps far LOD behind translucent and weather, matching modern ordering closely.
- **`RenderWorldLastEvent`** is used for the stub because it needs no Mixin bootstrap yet. It runs later than ideal but is easy to maintain while Mixin config is still off in `cleanroom/gradle.properties`.

**Planned production hook:** Mixin (or coremod) tail of `EntityRenderer.renderWorldPass` when `pass == 0`, gated by `VetusClientState.farLayerAllowed()`.

### External shaders policy

Vetus does **not** support OptiFine shader packs (or Shaders Mod). When detected, far rendering stays off and the client logs a warning once. No crash, no mixin into shader internals.

## OpenGL backend (`OpenGlGpu`) on Cleanroom LWJGL 3

Implementation lives in `com.eminus.client.gpu.opengl` (modern binding layer to Render Pearl / Mojang `GlStateManager`).

### LWJGL / GL features used

| Feature | API | Core profile only? | 1.12.2 compatibility context |
|---------|-----|--------------------|------------------------------|
| VAO | `GL30C glGenVertexArrays`, `glBindVertexArray` | Yes (core) | Available on LWJGL 3 Cleanroom if GL 3.2+ context. Must **unbind VAO** (`bind 0`) after far passes so legacy terrain is not stuck on eminus VAO. |
| VBO / IBO | `GL15C` buffer calls | No | Fine. Keep EBO binding restored if vanilla expects it. |
| Sampler objects | `GL33C glGenSamplers` | Yes | Usually available. If missing, fall back to `glTexParameter` per unit (vetus decision). |
| FBO | `GL30C` framebuffer, color and depth attachments | Yes | Fine. Track bindings via restored `GlStateManager`. |
| UBO | `GL31C glBindBufferBase`, uniform blocks in GLSL | Yes | Requires GL 3.1+ shader paths. 1.12.2 uses legacy shaders for terrain, eminus uses its own GLSL (okay if shader compiler accepts 150/330). |
| Texture buffers / texelFetch | `GL31C GL_TEXTURE_BUFFER`, `glTexBuffer` | Yes | Used for material/indirection tables. |
| `glMapBufferRange`, persistent mapping | `GL30C`, `ARB_buffer_storage` | Optional ARB | Guard with capability flags like `Capabilities.persistentMapping()`. |
| `ARB_clip_control` zero-to-one depth | `OpenGlGpu.create()` | N/A | Modern main depth may be reversed. 1.12.2 typically **increasing** depth. Vetus must match `GameHandles.depthReversed()` to vanilla (likely false) or convert matrices. |
| Indirect draw | `GL_ARB_draw_indirect`, `multi_draw_indirect` | Often core 4.x | Used for far draws. Verify caps on player GPUs. Fallback: direct draws. |
| Blit depth | `glBlitFramebuffer` | Yes | Used when copying depth between targets. |

`OpenGlGpu.create()` calls **`RenderSystem.assertOnRenderThread()`** and reads **`ARB_clip_control`** and main target depth mode. Vetus needs **`VetusRenderGuard`** (render thread check) and **`VetusHandles`** instead of `GameHandles` (lightmap texture, main FBO color/depth from `Framebuffer` / entity renderer).

### State save and restore around vanilla

Far rendering runs **between** vanilla passes. On 1.12.2 fixed-function and shader coexistence:

1. Save and restore: current **FBO**, **viewport**, **scissor**, **active texture unit**, bound **2D textures** on units eminus touches (budget **12** units in `GameHandles.GAME_TRACKED_TEXTURE_UNITS`), **program**, **VAO**, **array buffer**, **element array buffer**, blend and depth enable/mask, polygon offset if used.
2. `OpenGlPass` binds the shared eminus VAO and UBOs. Always **`glBindVertexArray(0)`** at pass end (already done).
3. Prefer routing framebuffer/texture mutations through **`GlStateManager`** on Forge/Cleanroom where possible so subsequent terrain draws see consistent state.
4. Vanilla may leave fixed-function enabled. Eminus pipelines are fully shader based. Disable `GL_TEXTURE_2D`, `GL_LIGHTING`, `GL_ALPHA_TEST` only inside eminus passes if conflicts appear (regression test on Cleanroom).

**Not compatible without rewrites:** dependencies on **Render Pearl** (`RenderPass`, `GpuTextureView`, `CommandEncoder`) and modern-only **chunk section render lists**. Vetus far pass should call simplified draw paths first, then port indirect batching.

## Decisions (resolved on this pass)

| Topic | Choice |
|-------|--------|
| Face type | Internal `com.eminus.geom.Face`. Adapters on loaders only. |
| Ingest | Column-first on client chunk receive only when remote. Server chunkgen optional later on integrated or dedicated server dist only. |
| Render hook | Production: after opaque terrain in `EntityRenderer.renderWorldPass(0)`. Stub: `RenderWorldLastEvent`. |
| Shaders | Unsupported on vetus. Detect OptiFine / Shaders Mod and disable far LOD with a warning. |
| Core width | Widen only where upstream diff stays small (`Face`, cell keys, ports). |
| Cleanroom | **0.7.14-alpha** reference runtime. |

**Still open:** publish metadata, Mixin bootstrap version when production render inject lands, column voxel mapping details.

## Remaining work (technical scope, not calendar)

| Layer | Scope |
|-------|--------|
| Build / CI | JDK 25 on agents, optional `enable_eminus_vetus`, `runClient`, publish `eminus_vetus` jar separate from modern `modJar`. |
| Geometry / core | Pull `store`, `ColumnCoverage`, and greedy mesh into `core` after remaining `Direction` at mesh boundaries. |
| Chunk ingest | Implement 1.12 column to `SectionPyramid` (or alternate column store) behind `ChunkColumnSink`. |
| Models | Full `vetus.client.model.game` adapter set and atlas baking on 1.12.2. |
| GPU bridge | `VetusHandles`, depth convention, GL state guard, drop Render Pearl types. |
| Render hook | Replace `LevelRendererMixin` strategy on terrain pass, far `FarRenderer` equivalent. |
| Session / UI | `ClientSession` port, settings screen, config path on Forge. |
| Beacons / API | Beam columns, optional API surface without Fabric callbacks. |
| QA | Visual parity tests, memory, SQLite store on Windows/Linux, multiplayer server-not-required paths. |

Roughly **70%** of modern `common` main sources touch Minecraft or optional mods directly or through a one-hop dependency. The independent **`gpu`** and **`settings`** layers are the smallest integration risk. **`ingest`**, **`client.model.game`**, **`client.render.far`**, and **mixin render injection** are the largest.

## License reminder

Ship **Eminus Vetus** under the same PolyForm Shield terms and Required Notice. Use a distinct name, public source, and clear unofficial port labeling on distribution pages when publishing (out of scope for this internal groundwork PR).
