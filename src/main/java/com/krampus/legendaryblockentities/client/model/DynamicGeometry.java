package com.krampus.legendaryblockentities.client.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class DynamicGeometry implements IUnbakedGeometry<DynamicGeometry> {
    private final ResourceLocation fullModel;
    private final ResourceLocation trunkModel;
    private final String deferGroup;

    public DynamicGeometry(ResourceLocation fullModel, ResourceLocation trunkModel, String deferGroup) {
        this.fullModel = fullModel;
        this.trunkModel = trunkModel;
        this.deferGroup = deferGroup;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter,
                           ModelState modelState, ItemOverrides overrides,
                           ResourceLocation modelLocation) {
        BakedModel full = baker.bake(fullModel, modelState, spriteGetter);
        BakedModel trunk = baker.bake(trunkModel, modelState, spriteGetter);
        return new DynamicBakedModel(full, trunk, deferGroup);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        UnbakedModel fullUnbaked = modelGetter.apply(fullModel);
        if (fullUnbaked != null) fullUnbaked.resolveParents(modelGetter);
        UnbakedModel trunkUnbaked = modelGetter.apply(trunkModel);
        if (trunkUnbaked != null) trunkUnbaked.resolveParents(modelGetter);
    }
}