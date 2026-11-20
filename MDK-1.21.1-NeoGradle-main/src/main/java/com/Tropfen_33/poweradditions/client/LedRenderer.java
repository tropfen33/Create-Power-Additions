package com.Tropfen_33.poweradditions.client;

import com.Tropfen_33.poweradditions.LedColor;
import com.Tropfen_33.poweradditions.components.AbstractLedComponent;
import com.Tropfen_33.poweradditions.components.LedProps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.RenderTypes;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;

public final class LedRenderer {
    private LedRenderer() {}

    private static final int FULL_BRIGHT = 15728880;

    public static void render(AbstractLedComponent led, CircuitBoardBlockEntity board, PlacedComponent placed,
                              PoseStack ms, MultiBufferSource buffer, int light) {
        LedColor[] dies = led.dies();
        float r = 0, g = 0, b = 0;
        for (int i = 0; i < dies.length; i++) {
            float br = placed.get(LedProps.BROKEN[i]) ? 0f : placed.get(LedProps.BRIGHTNESS[i]);
            r += dies[i].r * br;
            g += dies[i].g * br;
            b += dies[i].b * br;
        }
        int gr = Math.min(255, Math.round(r));
        int gg = Math.min(255, Math.round(g));
        int gb = Math.min(255, Math.round(b));
        boolean lit = Math.max(gr, Math.max(gg, gb)) > 6;

        // Glass-like see-through dome: tinted with the die colour (milky white for the RGB LED).
        int tr, tg, tb;
        if (dies.length == 1) {
            tr = 120 + dies[0].r * 135 / 255;
            tg = 120 + dies[0].g * 135 / 255;
            tb = 120 + dies[0].b * 135 / 255;
        } else {
            tr = tg = tb = 235;
        }
        if (lit) {
            tr = Math.min(255, (tr + gr) / 2 + 40);
            tg = Math.min(255, (tg + gg) / 2 + 40);
            tb = Math.min(255, (tb + gb) / 2 + 40);
        }
        SuperByteBuffer dome = CachedBuffers.partial(LedPartialModels.DOME, board.getBlockState());
        dome.color(tr, tg, tb, 90);
        dome.light(light);
        dome.renderInto(ms, buffer.getBuffer(RenderType.translucent()));

        if (lit) {
            // Render-only glow (no block light), additive so the three dies mix to white.
            SuperByteBuffer glow = CachedBuffers.partial(LedPartialModels.GLOW, board.getBlockState());
            glow.disableDiffuse();
            glow.color(gr, gg, gb, 255);
            glow.light(FULL_BRIGHT);
            glow.renderInto(ms, buffer.getBuffer(RenderTypes.additive()));
        }
    }
}
