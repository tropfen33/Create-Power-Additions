package com.Tropfen_33.poweradditions.mixin;

import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;

@Mixin(value = ThermalUnit.class, remap = false)
public interface ThermalUnitAccessor {
    @Accessor("heatSources")
    Collection<AbstractElectricWire> poweradditions$getHeatSources();
}
