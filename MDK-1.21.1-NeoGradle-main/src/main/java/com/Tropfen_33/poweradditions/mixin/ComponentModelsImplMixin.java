package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.board.BurntComponents;
import com.Tropfen_33.poweradditions.client.LedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import org.patryk3211.powergrid.circuits.components.ComponentModels;
import org.patryk3211.powergrid.circuits.components.forge.ComponentModelsImpl;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Burnt components have no broken model yet, so for now they simply disappear from the board. */
@Mixin(value = ComponentModelsImpl.class, remap = false)
public abstract class ComponentModelsImplMixin {
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private static void poweradditions$hideBurnt(PlacedComponent placed, CallbackInfoReturnable<BakedModel> cir) {
        if (BurntComponents.isBurnt(ClientSideAccess.world(), placed)) {
            cir.setReturnValue(Minecraft.getInstance().getModelManager().getModel(ComponentModels.modelId(LedClient.EMPTY_MODEL)));
        }
    }
}
