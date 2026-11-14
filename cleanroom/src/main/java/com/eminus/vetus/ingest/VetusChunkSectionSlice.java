package com.eminus.vetus.ingest;

import com.eminus.ingest.port.SectionSlice;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Adapts a 1.12.2 {@link ExtendedBlockStorage} column slice into upstream {@link SectionSlice}.
 */
public final class VetusChunkSectionSlice implements SectionSlice {
    private final ExtendedBlockStorage section;
    private final int sectionBaseY;
    private final Chunk chunk;

    public VetusChunkSectionSlice(Chunk chunk, ExtendedBlockStorage section, int sectionBaseY) {
        this.chunk = chunk;
        this.section = section;
        this.sectionBaseY = sectionBaseY;
    }

    @Override
    public Object blockStateKey(int x, int y, int z) {
        if (section == null || section.isEmpty()) {
            return "minecraft:air";
        }
        IBlockState state = section.get(x, y, z);
        if (state.getBlock() == net.minecraft.init.Blocks.AIR) {
            return "minecraft:air";
        }
        int meta = state.getBlock().getMetaFromState(state);
        net.minecraft.util.ResourceLocation name = state.getBlock().getRegistryName();
        return (name != null ? name.toString() : "unknown") + "|" + meta;
    }

    @Override
    public int skyLight(int x, int y, int z) {
        int worldY = sectionBaseY + y;
        return chunk.getLightFor(net.minecraft.world.EnumSkyBlock.SKY, new net.minecraft.util.math.BlockPos(
                (chunk.x << 4) + x, worldY, (chunk.z << 4) + z));
    }

    @Override
    public int blockLight(int x, int y, int z) {
        int worldY = sectionBaseY + y;
        return chunk.getLightFor(net.minecraft.world.EnumSkyBlock.BLOCK, new net.minecraft.util.math.BlockPos(
                (chunk.x << 4) + x, worldY, (chunk.z << 4) + z));
    }

    @Override
    public String biomeKey(int x, int y, int z) {
        int worldX = (chunk.x << 4) + x;
        int worldZ = (chunk.z << 4) + z;
        net.minecraft.world.biome.Biome biome = chunk.getBiome(new net.minecraft.util.math.BlockPos(worldX, 0, worldZ),
                chunk.getWorld().provider.getBiomeProvider());
        if (biome == null) {
            return "minecraft:plains";
        }
        return biome.getRegistryName() != null ? biome.getRegistryName().toString() : "minecraft:plains";
    }

}