package com.Tropfen_33.poweradditions.mixin;

import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.patryk3211.powergrid.circuits.gui.ComponentPropertiesWidget;
import org.patryk3211.powergrid.circuits.gui.PropertyWidget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Makes Powergrid's component "datasheet" panel scrollable for every component:
 *  - mouse wheel over the panel      = scroll rows up/down
 *  - Shift + mouse wheel (or sideways wheel) = scroll left/right
 * Rows outside the visible window are moved off screen; the panel height is limited to the GUI height.
 */
@Mixin(value = ComponentPropertiesWidget.class, remap = false)
public abstract class ComponentPropertiesWidgetMixin extends AbstractSimiWidget {
    @Shadow @Final private List<PropertyWidget<?, ?>> propertyWidgets;
    @Shadow @Final private int right;
    @Shadow private int propertyCount;

    /** Every row of the current component; the panel's own list only holds the rows in the visible window. */
    @Unique private final List<PropertyWidget<?, ?>> poweradditions$all = new ArrayList<>();
    @Unique private int poweradditions$scrollRow = 0;
    @Unique private int poweradditions$scrollX = 0;
    @Unique private int poweradditions$maxScrollRow = 0;
    @Unique private int poweradditions$maxScrollX = 0;
    @Unique private int poweradditions$total = 0;

    protected ComponentPropertiesWidgetMixin(int x, int y, int w, int h) {
        super(x, y, w, h);
    }

    @Unique
    private void poweradditions$layout() {
        if (poweradditions$all.isEmpty() && !propertyWidgets.isEmpty()) {
            poweradditions$all.addAll(propertyWidgets);
        }
        int total = poweradditions$all.size();
        poweradditions$total = total;
        if (total == 0) {
            poweradditions$maxScrollRow = 0;
            poweradditions$maxScrollX = 0;
            return;
        }
        int guiH = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int panelY = getY();
        int maxRows = Math.max(4, (guiH - panelY - 16 - 6 - 6) / 20);
        int visible = Math.min(total, maxRows);
        poweradditions$maxScrollRow = total - visible;
        poweradditions$scrollRow = Math.max(0, Math.min(poweradditions$scrollRow, poweradditions$maxScrollRow));

        int panelW = getWidth();
        poweradditions$maxScrollX = Math.max(0, panelW - right);
        poweradditions$scrollX = Math.max(0, Math.min(poweradditions$scrollX, poweradditions$maxScrollX));
        setX(right - panelW + poweradditions$scrollX);
        int valueX = right - 60 + poweradditions$scrollX;

        // The panel draws labels, values, tooltips and its footer from its own row list, so that list only
        // contains the visible rows, each placed directly below the previous one (labels and value boxes
        // therefore always move together).
        propertyWidgets.clear();
        for (int i = poweradditions$scrollRow; i < poweradditions$scrollRow + visible; i++) {
            PropertyWidget<?, ?> w = poweradditions$all.get(i);
            w.setX(valueX);
            w.setY(panelY + 16 + (i - poweradditions$scrollRow) * 20);
            propertyWidgets.add(w);
        }
        propertyCount = visible;
    }

    @Inject(method = "setComponent(Lorg/patryk3211/powergrid/circuits/schematic/PlacedComponent;Ljava/lang/Runnable;)V",
            at = @At("RETURN"))
    private void poweradditions$afterSetComponent(CallbackInfo ci) {
        poweradditions$all.clear();
        poweradditions$all.addAll(propertyWidgets);
        poweradditions$scrollRow = 0;
        poweradditions$scrollX = 0;
        poweradditions$layout();
    }

    @Inject(method = "doRender", at = @At("HEAD"))
    private void poweradditions$beforeRender(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
        poweradditions$layout();
        // draw the whole panel in front of the schematic components (they are rendered with a high z offset)
        g.pose().pushPose();
        g.pose().translate(0, 0, 500);
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    private void poweradditions$afterRender(GuiGraphics g, int mx, int my, float pt, CallbackInfo ci) {
        poweradditions$drawScrollbars(g);
        g.pose().popPose();
    }

    @Unique
    private void poweradditions$drawScrollbars(GuiGraphics g) {
        if (poweradditions$total == 0) return;
        int panelW = getWidth();
        int trackTop = getY() + 16;
        int trackH = propertyCount * 20;
        if (poweradditions$maxScrollRow > 0) {
            int thumbH = Math.max(8, trackH * propertyCount / poweradditions$total);
            int thumbY = trackTop + (trackH - thumbH) * poweradditions$scrollRow / poweradditions$maxScrollRow;
            int x = getX() + panelW - 4;
            g.fill(x, trackTop, x + 2, trackTop + trackH, 0x55000000);
            g.fill(x, thumbY, x + 2, thumbY + thumbH, 0xCCFFFFFF);
        }
        if (poweradditions$maxScrollX > 0) {
            int trackW = panelW - 12;
            int thumbW = Math.max(8, trackW * right / panelW);
            int thumbX = getX() + 6 + (trackW - thumbW) * (poweradditions$maxScrollX - poweradditions$scrollX) / poweradditions$maxScrollX;
            int y = getY() + 16 + trackH + 1;
            g.fill(getX() + 6, y, getX() + 6 + trackW, y + 2, 0x55000000);
            g.fill(thumbX, y, thumbX + thumbW, y + 2, 0xCCFFFFFF);
        }
    }

    @Inject(method = "mouseScrolled(DDDD)Z", at = @At("HEAD"), cancellable = true)
    private void poweradditions$scroll(double mx, double my, double sx, double sy, CallbackInfoReturnable<Boolean> cir) {
        if (propertyWidgets.isEmpty() && poweradditions$all.isEmpty()) return;
        poweradditions$layout();
        boolean vert = poweradditions$maxScrollRow > 0;
        boolean horiz = poweradditions$maxScrollX > 0;
        if (!vert && !horiz) return; // nothing to scroll: keep original behaviour

        int h = 16 + propertyCount * 20 + 6;
        if (mx < getX() || mx > getX() + getWidth() || my < getY() || my > getY() + h) return;

        double dx = sx, dy = sy;
        if (Screen.hasShiftDown() && dx == 0) {
            dx = dy;
            dy = 0;
        }
        if (horiz && dx != 0) {
            poweradditions$scrollX += (int) Math.signum(dx) * 16;
        } else if (vert && dy != 0) {
            poweradditions$scrollRow -= (int) Math.signum(dy);
        } else {
            return;
        }
        poweradditions$layout();
        cir.setReturnValue(true);
    }
}
