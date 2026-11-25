package com.Tropfen_33.poweradditions.board;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

/**
 * Finds the component the player is looking at.
 * Small components (tubes, bulbs, ...) have no selection shape of their own, so the game's ray goes through them
 * and hits the board plate somewhere else. So the player's view ray is tested against every component's box.
 */
public final class BoardPicking {
    private BoardPicking() {}

    /** Height of a component's pick box above the board plate, in blocks (tall tubes are about 5 px). */
    private static final double BOX_HEIGHT = 0.32;
    private static final double REACH = 8.0;

    public static PlacedComponent pick(CircuitBoardBlockEntity be, Vec3 eye, Vec3 look, Vec3 hit) {
        PlacedComponent found = pickByRay(be, eye, look);
        if (found == null && hit != null) found = pickByHit(be, hit, look);
        return found;
    }

    // ---------- ray vs. component boxes ----------

    private static Vec3 toLocal(CircuitBoardBlockEntity be, Vec3 world) {
        BlockPos pos = be.getBlockPos();
        Vec3 v = world.subtract(pos.getX(), pos.getY(), pos.getZ());
        v = VecHelper.rotateCentered(v, -CircuitBoardBlock.getAngleY(be.getBlockState()), Direction.Axis.Y);
        v = VecHelper.rotateCentered(v, -CircuitBoardBlock.getAngleX(be.getBlockState()), Direction.Axis.X);
        return v;
    }

    private static PlacedComponent pickByRay(CircuitBoardBlockEntity be, Vec3 eye, Vec3 look) {
        Vec3 o = toLocal(be, eye);
        Vec3 d = toLocal(be, eye.add(look.normalize())).subtract(o);
        PlacedComponent best = null;
        double bestT = Double.MAX_VALUE;
        for (PlacedComponent placed : (Iterable<PlacedComponent>) be.getComponentsStream()::iterator) {
            double t = rayBox(o, d,
                    placed.x / 16.0, 0.0, placed.y / 16.0,
                    (placed.x + placed.footprint().getWidth()) / 16.0, BOX_HEIGHT,
                    (placed.y + placed.footprint().getHeight()) / 16.0);
            if (t >= 0 && t <= REACH && t < bestT) {
                bestT = t;
                best = placed;
            }
        }
        return best;
    }

    /** Slab test; returns the entry distance along the ray or -1 when it misses. */
    private static double rayBox(Vec3 o, Vec3 d, double x0, double y0, double z0, double x1, double y1, double z1) {
        double tmin = 0.0, tmax = Double.MAX_VALUE;
        double[] os = {o.x, o.y, o.z}, ds = {d.x, d.y, d.z};
        double[] mins = {x0, y0, z0}, maxs = {x1, y1, z1};
        for (int i = 0; i < 3; i++) {
            if (Math.abs(ds[i]) < 1.0e-9) {
                if (os[i] < mins[i] || os[i] > maxs[i]) return -1;
            } else {
                double t1 = (mins[i] - os[i]) / ds[i];
                double t2 = (maxs[i] - os[i]) / ds[i];
                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
                tmin = Math.max(tmin, t1);
                tmax = Math.min(tmax, t2);
                if (tmin > tmax) return -1;
            }
        }
        return tmin;
    }

    // ---------- fallback: hit position on the board (same maths as Powergrid's goggle tooltip) ----------

    private static PlacedComponent pickByHit(CircuitBoardBlockEntity be, Vec3 hit, Vec3 look) {
        PlacedComponent found = pickAt(be, hit.add(look.normalize().scale(0.01)), 0.0);
        if (found == null) found = pickAt(be, hit, 0.06);
        return found;
    }

    private static PlacedComponent pickAt(CircuitBoardBlockEntity be, Vec3 world, double tolerance) {
        Vec3 v = toLocal(be, world);
        PlacedComponent best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlacedComponent placed : (Iterable<PlacedComponent>) be.getComponentsStream()::iterator) {
            double px = v.x * 16.0 - placed.x;
            double pz = v.z * 16.0 - placed.y;
            double w = placed.footprint().getWidth();
            double h = placed.footprint().getHeight();
            if (px >= -tolerance && pz >= -tolerance && px < w + tolerance && pz < h + tolerance) {
                double dx = px - w / 2.0, dz = pz - h / 2.0;
                double dist = dx * dx + dz * dz;
                if (dist < bestDist) { bestDist = dist; best = placed; }
            }
        }
        return best;
    }
}
