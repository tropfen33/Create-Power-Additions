package com.Tropfen_33.poweradditions.board;

import com.Tropfen_33.poweradditions.components.AbstractLedComponent;
import com.Tropfen_33.poweradditions.network.ClientBurnt;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.patryk3211.powergrid.circuits.circuitboard.BakedCircuit;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;

import java.util.List;

/**
 * "Burnt out" = the component is in the board's burnt list. The server owns the list (saved with the board) and
 * pushes it to clients with its own packet; on the client the synced temperature is used as a fallback.
 * LEDs keep their own burnt state.
 */
public final class BurntComponents {
    private BurntComponents() {}

    /**
     * Identifies a component on its board: position + type. The same on server and client (UUIDs are not: the
     * synced schematic has none, so the client generates new ones whenever it rebuilds the board).
     */
    public static int key(PlacedComponent placed) {
        return 31 * (31 * placed.x + placed.y) + ComponentRegistry.getId(placed.component).hashCode();
    }

    public static boolean isBurnt(CircuitBoardBlockEntity be, PlacedComponent placed) {
        if (placed.component instanceof AbstractLedComponent) return false;
        int key = key(placed);
        Level level = be.getLevel();
        if (level != null && level.isClientSide) {
            if (ClientBurnt.isBurnt(level, be.getBlockPos(), key)) return true;
            BakedCircuit baked = be.getBaked();
            if (baked != null) {
                for (ThermalUnit unit : baked.thermalUnits) {
                    // unit ids are this client's own component ids, so they match within the client
                    if (placed.uuid.equals(unit.getId())
                            && (unit.hasOverheated() || unit.getTemperature() >= unit.getOverheatTemperature())) {
                        ClientBurnt.remember(level, be.getBlockPos(), key);
                        return true;
                    }
                }
            }
            return false;
        }
        return ((IBurntBoard) (Object) be).poweradditions$isBurnt(key);
    }

    public static boolean isBurnt(Level level, PlacedComponent placed) {
        if (level == null) return false;
        return level.getBlockEntity(placed.getPos()) instanceof CircuitBoardBlockEntity be && isBurnt(be, placed);
    }

    /** Goggle lines for a burnt component: how to replace it and how to remove it. */
    public static void addRepairHints(List<Component> tooltip, Component itemName) {
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("goggles.poweradditions.replace", itemName)).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("    ")
                .append(Component.translatable("goggles.poweradditions.remove")).withStyle(ChatFormatting.YELLOW));
    }
}
