package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.BrokenComponents;
import com.Tropfen_33.poweradditions.PowerAdditions;
import com.Tropfen_33.poweradditions.board.BurntComponents;
import com.Tropfen_33.poweradditions.client.LedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.circuits.components.ComponentModels;
import org.patryk3211.powergrid.circuits.components.forge.ComponentModelsImpl;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Burnt components show their burnt-out model; components without one simply disappear. */
@Mixin(value = ComponentModelsImpl.class, remap = false)
public abstract class ComponentModelsImplMixin {
    @Inject(method = "getModel", at = @At("HEAD"), cancellable = true)
    private static void poweradditions$hideBurnt(PlacedComponent placed, CallbackInfoReturnable<BakedModel> cir) {
        if (BurntComponents.isBurnt(ClientSideAccess.world(), placed)) {
            ResourceLocation id = placed.component.getModelId(placed);
            ResourceLocation target = BrokenComponents.hasModel(id.getNamespace(), id.getPath())
                    ? ResourceLocation.fromNamespaceAndPath(PowerAdditions.MOD_ID, id.getPath() + "_broken")
                    : LedClient.EMPTY_MODEL;
            cir.setReturnValue(Minecraft.getInstance().getModelManager().getModel(ComponentModels.modelId(target)));
        }
    }
}
