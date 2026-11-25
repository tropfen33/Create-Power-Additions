package com.Tropfen_33.poweradditions;

import com.Tropfen_33.poweradditions.board.BoardRepair;
import com.Tropfen_33.poweradditions.network.BurntSync;
import com.Tropfen_33.poweradditions.client.LedClient;
import com.Tropfen_33.poweradditions.client.LedPartialModels;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(PowerAdditions.MOD_ID)
public class PowerAdditions {
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();
    public static final String MOD_ID = "poweradditions";

    public PowerAdditions(IEventBus modBus) {
        LedRegistries.register(modBus);
        NeoForge.EVENT_BUS.addListener(BoardRepair::onRightClick);
        BurntSync.register(modBus);
        NeoForge.EVENT_BUS.addListener(BurntSync::onChunkWatch);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LedPartialModels.init();
            LedClient.init(modBus);
        }
    }
}
