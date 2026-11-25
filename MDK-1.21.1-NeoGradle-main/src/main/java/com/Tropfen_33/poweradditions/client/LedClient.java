package com.Tropfen_33.poweradditions.client;

import com.Tropfen_33.poweradditions.BrokenComponents;
import com.Tropfen_33.poweradditions.PowerAdditions;
import com.Tropfen_33.poweradditions.network.ClientBurnt;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.patryk3211.powergrid.circuits.components.ComponentModels;

public final class LedClient {
    private LedClient() {}

    /** Model with no geometry, used in place of burnt components. */
    public static final ResourceLocation EMPTY_MODEL = ResourceLocation.fromNamespaceAndPath(PowerAdditions.MOD_ID, "empty");

    public static void init(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> ClientBurnt.clear());
        modBus.addListener(ModelEvent.RegisterAdditional.class, event -> {
            event.register(ComponentModels.modelId(EMPTY_MODEL));
            for (String name : BrokenComponents.MODEL_NAMES) {
                event.register(ComponentModels.modelId(
                        ResourceLocation.fromNamespaceAndPath(PowerAdditions.MOD_ID, name + "_broken")));
            }
        });
    }
}
