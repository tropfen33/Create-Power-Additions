package com.Tropfen_33.powerledsnadditions.board;

import com.Tropfen_33.powerledsnadditions.components.AbstractLedComponent;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.circuits.circuitboard.BakedCircuit;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;

/** "Burnt out" = one of the component's thermal units has overheated. LEDs have their own burnt state. */
public final class BurntComponents {
    private BurntComponents() {}

    public static boolean isBurnt(CircuitBoardBlockEntity be, PlacedComponent placed) {
        if (placed.component instanceof AbstractLedComponent) return false;
        BakedCircuit baked = be.getBaked();
        if (baked == null) return false;
        for (ThermalUnit unit : baked.thermalUnits) {
            if (placed.uuid.equals(unit.getId()) && unit.hasOverheated()) return true;
        }
        return false;
    }

    public static boolean isBurnt(Level level, PlacedComponent placed) {
        if (level == null) return false;
        return level.getBlockEntity(placed.getPos()) instanceof CircuitBoardBlockEntity be && isBurnt(be, placed);
    }
}
