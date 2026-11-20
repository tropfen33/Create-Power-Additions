package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.board.BurntComponents;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardRenderer;
import org.patryk3211.powergrid.circuits.components.IRenderedComponent;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Dynamic parts (glow, glass, ...) of burnt components are not drawn either. */
@Mixin(value = CircuitBoardRenderer.class, remap = false)
public abstract class CircuitBoardRendererMixin {
    @Redirect(
            method = "renderSafe(Lorg/patryk3211/powergrid/circuits/circuitboard/CircuitBoardBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", target = "Lorg/patryk3211/powergrid/circuits/components/IRenderedComponent;render(Lorg/patryk3211/powergrid/circuits/circuitboard/CircuitBoardBlockEntity;Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"))
    private void poweradditions$skipBurnt(IRenderedComponent component, CircuitBoardBlockEntity be, PlacedComponent placed,
                                     float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        if (BurntComponents.isBurnt(be, placed)) return;
        component.render(be, placed, partialTicks, ms, buffer, light, overlay);
    }
}
