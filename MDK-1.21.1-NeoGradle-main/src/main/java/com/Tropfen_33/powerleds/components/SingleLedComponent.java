package com.Tropfen_33.powerleds.components;

import com.Tropfen_33.powerleds.LedColor;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

/** One die, two pins: cathode = terminal 0 (short leg), anode = terminal 1 (long leg). */
public class SingleLedComponent extends AbstractLedComponent {
    public SingleLedComponent(LedColor color) {
        super(footprint(), color);
    }

    private static ComponentFootprint footprint() {
        return new ComponentFootprint.Builder(2, 2, null, "component.powergrid")
                .addPadSharedText(0, 0, 0, "generic.cathode", "generic.cathode.short")
                .addPadSharedText(1, 1, 1, "generic.anode", "generic.anode.short")
                .withItem()
                .withOutline()
                .build();
    }

    @Override protected int anodeTerminal(int die) { return 1; }
    @Override protected int cathodeTerminal() { return 0; }
}
