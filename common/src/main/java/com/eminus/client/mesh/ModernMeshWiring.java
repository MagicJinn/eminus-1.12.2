package com.eminus.client.mesh;

import com.eminus.client.model.ClientBakery;
import com.eminus.client.render.far.ModernFarClientBridge;
import com.eminus.mesh.MeshListener;
import com.eminus.mesh.MeshService;
import com.eminus.session.DimensionRuntime;
import com.eminus.session.EminusInstance;

import net.minecraft.client.Minecraft;

/** Modern loader entry that keeps {@link MeshWiring} free of {@link Minecraft}. */
public final class ModernMeshWiring {
    private ModernMeshWiring() {
    }

    public static MeshService service(Minecraft client, DimensionRuntime runtime, EminusInstance instance,
            ClientBakery baking, MeshListener listener) {
        return MeshWiring.service(new ModernFarClientBridge(client), runtime, instance, baking, listener);
    }
}