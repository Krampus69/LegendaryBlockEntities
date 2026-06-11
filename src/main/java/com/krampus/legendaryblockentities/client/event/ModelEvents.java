package com.krampus.legendaryblockentities.client.event;

import com.krampus.legendaryblockentities.LBESetup;
import com.krampus.legendaryblockentities.LegendaryBlockEntities;
import com.krampus.legendaryblockentities.LegendaryBlockEntityRegistry;
import com.krampus.legendaryblockentities.client.model.DynamicGeometryLoader;
import com.krampus.legendaryblockentities.client.model.RotatedBakedModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelEvents {

    /**
     * Lazily-resolved baked-model cache, keyed by model location.
     *
     * We deliberately do NOT capture model references in ModifyBakingResult any more.
     * Instead we resolve through ModelManager#getModel on demand (at render time) and
     * cache the result here. This keeps us compatible with ModernFix's "dynamic
     * resources" optimization, which bakes models lazily/evictably: under it the
     * ModifyBakingResult map is empty, but getModel() bakes the requested model on the
     * spot. In a vanilla (non-dynamic) pipeline getModel() simply returns the already
     * baked model, so the same code path works in both cases.
     */
    private static final Map<ResourceLocation, BakedModel> CACHE = new ConcurrentHashMap<>();

    /**
     * Resolve a baked model by location.
     *
     * @return the baked model, or {@code null} if it is absent (i.e. the model manager
     *         returned the missing model). Returning null lets callers skip rendering
     *         instead of drawing stone or the missing-texture cube. Misses are not
     *         cached, so a model that bakes slightly later will resolve on a later frame.
     */
    public static BakedModel resolve(ResourceLocation location) {
        BakedModel cached = CACHE.get(location);
        if (cached != null) return cached;

        ModelManager mm = Minecraft.getInstance().getModelManager();
        BakedModel model = mm.getModel(location);
        if (model == null || model == mm.getMissingModel()) {
            return null;
        }
        CACHE.put(location, model);
        return model;
    }

    public static BakedModel resolve(String namespace, String path) {
        return resolve(new ResourceLocation(namespace, path));
    }

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("dynamic", new DynamicGeometryLoader());
    }

    /**
     * Inject the LBE dynamic body model under each blockstate variant for blocks whose
     * owning mod registers its blockstate models in code (so our JSON override loses).
     *
     * Runs at ModifyBakingResult so the injected models are present before chunks build.
     * Under ModernFix the result map is the dynamic provider; put() writes a permanent
     * override that wins over both lazy baking and the (losing) blockstate JSON. The base
     * dynamic-model file itself is uncontested, so fetching it via get() returns our
     * DynamicBakedModel; we then re-apply the per-facing rotation (for families whose
     * blockstate rotates the body) since we are bypassing the blockstate's "y" value.
     *
     * We populate the bindings here (ensureDynamicBindings) rather than relying on client
     * setup, because under ModernFix the model bake can run on a worker thread BEFORE the
     * FMLClientSetupEvent enqueued work that would otherwise register them.
     */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        LBESetup.ensureDynamicBindings();

        Map<ResourceLocation, BakedModel> models = event.getModels();
        int bound = LegendaryBlockEntityRegistry.DYNAMIC_INJECT.size();
        LegendaryBlockEntities.LOG.info("ModifyBakingResult fired; DYNAMIC_INJECT has {} block(s)", bound);

        int injected = 0;
        try {
            for (Map.Entry<Block, LegendaryBlockEntityRegistry.DynamicBinding> e
                    : LegendaryBlockEntityRegistry.DYNAMIC_INJECT.entrySet()) {
                Block block = e.getKey();
                LegendaryBlockEntityRegistry.DynamicBinding binding = e.getValue();
                ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
                if (blockId == null) continue;

                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    ResourceLocation base = binding.baseModel().apply(state);
                    if (base == null) continue;

                    BakedModel dynamic = models.get(base);
                    if (dynamic == null) continue;

                    int y = (((binding.rotateByFacing() ? yAngle(state) : 0) + binding.yOffset()) % 360 + 360) % 360;
                    ModelResourceLocation mrl = BlockModelShaper.stateToModelLocation(blockId, state);
                    models.put(mrl, y == 0 ? dynamic : new RotatedBakedModel(dynamic, y));
                    injected++;
                }
            }
        } catch (Throwable t) {
            LegendaryBlockEntities.LOG.error("Dynamic body injection failed", t);
        }

        LegendaryBlockEntities.LOG.info("Injected {} dynamic chest-body model states", injected);
    }

    private static int yAngle(BlockState state) {
        if (!state.hasProperty(ChestBlock.FACING)) return 0;
        return switch (state.getValue(ChestBlock.FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0; // NORTH
        };
    }

    @SubscribeEvent
    public static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        CACHE.clear();
        for (var entry : LegendaryBlockEntityRegistry.ENTITIES.values()) {
            entry.getSecond().onModelsReload();
        }
    }

    @SubscribeEvent
    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        String[] chestVariants = {"chest_normal_center", "chest_normal_left", "chest_normal_right",
                "trapped_chest_normal_center", "trapped_chest_normal_left", "trapped_chest_normal_right",
                "ender_chest_normal_center"};
        for (String name : chestVariants) {
            event.register(new ResourceLocation("minecraft", "block/" + name + "_lid"));
            event.register(new ResourceLocation("minecraft", "block/" + name + "_trunk"));
            event.register(new ResourceLocation("minecraft", "block/" + name + "_full"));
        }

        event.register(new ResourceLocation("minecraft", "block/bell_body"));
        for (String variant : new String[]{"floor", "ceiling", "wall", "between_walls"}) {
            event.register(new ResourceLocation("minecraft", "block/bell_" + variant + "_with_bell"));
            event.register(new ResourceLocation("minecraft", "block/dynamic_bell_" + variant));
        }

        String[] bedColors = {"black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray",
                "lime", "magenta", "orange", "pink", "purple", "red", "white", "yellow"};
        for (String color : bedColors) {
            event.register(new ResourceLocation("minecraft", "block/" + color + "_bed_head"));
            event.register(new ResourceLocation("minecraft", "block/" + color + "_bed_foot"));
        }

        String[] betterEndChests = {
                "lucernia_chest", "dragon_tree_chest", "end_lotus_chest", "helix_tree_chest",
                "jellyshroom_chest", "lacugrove_chest", "mossy_glowshroom_chest",
                "pythadendron_chest", "tenanea_chest", "umbrella_tree_chest"
        };
        String[] betterNetherChests = {
                "anchor_tree_chest", "crimson_chest", "mushroom_fir_chest", "mushroom_fir_trimmed_chest",
                "nether_mushroom_chest", "nether_reed_chest", "nether_sakura_chest", "rubeus_chest",
                "stalagnate_chest", "warped_chest", "wart_chest", "willow_chest"
        };
        String[] allBclibChests = new String[betterEndChests.length + betterNetherChests.length];
        System.arraycopy(betterEndChests, 0, allBclibChests, 0, betterEndChests.length);
        System.arraycopy(betterNetherChests, 0, allBclibChests, betterEndChests.length, betterNetherChests.length);

        String[] ironChests = {
                "iron_chest", "gold_chest", "diamond_chest", "copper_chest", "obsidian_chest", "dirt_chest",
                "trapped_iron_chest", "trapped_gold_chest", "trapped_diamond_chest",
                "trapped_copper_chest", "trapped_obsidian_chest", "trapped_dirt_chest"
        };
        for (String name : ironChests) {
            event.register(new ResourceLocation("ironchest", "block/" + name + "_lbe"));
            event.register(new ResourceLocation("legendaryblockentities", "block/ic_" + name + "_lid"));
            event.register(new ResourceLocation("legendaryblockentities", "block/ic_" + name + "_full"));
            event.register(new ResourceLocation("legendaryblockentities", "block/ic_" + name + "_trunk"));
        }

        for (String name : allBclibChests) {
            for (String half : new String[]{ "", "_left", "_right" }) {
                event.register(new ResourceLocation("legendaryblockentities", "block/be_" + name + half + "_lid"));
                event.register(new ResourceLocation("legendaryblockentities", "block/be_" + name + half + "_full"));
                event.register(new ResourceLocation("legendaryblockentities", "block/be_" + name + half + "_trunk"));
            }
        }

        String[] shulkerColors = {"", "black_", "blue_", "brown_", "cyan_", "gray_", "green_",
                "light_blue_", "light_gray_", "lime_", "magenta_", "orange_", "pink_",
                "purple_", "red_", "white_", "yellow_"};
        for (String c : shulkerColors) {
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_full"));
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_bottom"));
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_lid"));
            event.register(new ResourceLocation("minecraft", "block/dynamic_" + c + "shulker_box"));
        }
        String[] quarkWoods = {"oak", "spruce", "birch", "jungle", "dark_oak", "acacia", "mangrove", "cherry",
                "bamboo", "crimson", "warped", "azalea", "blossom", "ancient", "nether_brick", "purpur", "prismarine"};
        for (String wood : quarkWoods) {
            for (String type : new String[]{"_chest", "_trapped_chest"}) {
                for (String half : new String[]{"center", "left", "right"}) {
                    for (String piece : new String[]{"full", "trunk", "lid"}) {
                        event.register(new ResourceLocation("quark", "block/" + wood + type + "_" + half + "_" + piece));
                    }
                    event.register(new ResourceLocation("quark", "block/" + wood + type + "_" + half));
                }
            }
        }
    }
}
