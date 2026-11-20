package com.Tropfen_33.poweradditions.mixin;

import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.SelectionScrollInput;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.patryk3211.powergrid.circuits.gui.EnumPropertyWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Same fix as for the text fields: the selector and its label follow the row when the panel scrolls. */
@Mixin(value = EnumPropertyWidget.class, remap = false)
public abstract class EnumPropertyWidgetMixin extends AbstractSimiWidget {
    @Shadow @Final private SelectionScrollInput widget;
    @Shadow @Final private Label widgetLabel;

    @Unique private int poweradditions$ox, poweradditions$oy, poweradditions$wx, poweradditions$wy, poweradditions$lx, poweradditions$ly;

    protected EnumPropertyWidgetMixin(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void poweradditions$remember(CallbackInfo ci) {
        poweradditions$ox = getX();
        poweradditions$oy = getY();
        poweradditions$wx = widget.getX();
        poweradditions$wy = widget.getY();
        poweradditions$lx = widgetLabel.getX();
        poweradditions$ly = widgetLabel.getY();
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    private void poweradditions$follow(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
        int dx = getX() - poweradditions$ox;
        int dy = getY() - poweradditions$oy;
        widget.setX(poweradditions$wx + dx);
        widget.setY(poweradditions$wy + dy);
        widgetLabel.setX(poweradditions$lx + dx);
        widgetLabel.setY(poweradditions$ly + dy);
    }
}
