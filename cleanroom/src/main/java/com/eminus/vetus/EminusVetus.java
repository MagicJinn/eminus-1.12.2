package com.eminus.vetus;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cleanroom 1.12.2 entry for the unofficial Eminus Vetus port. Game hooks and rendering land in later milestones.
 */
@Mod(
        modid = EminusVetus.MODID,
        name = EminusVetus.NAME,
        version = EminusVetus.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        acceptableSaveVersions = "*",
        acceptedMinecraftVersions = "[1.12.2]"
)
public final class EminusVetus {
    public static final String MODID = "eminus_vetus";
    public static final String NAME = "Eminus Vetus";
    public static final String VERSION = Tags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    private EminusVetus() {
    }
}
