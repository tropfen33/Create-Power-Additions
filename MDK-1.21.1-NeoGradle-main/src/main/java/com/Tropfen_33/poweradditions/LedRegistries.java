package com.Tropfen_33.poweradditions;

import com.Tropfen_33.poweradditions.components.RgbLedComponent;
import com.Tropfen_33.poweradditions.components.SingleLedComponent;
import com.Tropfen_33.poweradditions.items.BrokenWhiteLedItem;
import com.Tropfen_33.poweradditions.items.WhiteLedItem;
import com.Tropfen_33.poweradditions.recipe.WhiteLedRepairRecipe;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;

public final class LedRegistries {
    private LedRegistries() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PowerAdditions.MOD_ID);
    public static final DeferredRegister<org.patryk3211.powergrid.circuits.components.Component> COMPONENTS =
            DeferredRegister.create(ComponentRegistry.REGISTRY_KEY, PowerAdditions.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PowerAdditions.MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PowerAdditions.MOD_ID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, PowerAdditions.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<WhiteLedRepairRecipe>> WHITE_REPAIR =
            RECIPE_SERIALIZERS.register("white_led_repair", () -> new SimpleCraftingRecipeSerializer<>(WhiteLedRepairRecipe::new));

    /** Which dies of a broken white LED are burnt (bit0 red, bit1 green, bit2 blue). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> BROKEN_MASK =
            DATA_COMPONENTS.register("broken_mask", () -> DataComponentType.<Integer>builder()
                    .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT).build());

    // Working LEDs (only usable on circuit boards / for repairs; they are not blocks)
    public static final DeferredItem<Item> LED_RED   = ITEMS.registerSimpleItem("led_red");
    public static final DeferredItem<Item> LED_GREEN = ITEMS.registerSimpleItem("led_green");
    public static final DeferredItem<Item> LED_BLUE  = ITEMS.registerSimpleItem("led_blue");
    public static final DeferredItem<WhiteLedItem> LED_WHITE =
            ITEMS.register("led_white", () -> new WhiteLedItem(new Item.Properties()));

    // Burnt-out LEDs
    public static final DeferredItem<Item> LED_RED_BROKEN   = ITEMS.registerSimpleItem("led_red_broken");
    public static final DeferredItem<Item> LED_GREEN_BROKEN = ITEMS.registerSimpleItem("led_green_broken");
    public static final DeferredItem<Item> LED_BLUE_BROKEN  = ITEMS.registerSimpleItem("led_blue_broken");
    public static final DeferredItem<BrokenWhiteLedItem> LED_WHITE_BROKEN =
            ITEMS.register("led_white_broken", () -> new BrokenWhiteLedItem(new Item.Properties().stacksTo(16)));

    /** Burnt-out versions of Powergrid's own components, keyed by component name (no recipes for now). */
    public static final Map<String, DeferredItem<Item>> BROKEN_COMPONENT_ITEMS = new LinkedHashMap<>();
    static {
        for (String name : BrokenComponents.ITEM_NAMES) {
            BROKEN_COMPONENT_ITEMS.put(name, ITEMS.registerSimpleItem(name + "_broken"));
        }
    }

    public static Item ledItem(LedColor c) {
        return switch (c) {
            case RED -> LED_RED.get();
            case GREEN -> LED_GREEN.get();
            case BLUE -> LED_BLUE.get();
        };
    }

    public static Item brokenItem(LedColor c) {
        return switch (c) {
            case RED -> LED_RED_BROKEN.get();
            case GREEN -> LED_GREEN_BROKEN.get();
            case BLUE -> LED_BLUE_BROKEN.get();
        };
    }

    // Circuit board components (ids must equal the item ids, see data/poweradditions/powergrid/component_items)
    public static final DeferredHolder<org.patryk3211.powergrid.circuits.components.Component, SingleLedComponent> RED =
            COMPONENTS.register("led_red", () -> new SingleLedComponent(LedColor.RED));
    public static final DeferredHolder<org.patryk3211.powergrid.circuits.components.Component, SingleLedComponent> GREEN =
            COMPONENTS.register("led_green", () -> new SingleLedComponent(LedColor.GREEN));
    public static final DeferredHolder<org.patryk3211.powergrid.circuits.components.Component, SingleLedComponent> BLUE =
            COMPONENTS.register("led_blue", () -> new SingleLedComponent(LedColor.BLUE));
    public static final DeferredHolder<org.patryk3211.powergrid.circuits.components.Component, RgbLedComponent> WHITE =
            COMPONENTS.register("led_white", RgbLedComponent::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.poweradditions"))
                    .icon(() -> LED_WHITE.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(LED_RED.get());
                        out.accept(LED_GREEN.get());
                        out.accept(LED_BLUE.get());
                        out.accept(LED_WHITE.get());
                        out.accept(LED_RED_BROKEN.get());
                        out.accept(LED_GREEN_BROKEN.get());
                        out.accept(LED_BLUE_BROKEN.get());
                        out.accept(BrokenWhiteLedItem.of(7));
                        BROKEN_COMPONENT_ITEMS.values().forEach(i -> out.accept(i.get()));
                    })
                    .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
    }
}
