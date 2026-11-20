package com.Tropfen_33.poweradditions.components;

import com.Tropfen_33.poweradditions.LedColor;
import com.Tropfen_33.poweradditions.LedPhysics;
import com.Tropfen_33.poweradditions.LedPhysics.DieState;
import com.Tropfen_33.poweradditions.client.LedRenderer;
import com.google.common.collect.ImmutableCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.ChatFormatting;
import org.patryk3211.powergrid.circuits.components.IComponentGoggleInformation;
import org.patryk3211.powergrid.circuits.components.IInteractableComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;
import org.patryk3211.powergrid.circuits.components.IRenderedComponent;
import org.patryk3211.powergrid.circuits.components.MirrorableComponent;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.electricity.sim.special.PNJunctionWire;

import java.util.Collection;
import java.util.List;

/**
 * Shared logic for LEDs made of 1..3 dies. Rotatable and mirrorable (via MirrorableComponent).
 * Each die is a PNJunctionWire from its anode terminal to the cathode terminal.
 */
public abstract class AbstractLedComponent extends MirrorableComponent implements IRenderedComponent, IInteractableComponent, IComponentGoggleInformation {
    protected final LedColor[] dies;

    /**
     * Component's constructor calls addProperties() before our fields exist, so the dies are handed
     * over through this thread-local (set while the super() arguments are evaluated).
     */
    private static final ThreadLocal<LedColor[]> PENDING = new ThreadLocal<>();

    private static ComponentFootprint pending(ComponentFootprint footprint, LedColor[] dies) {
        PENDING.set(dies);
        return footprint;
    }

    protected AbstractLedComponent(ComponentFootprint footprint, LedColor... dies) {
        super(pending(footprint, dies));
        this.dies = dies;
        PENDING.remove();
    }

    /** Terminal index of the anode of die i. */
    protected abstract int anodeTerminal(int die);

    /** The working LED item that repairs this component. */
    protected abstract Item functionalItem();

    /** The burnt-out item handed back; bit i of the mask = die i was broken. */
    protected abstract ItemStack brokenStack(int mask);

    /** Terminal index of the (shared) cathode. */
    protected abstract int cathodeTerminal();

    public LedColor[] dies() { return dies; }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> builder) {
        super.addProperties(builder);
        LedColor[] d = PENDING.get();
        builder.add(current((float) LedColor.RATED_CURRENT), LedProps.MIN_CURRENT, LedProps.MAX_TEMP);
        for (int i = 0; i < d.length; i++) {
            int c = d[i].ordinal();
            builder.add(LedProps.VF[c], LedProps.SERIES_R[c], LedProps.BREAKDOWN[c],
                    LedProps.MAX_POWER[c], LedProps.WAVELENGTH[c], LedProps.INTENSITY[c],
                    LedProps.BRIGHTNESS[i], LedProps.BROKEN[i]);
        }
    }

    // ---------------- models ----------------

    public boolean anyBroken(PlacedComponent placed) {
        for (int i = 0; i < dies.length; i++) {
            if (placed.get(LedProps.BROKEN[i])) return true;
        }
        return false;
    }

    @Override
    public ResourceLocation getModelId(PlacedComponent placed) {
        ResourceLocation id = ComponentRegistry.getId(this);
        return anyBroken(placed) ? id.withSuffix("_burnt") : id;
    }

    @Override
    public Collection<ResourceLocation> requestedModels() {
        ResourceLocation id = ComponentRegistry.getId(this);
        return List.of(id, id.withSuffix("_burnt"));
    }

    // ---------------- simulation ----------------

    @Override
    public void bake(PlacedComponent placed, ComponentCircuitBuilder builder, ThermalBuilder.IEmitter thermal) {
        for (int i = 0; i < dies.length; i++) {
            LedColor c = dies[i];
            PNJunctionWire wire = new PNJunctionWire(
                    c.saturationCurrent(), c.rs, LedColor.ROOM_TEMP_C, c.ideality,
                    c.reverseBreakdown, 1.0e-6,
                    builder.terminalNode(anodeTerminal(i)),
                    builder.terminalNode(cathodeTerminal()));
            if (placed.get(LedProps.BROKEN[i])) {
                wire.setIdealityFactor(LedPhysics.BROKEN_IDEALITY);
            }
            builder.add(wire);
            placed.add(wire);
        }
    }

    private DieState[] state(PlacedComponent placed) {
        if (placed.customData instanceof DieState[] existing) {
            return existing;
        }
        DieState[] created = new DieState[dies.length];
        for (int i = 0; i < created.length; i++) created[i] = new DieState();
        placed.customData = created;
        return created;
    }

    @Override
    public boolean tick(PlacedComponent placed) {
        // client: only refresh the baked model when a die got destroyed
        placed.onClientWorld(() -> level -> {
            DieState[] st = state(placed);
            boolean broken = anyBroken(placed);
            if (st[0].brokenSeen != broken) {
                st[0].brokenSeen = broken;
                modelChanged(placed.getPos());
            }
        });

        if (placed.wires.size() < dies.length) return true;

        // server: light output + damage
        placed.onServerWorld(() -> level -> {
            DieState[] st = state(placed);
            for (int i = 0; i < dies.length; i++) {
                if (placed.get(LedProps.BROKEN[i])) continue;
                PNJunctionWire wire = (PNJunctionWire) placed.wires.get(i);
                double amps = wire.current();

                // --- heat: dissipated power warms the junction, cooling pulls it back to ambient ---
                double ambient = ThermalBehaviour.getAmbientTemperature(level, placed.getPos());
                double cooling = 1.0;
                if (level.getBlockEntity(placed.getPos()) instanceof CircuitBoardBlockEntity be
                        && be.totalCoolingFactorMultiplier > 0.01f) {
                    cooling = be.totalCoolingFactorMultiplier;
                }
                LedPhysics.heat(st[i], dies[i], Math.abs(wire.power()), ambient, cooling);

                // warning: smoke while it gets close to the limit
                if (st[i].temp > LedPhysics.SMOKE_C && level instanceof ServerLevel sl
                        && sl.random.nextFloat() < (st[i].temp - LedPhysics.SMOKE_C) / 35f) {
                    Vec3 pos = placed.getExactPos();
                    sl.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + 0.15, pos.z, 1, 0.02, 0.02, 0.02, 0.01);
                }

                boolean dead = LedPhysics.overheated(st[i]) || LedPhysics.accumulate(st[i], amps, dies[i]);
                if (dead) {
                    // spark + crack, then the die is dead
                    Vec3 pos = placed.getExactPos();
                    if (level instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.15, pos.z, 14, 0.06, 0.06, 0.06, 0.25);
                        sl.sendParticles(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + 0.15, pos.z, 4, 0.03, 0.03, 0.03, 0.02);
                    }
                    level.playSound(null, placed.getPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5f, 1.9f);
                    level.playSound(null, placed.getPos(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 0.12f, 2.0f);
                    placed.set(LedProps.BROKEN[i], true);
                    placed.notifyClients(LedProps.BROKEN[i]);
                    wire.setIdealityFactor(LedPhysics.BROKEN_IDEALITY);
                    st[i].smooth = 0f;
                    placed.set(LedProps.BRIGHTNESS[i], 0f);
                    placed.notifyClients(LedProps.BRIGHTNESS[i]);
                    st[i].sent = 0f;
                    continue;
                }

                float target = LedPhysics.brightness(amps);
                st[i].smooth += (target - st[i].smooth) * 0.5f;
                if (st[i].smooth < 0.01f) st[i].smooth = 0f;
                float v = st[i].smooth;
                if (Math.abs(v - st[i].sent) >= 0.04f || ((v == 0f) != (st[i].sent == 0f))) {
                    st[i].sent = v;
                    placed.set(LedProps.BRIGHTNESS[i], v);
                    placed.notifyClients(LedProps.BRIGHTNESS[i]);
                }
            }
        });
        return true;
    }

    // ---------------- replacing a burnt-out LED ----------------

    @Override
    public VoxelShape getShape(PlacedComponent placed) {
        return IInteractableComponent.extrudedFootprint(placed, 0.1875f);
    }

    /**
     * Right-click a burnt-out LED on a board with a working LED of the same type: the board gets the
     * working LED (all dies repaired) and the player gets the burnt-out one, remembering which dies broke.
     */
    @Override
    public InteractionResult use(CircuitBoardBlockEntity board, PlacedComponent placed, Player player) {
        ItemStack held = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!anyBroken(placed) || !held.is(functionalItem())) return InteractionResult.FAIL;
        Level level = board.getLevel();
        if (level != null && level.isClientSide) return InteractionResult.SUCCESS;

        int mask = 0;
        for (int i = 0; i < dies.length; i++) {
            if (placed.get(LedProps.BROKEN[i])) mask |= 1 << i;
        }
        DieState[] st = state(placed);
        for (int i = 0; i < dies.length; i++) {
            placed.set(LedProps.BROKEN[i], false);
            placed.notifyClients(LedProps.BROKEN[i]);
            placed.set(LedProps.BRIGHTNESS[i], 0f);
            placed.notifyClients(LedProps.BRIGHTNESS[i]);
            st[i].damage = 0f;
            st[i].temp = 0f;
            st[i].hotTicks = 0;
            st[i].smooth = 0f;
            st[i].sent = 0f;
            if (i < placed.wires.size()) {
                ((PNJunctionWire) placed.wires.get(i)).setIdealityFactor(dies[i].ideality);
            }
        }
        if (!player.isCreative()) held.shrink(1);
        ItemStack broken = brokenStack(mask);
        if (!player.getInventory().add(broken)) player.drop(broken, false);
        board.setChanged();
        return InteractionResult.SUCCESS;
    }

    // ---------------- goggles: what am I looking at? ----------------

    @Override
    public boolean addToGoggleTooltip(PlacedComponent placed, List<net.minecraft.network.chat.Component> tooltip, boolean sneaking) {
        // normal: only shown for a burnt-out LED (just the burnt dies); sneaking: everything
        if (!sneaking && !anyBroken(placed)) return false;
        String indent = "    ";
        tooltip.add(net.minecraft.network.chat.Component.literal(indent)
                .append(net.minecraft.network.chat.Component.translatable(ComponentRegistry.getItem(this).getDescriptionId()))
                .withStyle(ChatFormatting.AQUA));
        for (int i = 0; i < dies.length; i++) {
            net.minecraft.network.chat.Component color =
                    net.minecraft.network.chat.Component.translatable("color.poweradditions." + dies[i].id);
            if (placed.get(LedProps.BROKEN[i])) {
                tooltip.add(net.minecraft.network.chat.Component.literal(indent)
                        .append(net.minecraft.network.chat.Component.translatable("goggles.poweradditions.die_broken", color))
                        .withStyle(ChatFormatting.RED));
            } else if (sneaking) {
                int percent = Math.round(placed.get(LedProps.BRIGHTNESS[i]) * 100f);
                tooltip.add(net.minecraft.network.chat.Component.literal(indent)
                        .append(net.minecraft.network.chat.Component.translatable("goggles.poweradditions.die_ok", color, percent))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return true;
    }

    // ---------------- rendering ----------------

    @Override
    public void render(CircuitBoardBlockEntity board, PlacedComponent placed, float partialTicks,
                       PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        LedRenderer.render(this, board, placed, ms, buffer, light);
    }
}
