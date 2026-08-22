package com.krampus.legendaryblockentities;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BEDS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BELLS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_SHULKER_BOXES;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_SIGNS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_QUARK_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BETTER_END_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BETTER_NETHER_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_IRON_CHESTS;
    public static final ForgeConfigSpec.ConfigValue<String> VANILLIN_COMPAT_MODE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Toggle which block entities to optimize. Do not change param in this config with the game open");
        b.push("optimizations");

        OPTIMIZE_CHESTS = b.comment("Optimize chests, trapped chests, and ender chests")
                .define("chests", true);
        OPTIMIZE_BEDS = b.comment("Optimize all 16 bed colors")
                .define("beds", true);
        OPTIMIZE_BELLS = b.comment("Optimize bells")
                .define("bells", true);
        OPTIMIZE_SHULKER_BOXES = b.comment("Optimize all shulker box variants (17 colors)")
                .define("shulker_boxes", true);
        OPTIMIZE_SIGNS = b.comment("Optimize standing and wall signs (all 11 vanilla wood types). Hanging signs are not affected")
                .define("signs", true);
        OPTIMIZE_QUARK_CHESTS = b.comment("Optimize Quark variant chests (requires Quark to be installed)")
                .define("quark_chests", true);
        OPTIMIZE_BETTER_END_CHESTS = b.comment("Optimize BetterEnd chests (requires BetterEnd + BCLib)")
                .define("better_end_chests", true);
        OPTIMIZE_BETTER_NETHER_CHESTS = b.comment("Optimize BetterNether chests (requires BetterNether + BCLib)")
                .define("better_nether_chests", true);
        OPTIMIZE_IRON_CHESTS = b.comment("Optimize Iron Chests (requires Iron Chests). Crystal chests are excluded")
                .define("iron_chests", true);

        b.pop();
        b.push("compatibility");

        VANILLIN_COMPAT_MODE = b.comment(
                        "AUTO: if Vanillin is loaded and Flywheel backend is on, Vanillin handles vanilla chests, bells and shulkers while LBE keeps optimizing modded chests",
                        "ALWAYS_LBE: ignore Vanillin and let LBE handle everything. This can double-render if Vanillin is active",
                        "DEFER_TO_VANILLIN: if Vanillin is loaded at all, always hand vanilla types to it",
                        "If you switch Flywheel backend in-game, restart for LBE to re-evaluate")
                .define("vanillin_compat_mode", "AUTO",
                        o -> o instanceof String s &&
                                List.of("AUTO", "ALWAYS_LBE", "DEFER_TO_VANILLIN").contains(s.toUpperCase()));

        b.pop();
        SPEC = b.build();
    }

    private Config() {}

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        LegendaryBlockEntities.LOG.info("Config loaded: chests={}, beds={}, bells={}, shulkers={}, signs={}, quark={}, betterend={}, betternether={}, iron={}, vanillinCompat={}",
                OPTIMIZE_CHESTS.get(), OPTIMIZE_BEDS.get(), OPTIMIZE_BELLS.get(),
                OPTIMIZE_SHULKER_BOXES.get(), OPTIMIZE_SIGNS.get(), OPTIMIZE_QUARK_CHESTS.get(),
                OPTIMIZE_BETTER_END_CHESTS.get(), OPTIMIZE_BETTER_NETHER_CHESTS.get(),
                OPTIMIZE_IRON_CHESTS.get(), VANILLIN_COMPAT_MODE.get());
    }
}
