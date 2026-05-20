package com.krampus.legendaryblockentities.client.event;

import com.krampus.legendaryblockentities.LBESetup;
import com.krampus.legendaryblockentities.LegendaryBlockEntities;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import com.krampus.legendaryblockentities.Config;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (Config.OPTIMIZE_CHESTS.get()) {
                ItemBlockRenderTypes.setRenderLayer(Blocks.CHEST, RenderType.solid());
                ItemBlockRenderTypes.setRenderLayer(Blocks.TRAPPED_CHEST, RenderType.solid());
                ItemBlockRenderTypes.setRenderLayer(Blocks.ENDER_CHEST, RenderType.solid());
                LBESetup.setupChests();
            }
            if (Config.OPTIMIZE_BEDS.get()) {
                LBESetup.setupBeds();
            }

            if (Config.OPTIMIZE_BELLS.get()) {
                LBESetup.setupBells();
            }

            if (Config.OPTIMIZE_SHULKER_BOXES.get()) {
                Block[] shulkers = {
                        Blocks.SHULKER_BOX,
                        Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX,
                        Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
                        Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
                        Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX,
                        Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX,
                        Blocks.YELLOW_SHULKER_BOX
                };
                for (Block s : shulkers) ItemBlockRenderTypes.setRenderLayer(s, RenderType.cutoutMipped());
                LBESetup.setupShulkerBoxes();
            }

            if (Config.OPTIMIZE_QUARK_CHESTS.get()) {
                try {
                    if (net.minecraftforge.fml.ModList.get().isLoaded("quark")) {
                        LBESetup.setupQuarkChests();
                    }
                } catch (Throwable t) {
                    LegendaryBlockEntities.LOG.warn("Quark chest setup failed", t);
                }
            }

            if (Config.OPTIMIZE_BETTER_END_CHESTS.get()) {
                try {
                    if (net.minecraftforge.fml.ModList.get().isLoaded("betterend")
                            && net.minecraftforge.fml.ModList.get().isLoaded("bclib")) {
                        LBESetup.setupBetterEndChests();
                    }
                } catch (Throwable t) {
                    LegendaryBlockEntities.LOG.warn("BetterEnd chest setup failed", t);
                }
            }

            if (Config.OPTIMIZE_BETTER_NETHER_CHESTS.get()) {
                try {
                    if (net.minecraftforge.fml.ModList.get().isLoaded("betternether")
                            && net.minecraftforge.fml.ModList.get().isLoaded("bclib")) {
                        LBESetup.setupBetterNetherChests();
                    }
                } catch (Throwable t) {
                    LegendaryBlockEntities.LOG.warn("BetterNether chest setup failed", t);
                }
            }
        });
    }
}
