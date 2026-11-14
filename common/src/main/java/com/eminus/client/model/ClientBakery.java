package com.eminus.client.model;

import java.util.List;

import com.eminus.cell.CellStates;
import com.eminus.mesh.MeshOpacity;
import com.eminus.model.BiomeColors;
import com.eminus.model.ModelSource;
import com.eminus.model.PlaceholderModelSource;
import com.eminus.model.PackBlocks;
import com.eminus.model.port.VariantDraw;
import com.eminus.settings.GrassAndFlowers;
import com.eminus.settings.SettingsService;

public record ClientBakery(ModelSource bakery, BiomeColors colors, boolean seeThroughLeaves,
        GrassAndFlowers grassAndFlowers, VariantDraw variantDraw) {
    public MeshOpacity opacity(CellStates states) {
        return MeshOpacity.of(states, seeThroughLeaves);
    }

    public static ClientBakery placeholder(PackBlocks packBlocks) {
        PlaceholderModelSource bakery = new PlaceholderModelSource(packBlocks);
        BiomeColors colors = new BiomeColors(List.of());
        GrassAndFlowers grassAndFlowers = SettingsService.get().settings().lodQuality().grassAndFlowers();
        boolean seeThroughLeaves = SettingsService.get().settings().lodQuality().seeThroughLeaves();
        return new ClientBakery(bakery, colors, seeThroughLeaves, grassAndFlowers, VariantDraw.NEXT_INT);
    }

    public void stop() {
        if (bakery instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception failure) {
                throw new RuntimeException(failure);
            }
        } else if (bakery instanceof com.eminus.model.ModelBakery modelBakery) {
            modelBakery.stop();
        }
    }
}
