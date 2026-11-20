package com.Tropfen_33.powerledsnadditions.items;

import com.Tropfen_33.powerledsnadditions.LedColor;
import com.Tropfen_33.powerledsnadditions.LedRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The working white (RGB) LED. Shift + right-click takes it apart into a red, a green and a blue LED. */
public class WhiteLedItem extends Item {
    public WhiteLedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) return InteractionResultHolder.pass(stack);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        if (!player.isCreative()) stack.shrink(1);
        for (LedColor c : LedColor.values()) {
            ItemStack part = new ItemStack(LedRegistries.ledItem(c));
            if (!player.getInventory().add(part)) player.drop(part, false);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.power_leds_nadditions.dismantle_working"));
    }
}
