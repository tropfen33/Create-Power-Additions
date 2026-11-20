package com.Tropfen_33.powerledsnadditions.client;

import com.Tropfen_33.powerledsnadditions.PowerLeds;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.patryk3211.powergrid.circuits.components.ComponentModels;

public final class LedClient {
    private LedClient() {}

    /** Model with no geometry, used in place of burnt components. */
    public static final ResourceLocation EMPTY_MODEL = ResourceLocation.fromNamespaceAndPath(PowerLeds.MOD_ID, "empty");

    public static void init(IEventBus modBus) {
        modBus.addListener(ModelEvent.RegisterAdditional.class,
                event -> event.register(ComponentModels.modelId(EMPTY_MODEL)));
    }
}
