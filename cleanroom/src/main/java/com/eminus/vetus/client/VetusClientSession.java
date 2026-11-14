package com.eminus.vetus.client;

import java.nio.file.Path;
import java.util.Objects;

import com.eminus.client.gpu.Gpus;
import com.eminus.client.gpu.RenderThreadGuard;
import com.eminus.client.render.far.FarRenderer;
import com.eminus.client.render.far.FarStart;
import com.eminus.gpu.Gpu;
import com.eminus.ingest.IngestTrigger;
import com.eminus.ingest.SectionPyramid;
import com.eminus.ingest.port.ColumnRef;
import com.eminus.model.PackBlocks;
import com.eminus.session.DimensionRuntime;
import com.eminus.session.EminusInstance;
import com.eminus.session.SessionCells;
import com.eminus.session.StoreFolders;
import com.eminus.session.WorldIdentity;
import com.eminus.settings.Settings;
import com.eminus.settings.SettingsService;
import com.eminus.vetus.EminusVetus;
import com.eminus.client.render.far.VetusFarClientBridge;
import com.eminus.vetus.platform.VetusPlatform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraftforge.fml.common.Loader;

public final class VetusClientSession {
  private static final String IN_SESSION = "reached only inside a session";
  private static final String WITH_RUNTIME = "a renderer needs a dimension runtime";

  private static boolean renderHookLogged;
  private static boolean sessionStarted;
  private static EminusInstance instance;
  private static DimensionRuntime runtime;
  private static FarRenderer renderer;
  private static String refusal;
  private static int lastDimension = Integer.MIN_VALUE;
  private static String lastWorldKey = "";
  private static int sectionHandoffs;

  private VetusClientSession() {}

  public static void ingestSection(
      ColumnRef column, int sectionIndex, SectionPyramid pyramid, IngestTrigger trigger) {
    ensureSession();
    DimensionRuntime active = runtimeFor(column.dimensionId());
    if (active == null || instance == null) {
      return;
    }
    // CellMerger/CellCache require an eminus worker thread, same as modern
    // IngestService.
    int chunkX = column.chunkX();
    int chunkZ = column.chunkZ();
    instance
        .ingest()
        .enqueue(scratch -> active.merger().merge(pyramid, chunkX, sectionIndex, chunkZ));
    sectionHandoffs++;
    if (sectionHandoffs == 1 || sectionHandoffs % 64 == 0) {
      long samples = pyramid.level(com.eminus.cell.DetailLevel.MIN).length;
      EminusVetus.LOGGER.debug(
          "Vetus queued section {} of column {},{} dim {} ({} voxels, trigger {}, total {})",
          sectionIndex,
          chunkX,
          chunkZ,
          column.dimensionId(),
          samples,
          trigger,
          sectionHandoffs);
    }
  }

  public static void dropColumn(ColumnRef column) {
    if (instance == null) {
      return;
    }
    EminusVetus.LOGGER.debug(
        "Vetus dropped column {},{} dim {}", column.chunkX(), column.chunkZ(), column.dimensionId());
  }

  public static void drawFarLayer() {
    if (!VetusClientState.farLayerAllowed()) {
      return;
    }
    Minecraft minecraft = Minecraft.getMinecraft();
    if (minecraft.world == null) {
      return;
    }
    ensureSession();
    swapDimensionIfNeeded(minecraft);
    if (renderer == null) {
      startRenderer(minecraft, null);
    }
    if (!renderHookLogged) {
      renderHookLogged = true;
      if (renderer != null) {
        EminusVetus.LOGGER.info("Vetus far render pass active with upstream FarRenderer");
      } else if (refusal != null) {
        EminusVetus.LOGGER.warn("Vetus far render pass skipped: {}", refusal);
      }
    }
    try (VetusGlStateGuard ignored = VetusGlStateGuard.capture()) {
      if (renderer != null) {
        renderer.newFrame();
        renderer.frame();
      }
    }
  }

  private static void ensureSession() {
    if (sessionStarted) {
      return;
    }
    sessionStarted = true;
    VetusPlatform.install();
    SettingsService.set(SettingsService.load(PlatformsSafe.configDir()));
    RenderThreadGuard.install(
        () -> {
          if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("Eminus far rendering must run on the client thread");
          }
        });
    Settings settings = SettingsService.get().settings();
    instance =
        EminusInstance.start(
            storeBase(),
            settings.workerThreads(),
            settings.lodQuality().storedLevel(),
            System::currentTimeMillis,
            SessionCells.DICTIONARY);
    EminusVetus.LOGGER.info("Vetus client session started with dictionary cells");
  }

  private static void swapDimensionIfNeeded(Minecraft minecraft) {
    WorldClient world = minecraft.world;
    if (world == null || instance == null) {
      return;
    }
    int dimension = world.provider.getDimension();
    String worldKey = worldKey(minecraft);
    if (dimension == lastDimension && worldKey.equals(lastWorldKey) && runtime != null) {
      return;
    }
    lastDimension = dimension;
    lastWorldKey = worldKey;
    Gpu gpu = Gpus.create();
    long arenaBytes = renderer == null ? 0L : renderer.arenaBytes();
    stopRenderer();
    releaseRuntime();
    runtime =
        instance.acquire(
            new WorldIdentity(worldKey, world.getSeed(), Integer.toString(dimension)), 0);
    startRenderer(minecraft, gpu);
    if (arenaBytes > 0L && renderer != null) {
      // arena reuse is handled inside FarStart via replacedArenaBytes on the modern path
    }
  }

  private static DimensionRuntime runtimeFor(int dimensionId) {
    Minecraft minecraft = Minecraft.getMinecraft();
    if (minecraft.world == null || instance == null) {
      return null;
    }
    if (runtime == null || minecraft.world.provider.getDimension() != dimensionId) {
      swapDimensionIfNeeded(minecraft);
    }
    return runtime;
  }

  private static void startRenderer(Minecraft minecraft, Gpu existingGpu) {
    if (instance == null || runtime == null) {
      return;
    }
    Settings settings = SettingsService.get().settings();
    Gpu gpu = existingGpu != null ? existingGpu : Gpus.create();
    FarStart start =
        FarStart.start(
            new VetusFarClientBridge(minecraft),
            gpu,
            Objects.requireNonNull(instance, IN_SESSION),
            Objects.requireNonNull(runtime, WITH_RUNTIME),
            256,
            settings,
            0L,
            PackBlocks.NONE);
    renderer = start.renderer();
    refusal = start.refusal();
    if (refusal != null) {
      EminusVetus.LOGGER.warn("Far renderer refused on vetus: {}", refusal);
    }
  }

  private static void stopRenderer() {
    refusal = null;
    if (renderer != null) {
      renderer.close();
      renderer = null;
    }
  }

  private static void releaseRuntime() {
    if (runtime != null && instance != null) {
      instance.release(runtime);
      runtime = null;
    }
  }

  private static Path storeBase() {
    Minecraft minecraft = Minecraft.getMinecraft();
    Path gameDir = Loader.instance().getConfigDir().toPath().getParent();
    if (minecraft.isIntegratedServerRunning()) {
      return StoreFolders.singleplayerBase(
          gameDir.resolve("saves").resolve(minecraft.getIntegratedServer().getWorldName()));
    }
    String address =
        minecraft.getCurrentServerData() != null ? minecraft.getCurrentServerData().serverIP : "offline";
    return StoreFolders.multiplayerBase(gameDir, address);
  }

  private static String worldKey(Minecraft minecraft) {
    if (minecraft.isIntegratedServerRunning()) {
      return minecraft.getIntegratedServer().getWorldName();
    }
    if (minecraft.getCurrentServerData() != null) {
      return minecraft.getCurrentServerData().serverIP;
    }
    return "unknown";
  }

  /** Avoids importing {@link com.eminus.platform.Platforms} before {@link VetusPlatform#install()}. */
  private static final class PlatformsSafe {
    private PlatformsSafe() {}

    static Path configDir() {
      return Loader.instance().getConfigDir().toPath();
    }
  }
}