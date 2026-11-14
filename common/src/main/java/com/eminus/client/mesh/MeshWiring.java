package com.eminus.client.mesh;

import com.eminus.cell.CellStates;
import com.eminus.cell.StateTable;
import com.eminus.mesh.PlaceholderMeshModels;
import com.eminus.client.model.ClientBakery;
import com.eminus.client.render.far.FarClientBridge;
import com.eminus.client.render.far.ModernFarClientBridge;
import com.eminus.compat.sodium.SodiumMixinPlugin;
import com.eminus.mesh.BakeryModels;
import com.eminus.mesh.BakeryTints;
import com.eminus.mesh.CornerRule;
import com.eminus.mesh.MeshBlades;
import com.eminus.mesh.MeshListener;
import com.eminus.mesh.MeshModels;
import com.eminus.mesh.MeshOcclusion;
import com.eminus.mesh.MeshService;
import com.eminus.model.ModelIndex;
import com.eminus.platform.Platforms;
import com.eminus.session.DimensionRuntime;
import com.eminus.session.EminusInstance;
import com.eminus.settings.CornerOcclusion;
import com.eminus.settings.GrassAndFlowers;
import com.eminus.settings.SettingsService;

import net.minecraft.client.Minecraft;

public final class MeshWiring {
    public static MeshService service(FarClientBridge bridge, DimensionRuntime runtime, EminusInstance instance,
            ClientBakery baking, MeshListener listener) {
        return new MeshService(instance.build(), runtime.cells(), runtime.coverage(), runtime.frame(),
                models(runtime, baking), new BakeryTints(baking.colors(), runtime.biomes()), bridge.biomeBlendRadius(),
                baking.opacity(runtime.states()),
                occlusion(SettingsService.get().settings().lodQuality().cornerOcclusion()),
                blades(baking.grassAndFlowers()), listener, EminusInstance.URGENT_BUILD_WEIGHT);
    }

    public static MeshService service(Minecraft client, DimensionRuntime runtime, EminusInstance instance,
            ClientBakery baking, MeshListener listener) {
        return service(new ModernFarClientBridge(client), runtime, instance, baking, listener);
    }

    public static MeshBlades blades(GrassAndFlowers grassAndFlowers) {
        return grassAndFlowers == GrassAndFlowers.SIMPLIFIED ? MeshBlades.ONE : MeshBlades.BOTH;
    }

    public static MeshModels models(DimensionRuntime runtime, ClientBakery baking) {
        CellStates states = runtime.states();
        if (states instanceof StateTable table && baking.bakery() instanceof com.eminus.model.ModelBakery bakery) {
            return new BakeryModels(new ModelIndex(table, bakery), bakery);
        }
        return new PlaceholderMeshModels(baking.bakery());
    }

    public static MeshOcclusion occlusion(CornerOcclusion occlusion) {
        MeshOcclusion levels = switch (occlusion) {
            case EVERY_LEVEL -> MeshOcclusion.EVERY_LEVEL;
            case FINEST_LEVEL -> MeshOcclusion.FINEST_LEVEL;
            case OFF -> MeshOcclusion.NONE;
        };
        return levels.withRule(cornerRule()).withWater(SodiumMixinPlugin.sodiumPresent());
    }

    public static CornerRule cornerRule() {
        return SodiumMixinPlugin.sodiumPresent() || Platforms.get().enhancedBlockLighting()
                ? CornerRule.OWN_SIDE
                : CornerRule.VANILLA;
    }

    private MeshWiring() {
    }
}
