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

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = LegendaryBlockEntities.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModelEvents {

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

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register("dynamic", new DynamicGeometryLoader());
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();

        for (var entry : models.entrySet()) {
            String key = entry.getKey().toString();
            if      (key.contains("trapped_chest_normal_left_lid"))   trappedChestLeftLidModel = entry.getValue();
            else if (key.contains("trapped_chest_normal_right_lid"))  trappedChestRightLidModel = entry.getValue();
            else if (key.contains("trapped_chest_normal_center_lid")) trappedChestLidModel = entry.getValue();
            else if (key.contains("ender_chest_normal_center_lid"))   enderChestLidModel = entry.getValue();
            else if (key.contains("chest_normal_left_lid"))           chestLeftLidModel = entry.getValue();
            else if (key.contains("chest_normal_right_lid"))          chestRightLidModel = entry.getValue();
            else if (key.contains("chest_normal_center_lid"))         chestLidModel = entry.getValue();
            else if (key.equals("minecraft:block/bell_body"))         bellBodyModel = entry.getValue();
        }

        shulkerLidModels.clear();
        uncoloredShulkerLidModel = null;
        for (var entry : models.entrySet()) {
            String key = entry.getKey().toString();
            if (!key.startsWith("minecraft:block/")) continue;
            String suffix = key.substring("minecraft:block/".length());
            if (suffix.equals("shulker_box_lid")) {
                uncoloredShulkerLidModel = entry.getValue();
            } else if (suffix.endsWith("_shulker_box_lid")) {
                String name = suffix.substring(0, suffix.length() - "_shulker_box_lid".length());
                DyeColor color = DyeColor.byName(name, null);
                if (color != null) shulkerLidModels.put(color, entry.getValue());
            }
        }

        warnIfNull("chest_normal_center_lid", chestLidModel);
        warnIfNull("chest_normal_left_lid", chestLeftLidModel);
        warnIfNull("chest_normal_right_lid", chestRightLidModel);
        warnIfNull("trapped_chest_normal_center_lid", trappedChestLidModel);
        warnIfNull("trapped_chest_normal_left_lid", trappedChestLeftLidModel);
        warnIfNull("trapped_chest_normal_right_lid", trappedChestRightLidModel);
        warnIfNull("ender_chest_normal_center_lid", enderChestLidModel);
        warnIfNull("bell_body", bellBodyModel);
        if (uncoloredShulkerLidModel == null) {
            LegendaryBlockEntities.LOG.error("shulker_box_lid NOT FOUND");
        }
        for (DyeColor c : DyeColor.values()) {
            if (!shulkerLidModels.containsKey(c)) {
                LegendaryBlockEntities.LOG.error("{}_shulker_box_lid NOT FOUND", c.getName());
            }
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

        String[] shulkerColors = {"", "black_", "blue_", "brown_", "cyan_", "gray_", "green_",
                "light_blue_", "light_gray_", "lime_", "magenta_", "orange_", "pink_",
                "purple_", "red_", "white_", "yellow_"};
        for (String c : shulkerColors) {
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_full"));
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_bottom"));
            event.register(new ResourceLocation("minecraft", "block/" + c + "shulker_box_lid"));
            event.register(new ResourceLocation("minecraft", "block/dynamic_" + c + "shulker_box"));
        }
    }

    private static void warnIfNull(String name, BakedModel m) {
        if (m == null) LegendaryBlockEntities.LOG.error("{} NOT FOUND", name);
    }
}
