package com.Tropfen_33.poweradditions.components;

import com.Tropfen_33.poweradditions.LedColor;
import com.Tropfen_33.poweradditions.LedRegistries;
import com.Tropfen_33.poweradditions.items.BrokenWhiteLedItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
                .addPad(0, 0, 0, Component.translatable("component.poweradditions.pad.red"),
                        Component.translatable("component.poweradditions.pad.red.short"))
                .addPad(0, 1, 1, Component.translatable("component.poweradditions.pad.green"),
                        Component.translatable("component.poweradditions.pad.green.short"))
                .addPad(1, 1, 2, Component.translatable("component.poweradditions.pad.blue"),
                        Component.translatable("component.poweradditions.pad.blue.short"))
                .addPadSharedText(1, 0, 3, "generic.cathode", "generic.cathode.short")
                .withItem()
                .withOutline()
                .build();
    }

    @Override protected Item functionalItem() { return LedRegistries.LED_WHITE.get(); }
    @Override protected ItemStack brokenStack(int mask) { return BrokenWhiteLedItem.of(mask); }

    @Override protected int anodeTerminal(int die) { return die; }
    @Override protected int cathodeTerminal() { return 3; }
}
