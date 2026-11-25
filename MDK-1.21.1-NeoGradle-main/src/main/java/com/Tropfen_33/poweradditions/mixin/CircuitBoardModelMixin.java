package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.board.BurntComponents;
import org.patryk3211.powergrid.circuits.components.forge.CircuitBoardModel;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Powergrid darkens a component whose "destroyed" flag is set (colour 64/64/64 in the board mesh). That flag is
 * copied from the thermal unit's overheated state, and the thermal units are replaced by fresh ones whenever the
 * circuit is re-baked, e.g. because ANOTHER component got replaced. All other burnt components then lost their dark
 * tint and looked brighter. A burnt component (this mod's own burnt list) now always counts as destroyed here.
 */
@Mixin(value = CircuitBoardModel.class, remap = false)
public abstract class CircuitBoardModelMixin {
    @Redirect(
            method = "getQuads(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/util/RandomSource;Lnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;",
            at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
                    target = "Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;destroyed:Z"))
    private boolean poweradditions$keepBurntDark(PlacedComponent placed) {
        return placed.destroyed || BurntComponents.isBurnt(ClientSideAccess.world(), placed);
    }
}
