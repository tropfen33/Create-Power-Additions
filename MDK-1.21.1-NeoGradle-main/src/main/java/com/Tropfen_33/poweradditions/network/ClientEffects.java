package com.Tropfen_33.poweradditions.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.particles.SparkParticleData;

public final class ClientEffects {
    private ClientEffects() {}

    /** Exactly what Powergrid's BakedCircuit does when one of its own components overheats. */
    public static void handle(BurnEffectPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            Vec3 pos = payload.pos();
            SparkParticleData.explodeParticles(level, pos.x, pos.y, pos.z, Direction.UP, 10);
            ModdedSoundEvents.COMPONENT_EXPLODE.playAt(level, pos, 1.0f, level.random.nextFloat() * 0.1f + 0.9f, true);
        });
    }
}
