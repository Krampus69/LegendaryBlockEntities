package com.krampus.legendaryblockentities;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BEDS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BELLS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_SHULKER_BOXES;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_QUARK_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BETTER_END_CHESTS;
    public static final ForgeConfigSpec.BooleanValue OPTIMIZE_BETTER_NETHER_CHESTS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();
        b.comment("Toggle which block entities to optimize. Changes require restart.");
        b.push("optimizations");

        OPTIMIZE_CHESTS = b.comment("Optimize chests, trapped chests, and ender chests.")
                .define("chests", true);
        OPTIMIZE_BEDS = b.comment("Optimize all 16 bed colors.")
                .define("beds", true);
        OPTIMIZE_BELLS = b.comment("Optimize bells.")
                .define("bells", true);
        OPTIMIZE_SHULKER_BOXES = b.comment("Optimize all shulker box variants (17 colors).")
                .define("shulker_boxes", true);
        OPTIMIZE_QUARK_CHESTS = b.comment("Optimize Quark variant chests (requires Quark to be installed).")
                .define("quark_chests", true);
        OPTIMIZE_BETTER_END_CHESTS = b.comment("Optimize BetterEnd chests (requires BetterEnd + BCLib).")
                .define("better_end_chests", true);
        OPTIMIZE_BETTER_NETHER_CHESTS = b.comment("Optimize BetterNether chests (requires BetterNether + BCLib).")
                .define("better_nether_chests", true);

        b.pop();
        SPEC = b.build();
    }

    private Config() {}

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        LegendaryBlockEntities.LOG.info("Config loaded: chests={}, beds={}, bells={}, shulkers={}, quark={}, betterend={}, betternether={}",
                OPTIMIZE_CHESTS.get(), OPTIMIZE_BEDS.get(), OPTIMIZE_BELLS.get(),
                OPTIMIZE_SHULKER_BOXES.get(), OPTIMIZE_QUARK_CHESTS.get(),
                OPTIMIZE_BETTER_END_CHESTS.get(), OPTIMIZE_BETTER_NETHER_CHESTS.get());
    }
}