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

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(Blocks.CHEST, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(Blocks.TRAPPED_CHEST, RenderType.solid());
            ItemBlockRenderTypes.setRenderLayer(Blocks.ENDER_CHEST, RenderType.solid());

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

            LBESetup.setupChests();
            LBESetup.setupBeds();
            LBESetup.setupBells();
            LBESetup.setupShulkerBoxes();
        });
    }
}
