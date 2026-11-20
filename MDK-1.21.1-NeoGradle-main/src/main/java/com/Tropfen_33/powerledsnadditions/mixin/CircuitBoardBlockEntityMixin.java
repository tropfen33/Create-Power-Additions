package com.Tropfen_33.powerledsnadditions.mixin;

import com.Tropfen_33.powerledsnadditions.board.BoardPicking;
import com.Tropfen_33.powerledsnadditions.board.BurntComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import org.patryk3211.powergrid.circuits.circuitboard.BakedCircuit;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.components.IComponentGoggleInformation;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalUnit;
import org.patryk3211.powergrid.utility.ClientSideAccess;
import org.patryk3211.powergrid.utility.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * Client side additions to the circuit board's goggle tooltip:
 *  - Powergrid's "Damaged / This circuit board has damaged components" lines only show while sneaking and the
 *    "Sneak-right-click to repair" line is removed (complete repair is disabled).
 *  - Mini HUD for the component you look at: burnt components are always named; while sneaking every component is.
 * Also re-meshes the board when a component burns out (or gets replaced), so burnt components can disappear.
 */
@Mixin(value = CircuitBoardBlockEntity.class, remap = false)
public abstract class CircuitBoardBlockEntityMixin {
    @Unique private int powerleds$burntSignature = 0;

    @Inject(method = "addToGoggleTooltip(Ljava/util/List;Z)Z", at = @At("RETURN"), cancellable = true)
    private void powerleds$lookedAtComponent(List<Component> tooltip, boolean sneaking, CallbackInfoReturnable<Boolean> cir) {
        CircuitBoardBlockEntity be = (CircuitBoardBlockEntity) (Object) this;

        String header = I18n.get("powergrid.gui.damage_header");
        String body = I18n.get("powergrid.gui.circuit_board.damage_body");
        String repair = I18n.get("powergrid.gui.circuit_board.damage_creative_repair");
        tooltip.removeIf(c -> {
            String t = c.getString().trim();
            return t.equals(repair) || (!sneaking && (t.equals(header) || t.equals(body)));
        });

        HitResult hit = ClientSideAccess.getHitResult();
        if (hit != null) {
            PlacedComponent found = BoardPicking.pick(be, hit.getLocation(), ClientSideAccess.player().getLookAngle());
            if (found != null && !(found.component instanceof IComponentGoggleInformation)) {
                boolean burnt = BurntComponents.isBurnt(be, found);
                boolean hasUnit = false;
                float temperature = 0f, limit = 0f;
                BakedCircuit baked = be.getBaked();
                if (baked != null) {
                    for (ThermalUnit unit : baked.thermalUnits) {
                        if (!found.uuid.equals(unit.getId())) continue;
                        hasUnit = true;
                        temperature = Math.max(temperature, unit.getTemperature());
                        limit = Math.max(limit, unit.getOverheatTemperature());
                    }
                }
                // normal: only broken components get a HUD; sneaking: every component shows all its info
                if (burnt || sneaking) {
                    tooltip.add(Component.literal("    ")
                            .append(Component.translatable(ComponentRegistry.getItem(found.component).getDescriptionId()))
                            .withStyle(ChatFormatting.AQUA));
                    tooltip.add(Component.literal("    ")
                            .append(Component.translatable(burnt ? "goggles.power_leds_nadditions.burnt_out" : "goggles.power_leds_nadditions.working"))
                            .withStyle(burnt ? ChatFormatting.RED : ChatFormatting.GREEN));
                    if (sneaking && hasUnit) {
                        tooltip.add(Component.literal("    ")
                                .append(Component.translatable("goggles.power_leds_nadditions.temperature",
                                        Unit.TEMPERATURE.format(temperature), Unit.TEMPERATURE.format(limit)))
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        }
        cir.setReturnValue(!tooltip.isEmpty());
    }

    /** When a component burns out (or is replaced) the board's static model has to be rebuilt. */
    @Inject(method = "readFromSync", at = @At("RETURN"))
    private void powerleds$remeshOnBurn(FriendlyByteBuf buf, CallbackInfo ci) {
        CircuitBoardBlockEntity be = (CircuitBoardBlockEntity) (Object) this;
        BakedCircuit baked = be.getBaked();
        if (baked == null) return;
        int signature = 0;
        for (ThermalUnit unit : baked.thermalUnits) {
            if (unit.hasOverheated()) signature = signature * 31 + unit.getKey().hashCode() + 1;
        }
        if (signature != powerleds$burntSignature) {
            powerleds$burntSignature = signature;
            org.patryk3211.powergrid.circuits.components.Component.modelChanged(be.getBlockPos());
        }
    }
}
