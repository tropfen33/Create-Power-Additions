package com.Tropfen_33.powerleds.components;

import com.Tropfen_33.powerleds.LedColor;
import com.Tropfen_33.powerleds.PowerLeds;
import net.minecraft.network.chat.Component;
import org.patryk3211.powergrid.circuits.components.properties.BooleanProperty;
import org.patryk3211.powergrid.circuits.components.properties.ConstantProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.utility.Unit;

/**
 * Hidden synced per-die state (index 0..2 = R, G, B; single LEDs only use index 0) and the
 * read-only "datasheet" entries shown in the component info. Lang keys:
 * power_leds.component.property.<name> (+ ".summary").
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
    public static final ConstantProperty MIN_CURRENT;
    public static final ConstantProperty REVERSE_RATING;

    static {
        for (int i = 0; i < 3; i++) {
            BRIGHTNESS[i] = new FloatProperty(PowerLeds.MOD_ID, "brightness_" + i, 0f, 0f, 1f).hidden().<FloatProperty>cast();
            BROKEN[i] = new BooleanProperty(PowerLeds.MOD_ID, "broken_" + i, false).hidden().<BooleanProperty>cast();
        }
        for (LedColor c : LedColor.values()) {
            int i = c.ordinal();
            VF[i] = new ConstantProperty(PowerLeds.MOD_ID, "vf_" + c.id, Unit.VOLTAGE.format(c.vf20));
            BREAKDOWN[i] = new ConstantProperty(PowerLeds.MOD_ID, "vbr_" + c.id, Unit.VOLTAGE.format(c.reverseBreakdown));
            SERIES_R[i] = new ConstantProperty(PowerLeds.MOD_ID, "rs_" + c.id, Unit.RESISTANCE.format(c.rs));
            WAVELENGTH[i] = new ConstantProperty(PowerLeds.MOD_ID, "wl_" + c.id, Component.literal(c.wavelengthNm + " nm"));
            INTENSITY[i] = new ConstantProperty(PowerLeds.MOD_ID, "mcd_" + c.id, Component.literal(c.intensityMcd + " mcd"));
        }
        MIN_CURRENT = new ConstantProperty(PowerLeds.MOD_ID, "min_current", Unit.CURRENT.format(1.0e-4));
        REVERSE_RATING = new ConstantProperty(PowerLeds.MOD_ID, "reverse_rating", Unit.VOLTAGE.format(5.0));
    }
}
