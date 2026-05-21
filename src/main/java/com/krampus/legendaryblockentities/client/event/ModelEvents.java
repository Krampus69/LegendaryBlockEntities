package com.krampus.legendaryblockentities.client.event;

import com.krampus.legendaryblockentities.LegendaryBlockEntities;
import com.krampus.legendaryblockentities.LegendaryBlockEntityRegistry;
import com.krampus.legendaryblockentities.client.model.DynamicGeometryLoader;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelEvents {

    public static final Map<String, BakedModel> quarkChestLids = new HashMap<>();
    public static final Map<String, BakedModel> betterEndChestLids = new HashMap<>();
    public static volatile BakedModel chestLidModel = null;
    public static volatile BakedModel chestLeftLidModel = null;
    public static volatile BakedModel chestRightLidModel = null;
    public static volatile BakedModel trappedChestLidModel = null;
    public static volatile BakedModel trappedChestLeftLidModel = null;
    public static volatile BakedModel trappedChestRightLidModel = null;
    public static volatile BakedModel enderChestLidModel = null;
    public static volatile BakedModel bellBodyModel = null;
    public static volatile BakedModel uncoloredShulkerLidModel = null;
    public static final Map<DyeColor, BakedModel> shulkerLidModels = new HashMap<>();
    public static final Map<String, BakedModel> ironChestLids = new HashMap<>();

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("dynamic", new DynamicGeometryLoader());
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        // Reset state
        chestLidModel = chestLeftLidModel = chestRightLidModel = null;
        trappedChestLidModel = trappedChestLeftLidModel = trappedChestRightLidModel = null;
        enderChestLidModel = bellBodyModel = uncoloredShulkerLidModel = null;
        shulkerLidModels.clear();
        quarkChestLids.clear();
        betterEndChestLids.clear();
        ironChestLids.clear();

        // Single pass over all baked models
        for (var entry : event.getModels().entrySet()) {
            String key = entry.getKey().toString();
            BakedModel model = entry.getValue();

            if      (key.contains("trapped_chest_normal_left_lid"))   trappedChestLeftLidModel = model;
            else if (key.contains("trapped_chest_normal_right_lid"))  trappedChestRightLidModel = model;
            else if (key.contains("trapped_chest_normal_center_lid")) trappedChestLidModel = model;
            else if (key.contains("ender_chest_normal_center_lid"))   enderChestLidModel = model;
            else if (key.contains("chest_normal_left_lid"))           chestLeftLidModel = model;
            else if (key.contains("chest_normal_right_lid"))          chestRightLidModel = model;
            else if (key.contains("chest_normal_center_lid"))         chestLidModel = model;
            else if (key.equals("minecraft:block/bell_body"))         bellBodyModel = model;
            else if (key.startsWith("legendaryblockentities:block/ic_") && key.endsWith("_lid")) {
                String name = key.substring("legendaryblockentities:block/ic_".length(), key.length() - "_lid".length());
                ironChestLids.put(name, model);
            }
            else if (key.startsWith("minecraft:block/") && key.endsWith("shulker_box_lid")) {
                String suffix = key.substring("minecraft:block/".length());
                if (suffix.equals("shulker_box_lid")) {
                    uncoloredShulkerLidModel = model;
                } else {
                    String name = suffix.substring(0, suffix.length() - "_shulker_box_lid".length());
                    DyeColor color = DyeColor.byName(name, null);
                    if (color != null) shulkerLidModels.put(color, model);
                }
            }
            else if (key.startsWith("quark:block/") && key.endsWith("_lid")) {
                String name = key.substring("quark:block/".length(), key.length() - "_lid".length());
                quarkChestLids.put(name, model);
            }
            else if (key.startsWith("legendaryblockentities:block/be_") && key.endsWith("_lid")) {
                String name = key.substring("legendaryblockentities:block/be_".length(), key.length() - "_lid".length());
                betterEndChestLids.put(name, model);
            }
        }

        // Consolidated missing-model report
        List<String> missing = new ArrayList<>();
        if (chestLidModel == null)             missing.add("chest_normal_center_lid");
        if (chestLeftLidModel == null)         missing.add("chest_normal_left_lid");
        if (chestRightLidModel == null)        missing.add("chest_normal_right_lid");
        if (trappedChestLidModel == null)      missing.add("trapped_chest_normal_center_lid");
        if (trappedChestLeftLidModel == null)  missing.add("trapped_chest_normal_left_lid");
        if (trappedChestRightLidModel == null) missing.add("trapped_chest_normal_right_lid");
        if (enderChestLidModel == null)        missing.add("ender_chest_normal_center_lid");
        if (bellBodyModel == null)             missing.add("bell_body");
        if (uncoloredShulkerLidModel == null)  missing.add("shulker_box_lid");
        for (DyeColor c : DyeColor.values()) {
            if (!shulkerLidModels.containsKey(c)) missing.add(c.getName() + "_shulker_box_lid");
        }
        if (!missing.isEmpty()) {
            LegendaryBlockEntities.LOG.error("Missing baked models: {}", missing);
        }

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