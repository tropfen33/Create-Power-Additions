package com.Tropfen_33.poweradditions.board;

import java.util.Set;

/**
 * Implemented by Powergrid's CircuitBoardBlockEntity through a mixin: which components of a board are burnt out.
 * Components are identified by BurntComponents.key(...) (position + type), NOT by their UUID: Powergrid's synced
 * schematic carries no UUIDs, so the client invents new ones on every rebuild.
 */
public interface IBurntBoard {
    boolean poweradditions$isBurnt(int componentKey);

    void poweradditions$setBurnt(int componentKey, boolean burnt);

    Set<Integer> poweradditions$burntSet();

    /** Re-bakes the circuit (fresh wires) and syncs it; burnt components are cut off again by the next tick. */
    void poweradditions$rebake();
}
