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

    public static void setupBetterEndChests() {
        setupBclibChests("betterend", new String[]{
                "lucernia_chest", "dragon_tree_chest", "end_lotus_chest", "helix_tree_chest",
                "jellyshroom_chest", "lacugrove_chest", "mossy_glowshroom_chest",
                "pythadendron_chest", "tenanea_chest", "umbrella_tree_chest"
        });
    }

    public static void setupBetterNetherChests() {
        setupBclibChests("betternether", new String[]{
                "anchor_tree_chest", "crimson_chest", "mushroom_fir_chest", "mushroom_fir_trimmed_chest",
                "nether_mushroom_chest", "nether_reed_chest", "nether_sakura_chest", "rubeus_chest",
                "stalagnate_chest", "warped_chest", "wart_chest", "willow_chest"
        });
    }
    private static BakedModel lookupBetterEnd(String name) {
        BakedModel m = ModelEvents.betterEndChestLids.get(name);
        return m != null ? m : Minecraft.getInstance().getModelManager().getBlockModelShaper()
                .getBlockModel(Blocks.STONE.defaultBlockState());
    }
    private static void setupBclibChests(String namespace, String[] names) {
        var forgeReg = net.minecraftforge.registries.ForgeRegistries.BLOCKS;
        BlockEntityType<?> beType;
        try {
            Class<?> cls = Class.forName("org.betterx.bclib.registry.BaseBlockEntities");
            beType = (BlockEntityType<?>) cls.getField("CHEST").get(null);
        } catch (ReflectiveOperationException e) {
            LegendaryBlockEntities.LOG.warn("Could not resolve BCLib chest BE type: {}", e.getMessage());
            return;
        }
        if (beType == null) return;

        Function<BlockEntity, Integer> typeSelector = be -> {
            BlockState s = be.getBlockState();
            if (s.hasProperty(ChestBlock.TYPE)) {
                ChestType t = s.getValue(ChestBlock.TYPE);
                return t == ChestType.LEFT ? 1 : t == ChestType.RIGHT ? 2 : 0;
            }
            return 0;
        };

        int registered = 0;
        for (String name : names) {
            Block block = forgeReg.getValue(new net.minecraft.resources.ResourceLocation(namespace, name));
            if (!(block instanceof ChestBlock)) continue;

            final String key = name;
            Supplier<BakedModel[]> supplier = () -> new BakedModel[]{
                    lookupBetterEnd(key),
                    lookupBetterEnd(key + "_left"),
                    lookupBetterEnd(key + "_right")
            };

            LegendaryBlockEntityRegistry.register(
                    block, beType,
                    BlockEntityRenderCondition.CHEST,
                    new ChestBlockEntityRendererOverride(supplier, typeSelector)
            );
            registered++;
        }
        LegendaryBlockEntities.LOG.info("Registered {} {} chest variants", registered, namespace);
    }

    public static void setupQuarkChests() {
        Function<BlockEntity, Integer> chestTypeSelector = be -> {
            BlockState s = be.getBlockState();
            if (s.hasProperty(ChestBlock.TYPE)) {
                ChestType t = s.getValue(ChestBlock.TYPE);
                return t == ChestType.LEFT ? 1 : t == ChestType.RIGHT ? 2 : 0;
            }
            return 0;
        };

        var forgeReg = net.minecraftforge.registries.ForgeRegistries.BLOCKS;
        int registered = 0;

        for (var entry : forgeReg.getEntries()) {
            var key = entry.getKey().location();
            if (!"quark".equals(key.getNamespace())) continue;
            String path = key.getPath();
            if (!path.endsWith("_chest") && !path.endsWith("_trapped_chest")) continue;
            if (path.startsWith("lootr_")) continue;

            Block block = entry.getValue();
            if (!(block instanceof ChestBlock)) continue;

            // Look up the BE type via the actual block's expected type — Quark uses a shared one per category.
            boolean trapped = path.endsWith("_trapped_chest");
            BlockEntityType<?> beType;
            try {
                // Quark exposes the BE types statically. We try reflection to avoid a hard dep.
                Class<?> moduleCls = Class.forName("org.violetmoon.quark.content.building.module.VariantChestsModule");
                beType = (BlockEntityType<?>) moduleCls.getField(trapped ? "trappedChestTEType" : "chestTEType").get(null);
            } catch (ReflectiveOperationException e) {
                LegendaryBlockEntities.LOG.warn("Could not resolve Quark BE type for {}: {}", key, e.getMessage());
                continue;
            }
            if (beType == null) continue;

            String prefix = path;

            Supplier<BakedModel[]> supplier = () -> new BakedModel[]{
                    lookupQuark(prefix + "_center"),
                    lookupQuark(prefix + "_left"),
                    lookupQuark(prefix + "_right")
            };

            LegendaryBlockEntityRegistry.register(
                    block, beType,
                    BlockEntityRenderCondition.CHEST,
                    new ChestBlockEntityRendererOverride(supplier, chestTypeSelector)
            );
            registered++;
        }
        LegendaryBlockEntities.LOG.info("Registered {} Quark chest variants", registered);
    }

    private static BakedModel lookupQuark(String modelName) {
        BakedModel m = ModelEvents.quarkChestLids.get(modelName);
        return m != null ? m : Minecraft.getInstance().getModelManager().getBlockModelShaper()
                .getBlockModel(Blocks.STONE.defaultBlockState());
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