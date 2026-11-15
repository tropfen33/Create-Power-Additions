package com.Tropfen_33.powerleds.components;

import com.Tropfen_33.powerleds.LedColor;
import com.Tropfen_33.powerleds.LedPhysics;
import com.Tropfen_33.powerleds.LedPhysics.DieState;
import com.Tropfen_33.powerleds.client.LedRenderer;
import com.google.common.collect.ImmutableCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
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
public abstract class AbstractLedComponent extends MirrorableComponent implements IRenderedComponent {
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

    /** Terminal index of the (shared) cathode. */
    protected abstract int cathodeTerminal();

    public LedColor[] dies() { return dies; }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> builder) {
        super.addProperties(builder);
        LedColor[] d = PENDING.get();
        builder.add(current((float) LedColor.RATED_CURRENT), LedProps.MIN_CURRENT, LedProps.REVERSE_RATING);
        for (int i = 0; i < d.length; i++) {
            int c = d[i].ordinal();
            builder.add(LedProps.VF[c], LedProps.SERIES_R[c], LedProps.BREAKDOWN[c],
                    LedProps.WAVELENGTH[c], LedProps.INTENSITY[c],
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

                if (LedPhysics.accumulate(st[i], amps, dies[i])) {
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

    // ---------------- rendering ----------------

    @Override
    public void render(CircuitBoardBlockEntity board, PlacedComponent placed, float partialTicks,
                       PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        LedRenderer.render(this, board, placed, ms, buffer, light);
    }
}
