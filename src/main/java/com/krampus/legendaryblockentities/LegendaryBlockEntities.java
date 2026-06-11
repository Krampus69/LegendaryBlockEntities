package com.krampus.legendaryblockentities;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(LegendaryBlockEntities.MOD_ID)
public final class LegendaryBlockEntities {
    public static final String MOD_ID = "legendaryblockentities";
    public static final Logger LOG = LogUtils.getLogger();

    public LegendaryBlockEntities() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
