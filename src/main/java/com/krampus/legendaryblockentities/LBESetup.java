package com.krampus.legendaryblockentities;

import com.krampus.legendaryblockentities.client.event.ModelEvents;
import com.krampus.legendaryblockentities.client.render.BellBlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.ChestBlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.ShulkerBoxBlockEntityRendererOverride;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.function.Function;
import java.util.function.Supplier;

public final class LBESetup {
    private LBESetup() {}

    public static void setupChests() {
        Function<BlockEntity, Integer> chestTypeSelector = be -> {
            BlockState s = be.getBlockState();
            if (s.hasProperty(ChestBlock.TYPE)) {
                ChestType t = s.getValue(ChestBlock.TYPE);
                return t == ChestType.LEFT ? 1 : t == ChestType.RIGHT ? 2 : 0;
            }
            return 0;
        };
        Function<BlockEntity, Integer> singleModel = be -> 0;

        Supplier<BakedModel[]> chestSupplier = () -> new BakedModel[]{
                or(ModelEvents.chestLidModel), or(ModelEvents.chestLeftLidModel), or(ModelEvents.chestRightLidModel)};
        Supplier<BakedModel[]> trappedSupplier = () -> new BakedModel[]{
                or(ModelEvents.trappedChestLidModel), or(ModelEvents.trappedChestLeftLidModel), or(ModelEvents.trappedChestRightLidModel)};
        Supplier<BakedModel[]> enderSupplier = () -> new BakedModel[]{or(ModelEvents.enderChestLidModel)};

        LegendaryBlockEntityRegistry.register(Blocks.CHEST, BlockEntityType.CHEST,
                BlockEntityRenderCondition.CHEST,
                new ChestBlockEntityRendererOverride(chestSupplier, chestTypeSelector));
        LegendaryBlockEntityRegistry.register(Blocks.TRAPPED_CHEST, BlockEntityType.TRAPPED_CHEST,
                BlockEntityRenderCondition.CHEST,
                new ChestBlockEntityRendererOverride(trappedSupplier, chestTypeSelector));
        LegendaryBlockEntityRegistry.register(Blocks.ENDER_CHEST, BlockEntityType.ENDER_CHEST,
                BlockEntityRenderCondition.CHEST,
                new ChestBlockEntityRendererOverride(enderSupplier, singleModel));
    }

    public static void setupBells() {
        LegendaryBlockEntityRegistry.register(
                Blocks.BELL, BlockEntityType.BELL,
                BlockEntityRenderCondition.BELL,
                new BellBlockEntityRendererOverride()
        );
    }

    public static void setupShulkerBoxes() {
        ShulkerBoxBlockEntityRendererOverride renderer = new ShulkerBoxBlockEntityRendererOverride();
        Block[] boxes = {
                Blocks.SHULKER_BOX,
                Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX,
                Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX,
                Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX,
                Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX,
                Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX,
                Blocks.YELLOW_SHULKER_BOX
        };
        for (Block block : boxes) {
            LegendaryBlockEntityRegistry.register(
                    block, BlockEntityType.SHULKER_BOX,
                    BlockEntityRenderCondition.SHULKER_BOX,
                    renderer
            );
        }
    }

    public static void setupBeds() {
        Block[] beds = {
                Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED,
                Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED,
                Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED,
                Blocks.PURPLE_BED, Blocks.RED_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED
        };
        for (Block bed : beds) {
            LegendaryBlockEntityRegistry.register(
                    bed, BlockEntityType.BED,
                    BlockEntityRenderCondition.NEVER,
                    BlockEntityRendererOverride.NO_OP
            );
        }
    }

    private static BakedModel or(BakedModel m) {
        return m != null ? m : Minecraft.getInstance().getModelManager().getBlockModelShaper()
                .getBlockModel(Blocks.STONE.defaultBlockState());
    }
}
