package com.Tropfen_33.powerledsnadditions.recipe;

import com.Tropfen_33.powerledsnadditions.LedRegistries;
import com.Tropfen_33.powerledsnadditions.items.BrokenWhiteLedItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Repairs a burnt-out white LED: the broken LED + 1 quartz + the ore of every die that burnt
 * (redstone = red, emerald = green, diamond = blue). The result is a working white LED.
 */
public class WhiteLedRepairRecipe extends CustomRecipe {
    public WhiteLedRepairRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack broken = ItemStack.EMPTY;
        int quartz = 0, redstone = 0, emerald = 0, diamond = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack s = input.getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(LedRegistries.LED_WHITE_BROKEN.get())) {
                if (!broken.isEmpty()) return false;
                broken = s;
            } else if (s.is(Items.QUARTZ)) quartz++;
            else if (s.is(Items.REDSTONE)) redstone++;
            else if (s.is(Items.EMERALD)) emerald++;
            else if (s.is(Items.DIAMOND)) diamond++;
            else return false;
        }
        if (broken.isEmpty()) return false;
        int mask = BrokenWhiteLedItem.mask(broken);
        return quartz == 1
                && redstone == (mask & 1)
                && emerald == ((mask >> 1) & 1)
                && diamond == ((mask >> 2) & 1);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return new ItemStack(LedRegistries.LED_WHITE.get());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return LedRegistries.WHITE_REPAIR.get();
    }
}
