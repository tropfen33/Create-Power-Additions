package com.Tropfen_33.poweradditions.network;

import com.Tropfen_33.poweradditions.board.IBurntBoard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;

import java.util.ArrayList;

public final class BurntSync {
    private BurntSync() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event ->
                event.registrar("1")
                        .playToClient(BurntPayload.TYPE, BurntPayload.CODEC, ClientBurnt::handle)
                        .playToClient(BurnEffectPayload.TYPE, BurnEffectPayload.CODEC, ClientEffects::handle));
    }

    /** Plays the standard component burn-out effect (sparks + sound) for everyone near the position. */
    public static void sendBurnEffect(ServerLevel level, net.minecraft.world.phys.Vec3 pos) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(net.minecraft.core.BlockPos.containing(pos)),
                new BurnEffectPayload(pos));
    }

    /** Sends the board's burnt list to everyone who sees it. Call whenever the list changed. */
    public static void send(CircuitBoardBlockEntity board) {
        if (!(board.getLevel() instanceof ServerLevel level)) return;
        var burnt = ((IBurntBoard) (Object) board).poweradditions$burntSet();
        com.Tropfen_33.poweradditions.PowerAdditions.LOGGER.info("[PowerAdditions] sending burnt list of board {}: {} component(s)", board.getBlockPos(), burnt.size());
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(board.getBlockPos()),
                new BurntPayload(board.getBlockPos(), new ArrayList<>(burnt)));
    }

    /** A player starts seeing a chunk: tell them about every burnt component in it. */
    public static void onChunkWatch(ChunkWatchEvent.Sent event) {
        for (BlockEntity be : event.getChunk().getBlockEntities().values()) {
            if (!(be instanceof CircuitBoardBlockEntity board)) continue;
            var burnt = ((IBurntBoard) (Object) board).poweradditions$burntSet();
            if (!burnt.isEmpty()) {
                PacketDistributor.sendToPlayer(event.getPlayer(), new BurntPayload(board.getBlockPos(), new ArrayList<>(burnt)));
            }
        }
    }
}
