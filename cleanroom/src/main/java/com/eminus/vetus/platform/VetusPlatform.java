package com.eminus.vetus.platform;

import java.nio.file.Path;

import com.eminus.platform.Platform;
import com.eminus.platform.Platforms;
import com.eminus.vetus.EminusVetus;

import net.minecraftforge.fml.common.Loader;

public final class VetusPlatform implements Platform {
    public static void install() {
        Platforms.set(new VetusPlatform());
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public String modVersion() {
        return EminusVetus.VERSION;
    }

    @Override
    public String loaderName() {
        return "cleanroom";
    }

    @Override
    public String loaderVersion() {
        return Loader.instance().getMinecraftModContainer().getVersion();
    }

    @Override
    public Path configDir() {
        return Loader.instance().getConfigDir().toPath();
    }

    @Override
    public boolean enhancedBlockLighting() {
        return false;
    }
}