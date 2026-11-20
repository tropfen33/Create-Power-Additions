package com.Tropfen_33.poweradditions.components;

import com.Tropfen_33.poweradditions.LedColor;
import com.Tropfen_33.poweradditions.PowerAdditions;
import net.minecraft.network.chat.Component;
import org.patryk3211.powergrid.circuits.components.properties.BooleanProperty;
import org.patryk3211.powergrid.circuits.components.properties.ConstantProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.utility.Unit;

/**
 * Hidden synced per-die state (index 0..2 = R, G, B; single LEDs only use index 0) and the
 * read-only "datasheet" entries shown in the component info. Lang keys:
 * poweradditions.component.property.<name> (+ ".summary").
 */
public final class LedProps {
    private LedProps() {}

    public static final FloatProperty[] BRIGHTNESS = new FloatProperty[3];
    public static final BooleanProperty[] BROKEN = new BooleanProperty[3];

    // datasheet entries, indexed by LedColor.ordinal()
    public static final ConstantProperty[] VF = new ConstantProperty[3];
    public static final ConstantProperty[] BREAKDOWN = new ConstantProperty[3];
    public static final ConstantProperty[] SERIES_R = new ConstantProperty[3];
    public static final ConstantProperty[] WAVELENGTH = new ConstantProperty[3];
    public static final ConstantProperty[] INTENSITY = new ConstantProperty[3];
    public static final ConstantProperty[] MAX_POWER = new ConstantProperty[3];
    public static final ConstantProperty MAX_TEMP;
    public static final ConstantProperty MIN_CURRENT;

    static {
        for (int i = 0; i < 3; i++) {
            BRIGHTNESS[i] = new FloatProperty(PowerAdditions.MOD_ID, "brightness_" + i, 0f, 0f, 1f).hidden().<FloatProperty>cast();
            BROKEN[i] = new BooleanProperty(PowerAdditions.MOD_ID, "broken_" + i, false).hidden().<BooleanProperty>cast();
        }
        for (LedColor c : LedColor.values()) {
            int i = c.ordinal();
            VF[i] = new ConstantProperty(PowerAdditions.MOD_ID, "vf_" + c.id, Unit.VOLTAGE.format(c.vf20));
            BREAKDOWN[i] = new ConstantProperty(PowerAdditions.MOD_ID, "vbr_" + c.id, Unit.VOLTAGE.format(c.reverseBreakdown));
            SERIES_R[i] = new ConstantProperty(PowerAdditions.MOD_ID, "rs_" + c.id, Unit.RESISTANCE.format(c.rs));
            WAVELENGTH[i] = new ConstantProperty(PowerAdditions.MOD_ID, "wl_" + c.id, Component.literal(c.wavelengthNm + " nm"));
            MAX_POWER[i] = new ConstantProperty(PowerAdditions.MOD_ID, "pmax_" + c.id, Unit.POWER.format(c.maxPower()));
            INTENSITY[i] = new ConstantProperty(PowerAdditions.MOD_ID, "mcd_" + c.id, Component.literal(c.intensityMcd + " mcd"));
        }
        MAX_TEMP = new ConstantProperty(PowerAdditions.MOD_ID, "max_temp", Unit.TEMPERATURE.format(LedColor.MAX_JUNCTION_C));
        MIN_CURRENT = new ConstantProperty(PowerAdditions.MOD_ID, "min_current", Unit.CURRENT.format(1.0e-4));
    }
}
