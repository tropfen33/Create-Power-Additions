package com.Tropfen_33.powerledsnadditions.client;

import com.Tropfen_33.powerledsnadditions.PowerLeds;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

/** Must be class-loaded (init()) on the client before models get registered. */
public final class LedPartialModels {
    private LedPartialModels() {}

    public static final PartialModel DOME = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(PowerLeds.MOD_ID, "component/led_dome"));
    public static final PartialModel GLOW = PartialModel.of(
            ResourceLocation.fromNamespaceAndPath(PowerLeds.MOD_ID, "component/led_glow"));

    public static void init() {}
}
