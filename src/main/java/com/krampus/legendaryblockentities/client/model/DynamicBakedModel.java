package com.krampus.legendaryblockentities.client.model;

import com.krampus.legendaryblockentities.util.duck.AppearanceStateHolder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamicBakedModel implements BakedModel {
    public static final ModelProperty<Integer> APPEARANCE_STATE = new ModelProperty<>();

    private final BakedModel fullModel;
    private final BakedModel trunkModel;

    public DynamicBakedModel(BakedModel fullModel, BakedModel trunkModel) {
        this.fullModel = fullModel;
        this.trunkModel = trunkModel;
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        int appearanceState = 0;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AppearanceStateHolder h) {
            appearanceState = h.lbe$getModelState();
        }
        return modelData.derive().with(APPEARANCE_STATE, appearanceState).build();
    }

    private BakedModel pickModel(ModelData data) {
        Integer s = data.get(APPEARANCE_STATE);
        return (s != null && s == 1 && trunkModel != null) ? trunkModel : fullModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand) {
        return fullModel.getQuads(state, side, rand);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData extraData,
                                             @Nullable RenderType renderType) {
        return pickModel(extraData).getQuads(state, side, rand, extraData, renderType);
    }

    @Override public boolean useAmbientOcclusion() { return fullModel.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return fullModel.isGui3d(); }
    @Override public boolean usesBlockLight() { return fullModel.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return true; }
    @Override @SuppressWarnings("deprecation") public @NotNull TextureAtlasSprite getParticleIcon() { return fullModel.getParticleIcon(); }
    @Override public @NotNull ItemTransforms getTransforms() { return fullModel.getTransforms(); }
    @Override public @NotNull ItemOverrides getOverrides() { return fullModel.getOverrides(); }
}
