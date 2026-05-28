package com.krampus.legendaryblockentities.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.IGeometryLoader;

public class DynamicGeometryLoader implements IGeometryLoader<DynamicGeometry> {
    @Override
    public DynamicGeometry read(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation fullModel = new ResourceLocation(GsonHelper.getAsString(jsonObject, "full"));
        ResourceLocation trunkModel = new ResourceLocation(GsonHelper.getAsString(jsonObject, "trunk"));
        String deferGroup = GsonHelper.getAsString(jsonObject, "defer_group", "");
        return new DynamicGeometry(fullModel, trunkModel, deferGroup);
    }
}