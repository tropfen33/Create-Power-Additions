package com.Tropfen_33.powerledsnadditions;

/**
 * Electrical model of one LED die, fitted to typical 5 mm LED datasheet values
 * (Vf at 20 mA, absolute max forward current, reverse voltage rating).
 *
 * Shockley model used by Powergrid's PNJunctionWire:
 *   I = Is * (exp((V - I*Rs) / (n*Vt)) - 1)
 * Is is derived so that Vf(20 mA) hits the datasheet typical value:
 *   Is = I20 / exp((Vf20 - I20*Rs) / (n*Vt))
 */
public enum LedColor {
    //            name     Vf@20mA  Rs[ohm]  n     Imax[A]  Vbr[V]  glow RGB          nm   mcd@20mA
    RED  ("red",   2.00,    8.0,     1.97, 0.020,   12.0,   255,  16,   0,      630,  500),
    GREEN("green", 3.10,   10.0,     3.40, 0.020,    8.0,    24, 255,  64,      525, 1300),
    BLUE ("blue",  3.20,   10.0,     3.40, 0.020,    8.0,    16,  48, 255,      470,  350);

    public static final double RATED_CURRENT = 0.020;      // reference current for Vf / brightness
    public static final double ROOM_TEMP_C = 22.0;          // the temperature Powergrid's diode uses
    private static final double VT = 1.380649E-23 * (ROOM_TEMP_C + 273.15) / 1.602176634E-19;

    public final String id;
    public final double vf20;
    public final double rs;
    public final double ideality;
    public final double maxCurrent;
    public final double reverseBreakdown;
    public final int r, g, b;
    public final int wavelengthNm, intensityMcd;
    /** Datasheet absolute maximum power dissipation [W]. */
    public double maxPower() { return this == RED ? 0.075 : 0.100; }
    /** Datasheet maximum junction temperature [degC]. */
    public static final double MAX_JUNCTION_C = 125.0;

    LedColor(String id, double vf20, double rs, double ideality, double maxCurrent,
             double reverseBreakdown, int r, int g, int b,
             int wavelengthNm, int intensityMcd) {
        this.id = id; this.vf20 = vf20; this.rs = rs; this.ideality = ideality;
        this.maxCurrent = maxCurrent; this.reverseBreakdown = reverseBreakdown;
        this.r = r; this.g = g; this.b = b;
        this.wavelengthNm = wavelengthNm; this.intensityMcd = intensityMcd;
    }

    public double saturationCurrent() {
        return RATED_CURRENT / Math.exp((vf20 - RATED_CURRENT * rs) / (ideality * VT));
    }
}
