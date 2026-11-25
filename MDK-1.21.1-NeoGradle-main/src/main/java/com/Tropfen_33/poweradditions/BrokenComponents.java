package com.Tropfen_33.poweradditions;

import java.util.List;
import java.util.Set;

/** Burnt-out variants of Powergrid's own circuit board components (generated lists). */
public final class BrokenComponents {
    private BrokenComponents() {}

    /** Components that get a burnt-out ITEM (they have their own item sprite): item id is "<name>_broken". */
    public static final List<String> ITEM_NAMES = List.of(
            "barretter_tube",
            "bjt_npn",
            "bjt_pnp",
            "diode",
            "display_module",
            "neon_bulb",
            "pentode",
            "potentiometer",
            "regulator_tube",
            "resistor",
            "thyratron",
            "triode",
            "varistor",
            "vfet",
            "switch",
            "button",
            "light_bulb");

    /** Powergrid board models that have a burnt 3D model: model id is "<name>_broken" in this mod's namespace. */
    public static final Set<String> MODEL_NAMES = Set.of(
            "barretter_tube",
            "bjt_npn",
            "bjt_pnp",
            "button",
            "button_on",
            "diode",
            "diode_vertical",
            "display_module",
            "light_bulb",
            "neon_bulb",
            "neon_bulb_vertical",
            "pentode",
            "potentiometer",
            "regulator_tube",
            "resistor",
            "resistor_vertical",
            "switch",
            "switch_on",
            "thyratron",
            "triode",
            "varistor",
            "vfet");

    public static boolean hasModel(String namespace, String path) {
        return "powergrid".equals(namespace) && MODEL_NAMES.contains(path);
    }
}
