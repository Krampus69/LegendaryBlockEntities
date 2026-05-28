package com.krampus.legendaryblockentities.client;

import com.krampus.legendaryblockentities.Config;
import com.krampus.legendaryblockentities.LegendaryBlockEntities;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;

/**
 * Detects whether Vanillin (Flywheel-based instanced rendering) is active and handling the
 * vanilla block entities that LBE also optimizes. When it is, LBE backs off those vanilla
 * types to avoid double-rendering, while STILL optimizing modded chests (Quark/BetterEnd/
 * BetterNether/Iron), which Vanillin does not cover.
 *
 * Vanillin only registers visuals for vanilla CHEST, TRAPPED_CHEST, ENDER_CHEST, BELL and
 * SHULKER_BOX (by BlockEntityType identity), so only those are deferred.
 *
 * The decision is made once at client setup. If the user toggles Flywheel's backend at
 * runtime, a restart is required for LBE to re-evaluate (documented in the config comment).
 */
public final class VanillinCompat {
    private VanillinCompat() {}

    private static Boolean cached = null;

    /**
     * @return true if LBE should SKIP the vanilla block entities because Vanillin/Flywheel
     *         is present and its rendering backend is on.
     */
    public static boolean shouldDeferVanillaToVanillin() {
        if (cached != null) return cached;
        cached = compute();
        return cached;
    }

    private static boolean compute() {
        // Honor explicit user override first.
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

        // AUTO (default): defer only if Vanillin is loaded AND Flywheel's backend is on.
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
            // Flywheel API not present/changed — be safe and don't defer.
            LegendaryBlockEntities.LOG.warn("Could not query Flywheel backend state: {}", t.toString());
            return false;
        }
    }
}