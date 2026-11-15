package com.Tropfen_33.powerleds;

import com.Tropfen_33.powerleds.client.LedPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(PowerLeds.MOD_ID)
public class PowerLeds {
    public static final String MOD_ID = "power_leds";

    public PowerLeds(IEventBus modBus) {
        LedRegistries.register(modBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LedPartialModels.init();
        }
    }
}
