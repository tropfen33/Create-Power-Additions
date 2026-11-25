package com.Tropfen_33.poweradditions.network;

import com.Tropfen_33.poweradditions.PowerAdditions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client copy of the burnt components per board.
 * Filled by the server's BurntPayload packets (authoritative) and, as a fallback, remembered as soon as the client
 * itself sees a component overheat - so a burnt component does not look repaired again when the circuit is
 * re-baked (which resets Powergrid's own thermal state) because ANOTHER component got replaced.
 */
public final class ClientBurnt {
    private ClientBurnt() {}

    private record Key(ResourceKey<Level> dimension, BlockPos pos) {}

    private static final Map<Key, Set<Integer>> BURNT = new ConcurrentHashMap<>();

    private static Key key(Level level, BlockPos pos) {
        return new Key(level.dimension(), pos.immutable());
    }

    public static boolean isBurnt(Level level, BlockPos pos, int component) {
        Set<Integer> set = BURNT.get(key(level, pos));
        return set != null && set.contains(component);
    }

    /** The client noticed an overheated component itself; keep it burnt until the server says otherwise. */
    public static void remember(Level level, BlockPos pos, int component) {
        BURNT.computeIfAbsent(key(level, pos), k -> ConcurrentHashMap.newKeySet()).add(component);
    }

    public static void clear() {
        BURNT.clear();
    }

    public static void handle(BurntPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level == null) return;
            PowerAdditions.LOGGER.info("[PowerAdditions] burnt list received for board {}: {} component(s)",
                    payload.pos(), payload.burnt().size());
            Key key = key(level, payload.pos());
            if (payload.burnt().isEmpty()) {
                BURNT.remove(key);
            } else {
                Set<Integer> set = ConcurrentHashMap.newKeySet();
                set.addAll(payload.burnt());
                BURNT.put(key, set);
            }
            // the board's static model has to be rebuilt (burnt parts show their burnt model)
            org.patryk3211.powergrid.circuits.components.Component.modelChanged(payload.pos());
        });
    }
}
