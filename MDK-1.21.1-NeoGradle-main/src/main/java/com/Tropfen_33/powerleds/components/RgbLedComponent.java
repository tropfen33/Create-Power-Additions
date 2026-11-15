package com.Tropfen_33.powerleds.components;

import com.Tropfen_33.powerleds.LedColor;
import net.minecraft.network.chat.Component;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

/**
 * Four-pin common-cathode RGB LED ("white" LED). Pads counter-clockwise from the top-left:
 * R anode (0,0), G anode (0,1), B anode (1,1), common cathode (1,0).
 * Each die has its own current, so unequal currents shift the colour.
 */
public class RgbLedComponent extends AbstractLedComponent {
    public RgbLedComponent() {
        super(footprint(), LedColor.RED, LedColor.GREEN, LedColor.BLUE);
    }

    private static ComponentFootprint footprint() {
        return new ComponentFootprint.Builder(2, 2, null, "component.powergrid")
                .addPad(0, 0, 0, Component.translatable("component.power_leds.pad.red"),
                        Component.translatable("component.power_leds.pad.red.short"))
                .addPad(0, 1, 1, Component.translatable("component.power_leds.pad.green"),
                        Component.translatable("component.power_leds.pad.green.short"))
                .addPad(1, 1, 2, Component.translatable("component.power_leds.pad.blue"),
                        Component.translatable("component.power_leds.pad.blue.short"))
                .addPadSharedText(1, 0, 3, "generic.cathode", "generic.cathode.short")
                .withItem()
                .withOutline()
                .build();
    }

    @Override protected int anodeTerminal(int die) { return die; }
    @Override protected int cathodeTerminal() { return 3; }
}
