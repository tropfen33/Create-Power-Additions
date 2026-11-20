package com.Tropfen_33.poweradditions.board;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.patryk3211.powergrid.circuits.circuitboard.BakedCircuit;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;

import java.util.HashSet;
import java.util.Set;

/**
 * Burnt-out components can only be replaced one by one: right-click the burnt component with a new one of the
 * same kind. Powergrid's "repair the whole board" is disabled (see CircuitBoardBlockEntityCommonMixin).
 */
public final class BoardRepair {
    private BoardRepair() {}

    /** Set while this mod calls Powergrid's re-bake, so the mixin lets it through. */
    public static boolean allowRebake = false;

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;
        BlockEntity entity = level.getBlockEntity(event.getPos());
        if (!(entity instanceof CircuitBoardBlockEntity board)) return;
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (held.isEmpty()) return;

        PlacedComponent target = BoardPicking.pick(board, event.getHitVec().getLocation(), player.getLookAngle());
        if (target == null || !BurntComponents.isBurnt(board, target)) return;
        if (held.getItem() != ComponentRegistry.getItem(target.component)) return;

        // remember the OTHER burnt units, re-bake the circuit (resets all units), then burn those again
        Set<String> stillBurnt = new HashSet<>();
        BakedCircuit before = board.getBaked();
        if (before != null) {
            for (ThermalUnit unit : before.thermalUnits) {
                if (unit.hasOverheated() && !target.uuid.equals(unit.getId())) stillBurnt.add(unit.getKey());
            }
        }
        allowRebake = true;
        try {
            board.repairBroken();
        } finally {
            allowRebake = false;
        }
        BakedCircuit after = board.getBaked();
        if (after != null) {
            for (ThermalUnit unit : after.thermalUnits) {
                if (stillBurnt.contains(unit.getKey())) unit.setTemperature(unit.getOverheatTemperature() + 5f);
            }
        }
        if (!player.isCreative()) held.shrink(1);
        board.setChanged();
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.world.InteractionResult.SUCCESS);
    }
}
