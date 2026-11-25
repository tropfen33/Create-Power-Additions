package com.Tropfen_33.poweradditions.board;

import com.Tropfen_33.poweradditions.LedRegistries;
import com.Tropfen_33.poweradditions.components.AbstractLedComponent;
import com.Tropfen_33.poweradditions.network.BurntSync;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.schematic.CircuitSchematic;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

/**
 * Two ways to deal with a burnt-out component (Powergrid's "repair the whole board" is disabled, see
 * CircuitBoardBlockEntityCommonMixin):
 *  - REPLACE: right-click it with a new one of the same kind. Only that component is taken out of the burnt
 *    list; the circuit is re-baked and every other burnt component is cut off again by BakedCircuitMixin
 *    (no temperatures are touched, so no new sparks).
 *  - REMOVE: sneak + right-click takes it off the board completely and hands you the burnt-out item.
 */
public final class BoardRepair {
    private BoardRepair() {}

    /** Kept for the repairBroken mixin: Powergrid's own complete repair stays blocked. */
    public static boolean allowRebake = false;

    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;
        BlockEntity entity = level.getBlockEntity(event.getPos());
        if (!(entity instanceof CircuitBoardBlockEntity board)) return;
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();

        PlacedComponent target = BoardPicking.pick(board, player.getEyePosition(), player.getLookAngle(), event.getHitVec().getLocation());
        if (target == null) return;
        boolean ledBurnt = target.component instanceof AbstractLedComponent led && led.anyBroken(target);
        boolean burnt = BurntComponents.isBurnt(board, target);

        if (player.isShiftKeyDown() && (burnt || ledBurnt)) {
            remove(board, target, player, ledBurnt);
            finish(event);
            return;
        }

        if (held.isEmpty() || !burnt) return;
        if (held.getItem() != ComponentRegistry.getItem(target.component)) return;
        replace(board, target, player, held);
        finish(event);
    }

    private static void finish(PlayerInteractEvent.RightClickBlock event) {
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void replace(CircuitBoardBlockEntity board, PlacedComponent target, Player player, ItemStack held) {
        IBurntBoard burnt = (IBurntBoard) (Object) board;
        burnt.poweradditions$setBurnt(BurntComponents.key(target), false);
        burnt.poweradditions$rebake();
        BurntSync.send(board);

        if (!player.isCreative()) held.shrink(1);
        give(player, burntItem(target, false));
        board.setChanged();
    }

    /** Takes the burnt component off the board completely. The traces stay, only the component goes. */
    private static void remove(CircuitBoardBlockEntity board, PlacedComponent target, Player player, boolean led) {
        ItemStack burntItem = burntItem(target, led);
        CircuitSchematic schematic = new CircuitSchematic(board.getSchematic());
        schematic.removeComponents(target.x, target.y, 1, 1);
        ((IBurntBoard) (Object) board).poweradditions$setBurnt(BurntComponents.key(target), false);
        board.setSchematic(schematic);   // re-bakes the circuit and syncs the new schematic
        BurntSync.send(board);
        give(player, burntItem);
        board.setChanged();
    }

    private static ItemStack burntItem(PlacedComponent target, boolean led) {
        if (target.component instanceof AbstractLedComponent ledComponent) return ledComponent.burntItem(target);
        var item = LedRegistries.BROKEN_COMPONENT_ITEMS.get(ComponentRegistry.getId(target.component).getPath());
        return item == null ? ItemStack.EMPTY : new ItemStack(item.get());
    }

    private static void give(Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!player.getInventory().add(stack)) player.drop(stack, false);
    }
}
