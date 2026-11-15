package com.Tropfen_33.powerleds;

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

    /**
     * Accumulates damage for one tick. Overcurrent damage grows with (I/Imax)^2 - 1, so 5 % over the
     * limit takes many seconds while 2x the limit kills the die within a few ticks. Reverse breakdown
     * conduction (a short burst) melts the die very quickly. Returns true when the die is destroyed.
     */
    public static boolean accumulate(DieState s, double amps, LedColor c) {
        if (amps < -REVERSE_BURST_CURRENT) {
            s.damage += 6f;
        } else {
            double ratio = amps / c.maxCurrent;
            if (ratio > 1.0) s.damage += (float) (ratio * ratio - 1.0);
            else s.damage = Math.max(0f, s.damage - 0.02f);
        }
        return s.damage >= BREAK_DAMAGE;
    }

    public static final class DieState {
        public float damage;
        public float smooth;      // low-pass filtered brightness
        public float sent;        // last brightness sent to clients
        public boolean brokenSeen; // client side: last broken flag, to refresh the baked model
    }
}
