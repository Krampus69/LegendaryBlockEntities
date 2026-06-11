package com.krampus.legendaryblockentities.client;

import com.krampus.legendaryblockentities.Config;
import com.krampus.legendaryblockentities.LegendaryBlockEntities;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

public final class VanillinCompat {
    private VanillinCompat() {}

    private static Boolean cached = null;

    public static boolean shouldDeferVanillaToVanillin() {
        if (cached != null) return cached;
        cached = compute();
        return cached;
    }

    private static boolean compute() {

        String mode = Config.VANILLIN_COMPAT_MODE.get();
        if ("ALWAYS_LBE".equalsIgnoreCase(mode)) {
            LegendaryBlockEntities.LOG.info("Vanillin compat: ALWAYS_LBE — LBE handles all block entities.");
            return false;
        }
        if ("DEFER_TO_VANILLIN".equalsIgnoreCase(mode)) {
            boolean present = ModList.get().isLoaded("vanillin");
            LegendaryBlockEntities.LOG.info("Vanillin compat: DEFER_TO_VANILLIN (vanillin loaded={}).", present);
            return present;
        }

        if (!ModList.get().isLoaded("vanillin")) {
            return false;
        }
        boolean backendOn = isFlywheelBackendOn();
        if (backendOn) {
            LegendaryBlockEntities.LOG.info(
                    "Vanillin detected with Flywheel backend ON — deferring vanilla chests/bells/shulkers to Vanillin. "
                            + "LBE still optimizes modded chests.");
        } else {
            LegendaryBlockEntities.LOG.info(
                    "Vanillin present but Flywheel backend is OFF — LBE will handle vanilla block entities itself.");
        }
        return backendOn;
    }

    private static boolean isFlywheelBackendOn() {
        try {
            Class<?> bm = Class.forName("dev.engine_room.flywheel.api.backend.BackendManager");
            Method isOn = bm.getMethod("isBackendOn");
            Object result = isOn.invoke(null);
            return result instanceof Boolean b && b;
        } catch (Throwable t) {

            LegendaryBlockEntities.LOG.warn("Could not query Flywheel backend state: {}", t.toString());
            return false;
        }
    }
}
