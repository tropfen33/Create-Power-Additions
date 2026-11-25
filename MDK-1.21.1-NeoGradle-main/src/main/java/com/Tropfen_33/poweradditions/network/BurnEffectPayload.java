package com.Tropfen_33.poweradditions.network;

import com.Tropfen_33.poweradditions.PowerAdditions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Server -> client: play Powergrid's component burn-out effect (sparks + explode sound) at a position. */
public record BurnEffectPayload(Vec3 pos) implements CustomPacketPayload {
    public static final Type<BurnEffectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PowerAdditions.MOD_ID, "burn_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BurnEffectPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, payload -> payload.pos().x,
            ByteBufCodecs.DOUBLE, payload -> payload.pos().y,
            ByteBufCodecs.DOUBLE, payload -> payload.pos().z,
            (x, y, z) -> new BurnEffectPayload(new Vec3(x, y, z)));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
