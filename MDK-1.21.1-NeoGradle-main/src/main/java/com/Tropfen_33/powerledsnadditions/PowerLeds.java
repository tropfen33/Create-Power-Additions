package com.Tropfen_33.powerledsnadditions;

import com.Tropfen_33.powerledsnadditions.board.BoardRepair;
import com.Tropfen_33.powerledsnadditions.client.LedClient;
import com.Tropfen_33.powerledsnadditions.client.LedPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(PowerLeds.MOD_ID)
public class PowerLeds {
    public static final String MOD_ID = "power_leds_nadditions";

    public PowerLeds(IEventBus modBus) {
        LedRegistries.register(modBus);
        NeoForge.EVENT_BUS.addListener(BoardRepair::onRightClick);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LedPartialModels.init();
            LedClient.init(modBus);
        }
    }
}
