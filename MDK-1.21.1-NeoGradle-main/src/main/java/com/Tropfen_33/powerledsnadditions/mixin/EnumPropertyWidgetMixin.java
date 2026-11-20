package com.Tropfen_33.powerledsnadditions.mixin;

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

    @Unique private int powerleds$ox, powerleds$oy, powerleds$wx, powerleds$wy, powerleds$lx, powerleds$ly;

    protected EnumPropertyWidgetMixin(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void powerleds$remember(CallbackInfo ci) {
        powerleds$ox = getX();
        powerleds$oy = getY();
        powerleds$wx = widget.getX();
        powerleds$wy = widget.getY();
        powerleds$lx = widgetLabel.getX();
        powerleds$ly = widgetLabel.getY();
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    private void powerleds$follow(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
        int dx = getX() - powerleds$ox;
        int dy = getY() - powerleds$oy;
        widget.setX(powerleds$wx + dx);
        widget.setY(powerleds$wy + dy);
        widgetLabel.setX(powerleds$lx + dx);
        widgetLabel.setY(powerleds$ly + dy);
    }
}
