package com.Tropfen_33.poweradditions.mixin;

import com.Tropfen_33.poweradditions.board.BoardRepair;
import com.Tropfen_33.poweradditions.board.BurntComponents;
import com.Tropfen_33.poweradditions.board.IBurntBoard;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.IInteractableComponent;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Burnt-component bookkeeping on the circuit board (saved + synced), the selection outline for burnt components,
 * and the removal of Powergrid's "repair the whole board".
 */
@Mixin(value = CircuitBoardBlockEntity.class, remap = false)
public abstract class CircuitBoardBlockEntityCommonMixin implements IBurntBoard {
    @Unique private final Set<Integer> poweradditions$burnt = new HashSet<>();

    @Shadow protected abstract void bakeCircuit();

    // ---------------- IBurntBoard ----------------
    @Override public boolean poweradditions$isBurnt(int key) { return poweradditions$burnt.contains(key); }

    @Override public void poweradditions$setBurnt(int key, boolean burnt) {
        if (burnt) poweradditions$burnt.add(key); else poweradditions$burnt.remove(key);
    }

    @Override public Set<Integer> poweradditions$burntSet() { return poweradditions$burnt; }

    @Override public void poweradditions$rebake() {
        bakeCircuit();
        ((CircuitBoardBlockEntity) (Object) this).notifyUpdate();
    }

    // ---------------- save / sync (the same methods Create uses for disk and client packets) ----------------
    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("RETURN"))
    private void poweradditions$write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        tag.putIntArray("PowerAdditionsBurntKeys", poweradditions$burnt.stream().mapToInt(Integer::intValue).toArray());
    }

    // HEAD: the set must be known before Powergrid bakes the circuit inside read()
    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Z)V", at = @At("HEAD"))
    private void poweradditions$readHead(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket, CallbackInfo ci) {
        // only replace the list when the tag actually carries it (some sync packets do not)
        if (tag.contains("PowerAdditionsBurntKeys", Tag.TAG_INT_ARRAY)) {
            poweradditions$burnt.clear();
            for (int key : tag.getIntArray("PowerAdditionsBurntKeys")) poweradditions$burnt.add(key);
        }
    }

    // ---------------- selection outline: burnt components are outlined like the LEDs ----------------
    @Inject(method = "getShape(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/phys/shapes/VoxelShape;)Lnet/minecraft/world/phys/shapes/VoxelShape;",
            at = @At("RETURN"), cancellable = true)
    private void poweradditions$burntShape(BlockState state, VoxelShape base, CallbackInfoReturnable<VoxelShape> cir) {
        CircuitBoardBlockEntity self = (CircuitBoardBlockEntity) (Object) this;
        float ax = CircuitBoardBlock.getAngleX(state);
        float ay = CircuitBoardBlock.getAngleY(state);
        VoxelShape result = cir.getReturnValue();
        for (PlacedComponent placed : (Iterable<PlacedComponent>) self.getComponentsStream()::iterator) {
            // interactable components (switches, LEDs, ...) already contribute their own shape
            if (placed.component instanceof IInteractableComponent || !BurntComponents.isBurnt(self, placed)) continue;
            result = Shapes.or(result, CircuitBoardBlock.rotate(IInteractableComponent.extrudedFootprint(placed, 0.25f), ax, ay));
        }
        cir.setReturnValue(result);
    }

    // ---------------- no complete repair ----------------
    @Inject(method = "repairBroken", at = @At("HEAD"), cancellable = true)
    private void poweradditions$noCompleteRepair(CallbackInfo ci) {
        if (!BoardRepair.allowRebake) ci.cancel();
    }
}
