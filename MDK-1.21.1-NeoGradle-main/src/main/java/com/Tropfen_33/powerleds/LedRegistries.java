package com.Tropfen_33.powerleds;

import com.Tropfen_33.powerleds.components.RgbLedComponent;
import com.Tropfen_33.powerleds.components.SingleLedComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.patryk3211.powergrid.circuits.components.ComponentRegistry;

public final class LedRegistries {
    private LedRegistries() {}

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PowerLeds.MOD_ID);
    public static final DeferredRegister<org.patryk3211.powergrid.circuits.components.Component> COMPONENTS =
            DeferredRegister.create(ComponentRegistry.REGISTRY_KEY, PowerLeds.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PowerLeds.MOD_ID);

    // Items (only usable on circuit boards / in crafting; they are not blocks)
    public static final DeferredItem<Item> LED_RED   = ITEMS.registerSimpleItem("led_red");
    public static final DeferredItem<Item> LED_GREEN = ITEMS.registerSimpleItem("led_green");
    public static final DeferredItem<Item> LED_BLUE  = ITEMS.registerSimpleItem("led_blue");
    public static final DeferredItem<Item> LED_WHITE = ITEMS.registerSimpleItem("led_white");

    // Circuit board components (ids must equal the item ids, see data/power_leds/powergrid/component_items)
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
                    .title(Component.translatable("itemGroup.power_leds"))
                    .icon(() -> LED_WHITE.get().getDefaultInstance())
                    .displayItems((params, out) -> {
                        out.accept(LED_RED.get());
                        out.accept(LED_GREEN.get());
                        out.accept(LED_BLUE.get());
                        out.accept(LED_WHITE.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        COMPONENTS.register(bus);
        TABS.register(bus);
    }
}
