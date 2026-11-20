package com.Tropfen_33.poweradditions.items;

import com.Tropfen_33.poweradditions.LedColor;
import com.Tropfen_33.poweradditions.LedRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A burnt-out white (RGB) LED. The mask says which dies (bit0 = red, bit1 = green, bit2 = blue) are broken.
 * Shift + right-click it to take it apart into red, green and blue LEDs: only the broken dies come out broken.
 */
public class BrokenWhiteLedItem extends Item {
    public BrokenWhiteLedItem(Properties properties) {
        super(properties);
    }

    public static int mask(ItemStack stack) {
        return stack.getOrDefault(LedRegistries.BROKEN_MASK.get(), 7);
    }

    public static ItemStack of(int mask) {
        ItemStack s = new ItemStack(LedRegistries.LED_WHITE_BROKEN.get());
        s.set(LedRegistries.BROKEN_MASK.get(), mask);
        return s;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        int mask = mask(stack);
        LedColor[] colors = LedColor.values(); // RED, GREEN, BLUE = bit 0, 1, 2
        if (!player.isCreative()) stack.shrink(1);
        for (int i = 0; i < 3; i++) {
            ItemStack part = new ItemStack(((mask >> i) & 1) != 0
                    ? LedRegistries.brokenItem(colors[i]) : LedRegistries.ledItem(colors[i]));
            if (!player.getInventory().add(part)) player.drop(part, false);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        int mask = mask(stack);
        LedColor[] colors = LedColor.values();
        for (int i = 0; i < 3; i++) {
            boolean broken = ((mask >> i) & 1) != 0;
            tooltip.add(Component.translatable(
                    broken ? "tooltip.poweradditions.die_broken" : "tooltip.poweradditions.die_intact",
                    Component.translatable("color.poweradditions." + colors[i].id)));
        }
        tooltip.add(Component.translatable("tooltip.poweradditions.repair_white"));
        tooltip.add(Component.translatable("tooltip.poweradditions.dismantle"));
    }
}
