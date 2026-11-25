package com.Tropfen_33.poweradditions.network;

import com.Tropfen_33.poweradditions.PowerAdditions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Server -> client: the complete list of burnt component keys of one circuit board. */
public record BurntPayload(BlockPos pos, List<Integer> burnt) implements CustomPacketPayload {
    public static final Type<BurntPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(PowerAdditions.MOD_ID, "burnt_components"));

    public static final StreamCodec<RegistryFriendlyByteBuf, BurntPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BurntPayload::pos,
            ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), BurntPayload::burnt,
            BurntPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
