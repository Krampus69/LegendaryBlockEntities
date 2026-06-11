package com.krampus.legendaryblockentities;

import com.krampus.legendaryblockentities.client.event.ModelEvents;
import com.krampus.legendaryblockentities.client.render.BellBlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.ChestBlockEntityRendererOverride;
import com.krampus.legendaryblockentities.client.render.ShulkerBoxBlockEntityRendererOverride;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

public final class LBESetup {
    private LBESetup() {}

    private static final String[] BETTER_END_NAMES = {
            "lucernia_chest", "dragon_tree_chest", "end_lotus_chest", "helix_tree_chest",
            "jellyshroom_chest", "lacugrove_chest", "mossy_glowshroom_chest",
            "pythadendron_chest", "tenanea_chest", "umbrella_tree_chest"
    };
    private static final String[] BETTER_NETHER_NAMES = {
            "anchor_tree_chest", "crimson_chest", "mushroom_fir_chest", "mushroom_fir_trimmed_chest",
            "nether_mushroom_chest", "nether_reed_chest", "nether_sakura_chest", "rubeus_chest",
            "stalagnate_chest", "warped_chest", "wart_chest", "willow_chest"
    };
    private static final String[] IRON_CHEST_NAMES = {
            "iron_chest", "gold_chest", "diamond_chest", "copper_chest", "obsidian_chest", "dirt_chest",
            "trapped_iron_chest", "trapped_gold_chest", "trapped_diamond_chest",
            "trapped_copper_chest", "trapped_obsidian_chest", "trapped_dirt_chest"
    };

    /**
     * Guards {@link #ensureDynamicBindings()} so the registry scan runs at most once
     * (unless it fails, in which case it is allowed to retry on a later reload).
     */
    private static final AtomicBoolean DYNAMIC_BINDINGS_DONE = new AtomicBoolean(false);

    /**
     * Populate {@link LegendaryBlockEntityRegistry#DYNAMIC_INJECT} independently of
     * client-setup timing.
     *
     * Under ModernFix the model bake can run on a worker thread BEFORE the
     * FMLClientSetupEvent enqueued work that registers the chest overrides. If we relied
     * on that work to populate the bindings, the body-injection in
     * {@code ModelEvents.onModifyBakingResult} could find an empty map and render nothing.
     * Calling this from the bake event guarantees the bindings exist whenever injection
     * runs. The block registry is frozen by bake time, so the scan is safe off-thread.
     */
    public static void ensureDynamicBindings() {
        if (!DYNAMIC_BINDINGS_DONE.compareAndSet(false, true)) return;
        try {
            ModList mods = ModList.get();

            if (Config.OPTIMIZE_QUARK_CHESTS.get() && mods.isLoaded("quark")) {
                bindQuarkDynamicModels();
            }
            if (Config.OPTIMIZE_IRON_CHESTS.get() && mods.isLoaded("ironchest")) {
                bindIronDynamicModels();
            }
            if (Config.OPTIMIZE_BETTER_END_CHESTS.get() && mods.isLoaded("betterend") && mods.isLoaded("bclib")) {
                bindBclibDynamicModels("betterend", BETTER_END_NAMES);
            }
            if (Config.OPTIMIZE_BETTER_NETHER_CHESTS.get() && mods.isLoaded("betternether") && mods.isLoaded("bclib")) {
                bindBclibDynamicModels("betternether", BETTER_NETHER_NAMES);
            }
        } catch (Throwable t) {
            LegendaryBlockEntities.LOG.warn("ensureDynamicBindings failed", t);
            DYNAMIC_BINDINGS_DONE.set(false);
        }
    }

    private static void bindQuarkDynamicModels() {
        int boundCount = 0;
        for (var entry : ForgeRegistries.BLOCKS.getEntries()) {
            var key = entry.getKey().location();
            if (!"quark".equals(key.getNamespace())) continue;
            String path = key.getPath();
            if (!path.endsWith("_chest") && !path.endsWith("_trapped_chest")) continue;
            if (path.startsWith("lootr_")) continue;

            Block block = entry.getValue();
            if (!(block instanceof ChestBlock)) continue;

            final String basePath = path;
            LegendaryBlockEntityRegistry.bindDynamicModel(block, state -> {
                String half = "center";
                if (state.hasProperty(ChestBlock.TYPE)) {
                    ChestType t = state.getValue(ChestBlock.TYPE);
                    half = t == ChestType.LEFT ? "left" : t == ChestType.RIGHT ? "right" : "center";
                }
                return new ResourceLocation("quark", "block/" + basePath + "_" + half);
            });
            boundCount++;
        }
        LegendaryBlockEntities.LOG.info("Bound {} Quark chest blocks for dynamic body injection", boundCount);
    }

    /** Iron Chests: full/trunk geometry is authored 180-rotated, so rotate by facing + 180. */
    private static void bindIronDynamicModels() {
        int boundCount = 0;
        for (String name : IRON_CHEST_NAMES) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ironchest", name));
            if (block == null) continue;
            final String modelPath = "block/" + name + "_lbe";
            LegendaryBlockEntityRegistry.bindDynamicModel(block,
                    state -> new ResourceLocation("ironchest", modelPath),
                    true, 180 /* +180 cancels the pre-rotated ic_ full/trunk templates */);
            boundCount++;
        }
        LegendaryBlockEntities.LOG.info("Bound {} Iron Chests blocks for dynamic body injection", boundCount);
    }

    /** BetterEnd / BetterNether: per-facing rotation, base differs by ChestType (_lbe / _left_lbe / _right_lbe). */
    private static void bindBclibDynamicModels(String namespace, String[] names) {
        int boundCount = 0;
        for (String name : names) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, name));
            if (!(block instanceof ChestBlock)) continue;

            final String baseName = name;
            LegendaryBlockEntityRegistry.bindDynamicModel(block, state -> {
                String suffix = "";
                if (state.hasProperty(ChestBlock.TYPE)) {
                    ChestType t = state.getValue(ChestBlock.TYPE);
                    if (t == ChestType.LEFT) suffix = "_left";
                    else if (t == ChestType.RIGHT) suffix = "_right";
                }
                return new ResourceLocation(namespace, "block/" + baseName + suffix + "_lbe");
            });
            boundCount++;
        }
        LegendaryBlockEntities.LOG.info("Bound {} {} chest blocks for dynamic body injection", boundCount, namespace);
    }

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
                ModelEvents.resolve("minecraft", "block/chest_normal_center_lid"),
                ModelEvents.resolve("minecraft", "block/chest_normal_left_lid"),
                ModelEvents.resolve("minecraft", "block/chest_normal_right_lid")};
        Supplier<BakedModel[]> trappedSupplier = () -> new BakedModel[]{
                ModelEvents.resolve("minecraft", "block/trapped_chest_normal_center_lid"),
                ModelEvents.resolve("minecraft", "block/trapped_chest_normal_left_lid"),
                ModelEvents.resolve("minecraft", "block/trapped_chest_normal_right_lid")};
        Supplier<BakedModel[]> enderSupplier = () -> new BakedModel[]{
                ModelEvents.resolve("minecraft", "block/ender_chest_normal_center_lid")};

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
        setupBclibChests("betterend", BETTER_END_NAMES);
    }

    public static void setupBetterNetherChests() {
        setupBclibChests("betternether", BETTER_NETHER_NAMES);
    }

    public static void setupIronChests() {
        Class<?> typesCls;
        try {
            typesCls = Class.forName("com.progwml6.ironchest.common.block.entity.IronChestsBlockEntityTypes");
        } catch (ClassNotFoundException e) {
            LegendaryBlockEntities.LOG.warn("Iron Chests BE types class not found: {}", e.getMessage());
            return;
        }

        Function<BlockEntity, Integer> singleSelector = be -> 0;
        int registered = 0;

        for (String name : IRON_CHEST_NAMES) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("ironchest", name));
            if (block == null) continue;

            BlockEntityType<?> beType;
            try {
                Object regObj = typesCls.getField(name.toUpperCase(java.util.Locale.ROOT)).get(null);

                beType = (BlockEntityType<?>) regObj.getClass().getMethod("get").invoke(regObj);
            } catch (ReflectiveOperationException e) {
                LegendaryBlockEntities.LOG.warn("Could not resolve Iron Chests BE type for {}: {}", name, e.getMessage());
                continue;
            }
            if (beType == null) continue;

            final String key = name;
            Supplier<BakedModel[]> supplier = () -> {
                BakedModel lid = lookupIron(key);
                return new BakedModel[]{ lid };
            };

            LegendaryBlockEntityRegistry.register(
                    block, beType,
                    BlockEntityRenderCondition.CHEST,
                    new ChestBlockEntityRendererOverride(supplier, singleSelector)
            );
            registered++;
        }
        LegendaryBlockEntities.LOG.info("Registered {} Iron Chests variants", registered);
    }

    private static BakedModel lookupIron(String name) {
        return ModelEvents.resolve("legendaryblockentities", "block/ic_" + name + "_lid");
    }

    private static BakedModel lookupBetterEnd(String name) {
        return ModelEvents.resolve("legendaryblockentities", "block/be_" + name + "_lid");
    }
    private static void setupBclibChests(String namespace, String[] names) {
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
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(namespace, name));
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

        int registered = 0;

        for (var entry : ForgeRegistries.BLOCKS.getEntries()) {
            var key = entry.getKey().location();
            if (!"quark".equals(key.getNamespace())) continue;
            String path = key.getPath();
            if (!path.endsWith("_chest") && !path.endsWith("_trapped_chest")) continue;
            if (path.startsWith("lootr_")) continue;

            Block block = entry.getValue();
            if (!(block instanceof ChestBlock)) continue;

            boolean trapped = path.endsWith("_trapped_chest");
            BlockEntityType<?> beType;
            try {

                Class<?> moduleCls = Class.forName("org.violetmoon.quark.content.building.module.VariantChestsModule");
                beType = (BlockEntityType<?>) moduleCls.getField(trapped ? "trappedChestTEType" : "chestTEType").get(null);
            } catch (ReflectiveOperationException e) {
                LegendaryBlockEntities.LOG.warn("Could not resolve Quark BE type for {}: {}", key, e.getMessage());
                continue;
            }
            if (beType == null) continue;

            Supplier<BakedModel[]> supplier = () -> new BakedModel[]{
                    lookupQuark(path + "_center"),
                    lookupQuark(path + "_left"),
                    lookupQuark(path + "_right")
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
        return ModelEvents.resolve("quark", "block/" + modelName + "_lid");
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
}
