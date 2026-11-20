package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.board.BoardRepair;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Disables Powergrid's "repair the whole board" (sneak-right-click). Components have to be replaced one by one. */
@Mixin(value = CircuitBoardBlockEntity.class, remap = false)
public abstract class CircuitBoardBlockEntityCommonMixin {
    @Inject(method = "repairBroken", at = @At("HEAD"), cancellable = true)
    private void poweradditions$noCompleteRepair(CallbackInfo ci) {
        if (!BoardRepair.allowRebake) ci.cancel();
    }
}
