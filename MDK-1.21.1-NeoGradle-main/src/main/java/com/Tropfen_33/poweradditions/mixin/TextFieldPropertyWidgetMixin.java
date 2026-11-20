package com.Tropfen_33.poweradditions.mixin;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import org.patryk3211.powergrid.circuits.gui.TextFieldPropertyWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The inner EditBox is created at a fixed screen position, so it stayed behind when the datasheet panel
 * moved this row while scrolling. Keep it at the same offset from the row as it had when it was created.
 */
@Mixin(value = TextFieldPropertyWidget.class, remap = false)
public abstract class TextFieldPropertyWidgetMixin extends AbstractSimiWidget {
    @Shadow @Final private EditBox widget;

    @Unique private int poweradditions$ox, poweradditions$oy, poweradditions$ix, poweradditions$iy;

    protected TextFieldPropertyWidgetMixin(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void poweradditions$remember(CallbackInfo ci) {
        poweradditions$ox = getX();
        poweradditions$oy = getY();
        poweradditions$ix = widget.getX();
        poweradditions$iy = widget.getY();
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    private void poweradditions$follow(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
        widget.setX(poweradditions$ix + getX() - poweradditions$ox);
        widget.setY(poweradditions$iy + getY() - poweradditions$oy);
    }
}
