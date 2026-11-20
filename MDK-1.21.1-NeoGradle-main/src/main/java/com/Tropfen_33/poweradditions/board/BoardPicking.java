package com.Tropfen_33.poweradditions.board;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlock;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

/** Finds the component under a hit position (same maths as Powergrid's goggle tooltip, but forgiving at edges). */
public final class BoardPicking {
    private BoardPicking() {}

    public static PlacedComponent pick(CircuitBoardBlockEntity be, Vec3 hit, Vec3 look) {
        // Hits on the side of a tall component lie exactly on its footprint edge, so first nudge the point
        // a little into the surface (along the view direction), then fall back to a small tolerance.
        PlacedComponent found = pickAt(be, hit.add(look.normalize().scale(0.01)), 0.0);
        if (found == null) found = pickAt(be, hit, 0.06);
        return found;
    }

    private static PlacedComponent pickAt(CircuitBoardBlockEntity be, Vec3 world, double tolerance) {
        BlockPos pos = be.getBlockPos();
        Vec3 v = world.subtract(pos.getX(), pos.getY(), pos.getZ());
        v = VecHelper.rotateCentered(v, -CircuitBoardBlock.getAngleY(be.getBlockState()), Direction.Axis.Y);
        v = VecHelper.rotateCentered(v, -CircuitBoardBlock.getAngleX(be.getBlockState()), Direction.Axis.X);

        PlacedComponent best = null;
        double bestDist = Double.MAX_VALUE;
        for (PlacedComponent placed : (Iterable<PlacedComponent>) be.getComponentsStream()::iterator) {
            double px = v.x * 16.0 - placed.x;
            double pz = v.z * 16.0 - placed.y;
            double w = placed.footprint().getWidth();
            double h = placed.footprint().getHeight();
            if (px >= -tolerance && pz >= -tolerance && px < w + tolerance && pz < h + tolerance) {
                double dx = px - w / 2.0, dz = pz - h / 2.0;
                double d = dx * dx + dz * dz;
                if (d < bestDist) { bestDist = d; best = placed; }
            }
        }
        return best;
    }
}
