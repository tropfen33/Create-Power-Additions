package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.PowerAdditions;
import com.Tropfen_33.poweradditions.board.BurntComponents;
import com.Tropfen_33.poweradditions.board.IBurntBoard;
import com.Tropfen_33.poweradditions.network.BurntSync;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.circuits.circuitboard.BakedCircuit;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server side: remembers which components overheated (so the state is saved and synced to clients) and keeps the
 * heat-source wires of every burnt component cut off. That is also how a single component stays burnt when the
 * circuit is re-baked to repair another one - without touching any temperatures, so no new sparks.
 */
@Mixin(value = BakedCircuit.class, remap = false)
public abstract class BakedCircuitMixin {
    @Shadow @Final private CircuitBoardBlockEntity be;
    @Shadow @Final public List<ThermalUnit> thermalUnits;

    @Inject(method = "tick", at = @At("RETURN"))
    private void poweradditions$trackBurnt(CallbackInfo ci) {
        Level level = be.getLevel();
        if (level == null || level.isClientSide) return;
        IBurntBoard board = (IBurntBoard) (Object) be;

        boolean anyOverheated = false;
        for (ThermalUnit unit : thermalUnits) {
            if (unit.hasOverheated()) { anyOverheated = true; break; }
        }
        if (!anyOverheated && board.poweradditions$burntSet().isEmpty()) return;

        // thermal units know their component's UUID; the burnt list uses position + type keys
        Map<UUID, Integer> keys = new HashMap<>();
        be.getComponentsStream().forEach(placed -> keys.put(placed.uuid, BurntComponents.key(placed)));

        boolean changed = false;
        for (ThermalUnit unit : thermalUnits) {
            Integer key = keys.get(unit.getId());
            if (key == null) continue;
            if (unit.hasOverheated() && !board.poweradditions$isBurnt(key)) {
                board.poweradditions$setBurnt(key, true);
                changed = true;
                PowerAdditions.LOGGER.info("[PowerAdditions] component {} on board {} burnt out", key, be.getBlockPos());
            }
            if (board.poweradditions$isBurnt(key)) {
                for (AbstractElectricWire wire : ((ThermalUnitAccessor) unit).poweradditions$getHeatSources()) {
                    if (wire.getNetwork() != null) wire.remove();
                }
            }
        }
        if (changed) {
            be.setChanged();
            BurntSync.send(be);
        }
    }
}
