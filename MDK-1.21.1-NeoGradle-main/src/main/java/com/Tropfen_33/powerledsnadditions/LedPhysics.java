package com.Tropfen_33.powerledsnadditions;

public final class LedPhysics {
    private LedPhysics() {}

    /** Ideality factor used to turn a destroyed die into an (almost) open circuit. */
    public static final double BROKEN_IDEALITY = 5000.0;
    /** Reverse current that counts as "breakdown conduction". */
    public static final double REVERSE_BURST_CURRENT = 0.001;
    /** Accumulated damage (in "ticks at 2x limit current") at which a die dies. */
    public static final float BREAK_DAMAGE = 40f;

    /** Light output 0..1. Datasheet curves of luminous intensity vs. current are slightly sub-linear. */
    public static float brightness(double forwardAmps) {
        if (forwardAmps < 1.0e-4) return 0f; // below 0.1 mA nothing visible
        double x = forwardAmps / LedColor.RATED_CURRENT;
        return (float) Math.min(1.0, Math.pow(x, 0.9));
    }

    /** Seconds per game tick. */
    public static final double DT = 0.05;
    /** Thermal time constant of a 5 mm LED junction in the epoxy [s]. */
    public static final double TAU = 2.0;
    /** Ticks a die has to stay at/over the maximum junction temperature before it dies (cf. Powergrid's own units). */
    public static final int OVERHEAT_TICKS = 2;
    /** Smoke starts when the junction passes this temperature [degC]. */
    public static final double SMOKE_C = 90.0;
    /** Thermal resistance junction -> ambient of a 5 mm LED in air [K/W] (datasheets: roughly 250-400). */
    public static final double RTH_JA = 300.0;

    /** Thermal resistance junction -> ambient [K/W]. At 20 mA this only warms the die by roughly 12-19 K. */
    public static double thermalResistance(LedColor c, double ambientC) {
        return RTH_JA;
    }

    /** One tick of the junction temperature: C dT/dt = P - (T - Tamb) * cooling / Rth. */
    public static void heat(DieState s, LedColor c, double powerW, double ambientC, double cooling) {
        double rth = thermalResistance(c, ambientC);
        double cap = TAU / rth;                       // heat capacity [J/K]
        if (s.temp == 0f) s.temp = (float) ambientC;  // first tick
        double dT = (powerW - (s.temp - ambientC) * cooling / rth) / cap * DT;
        s.temp += (float) dT;
        if (s.temp < ambientC) s.temp = (float) ambientC;
        if (s.temp >= LedColor.MAX_JUNCTION_C) s.hotTicks++; else s.hotTicks = Math.max(0, s.hotTicks - 1);
    }

    /**
     * Reverse breakdown check for one tick: conducting backwards for a moment melts the die almost at once.
     * Returns true when the die is destroyed by that (overheating is handled by heat()).
     */
    public static boolean accumulate(DieState s, double amps, LedColor c) {
        if (amps < -REVERSE_BURST_CURRENT) s.damage += 6f;
        else s.damage = Math.max(0f, s.damage - 0.02f);
        return s.damage >= BREAK_DAMAGE;
    }

    public static boolean overheated(DieState s) {
        return s.hotTicks >= OVERHEAT_TICKS;
    }

    public static final class DieState {
        public float damage;
        public float temp;        // junction temperature [degC], 0 = not initialised
        public int hotTicks;
        public float smooth;      // low-pass filtered brightness
        public float sent;        // last brightness sent to clients
        public boolean brokenSeen; // client side: last broken flag, to refresh the baked model
    }
}
