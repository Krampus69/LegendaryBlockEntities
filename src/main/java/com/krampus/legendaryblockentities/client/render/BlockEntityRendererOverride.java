package com.krampus.legendaryblockentities.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class BlockEntityRendererOverride {

    public static final BlockEntityRendererOverride NO_OP = new BlockEntityRendererOverride() {
        @Override
        public void render(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity,
                           float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                           int packedLight, int packedOverlay) {
        }
    };

    public abstract void render(BlockEntityRenderer<BlockEntity> renderer,
                                BlockEntity blockEntity,
                                float partialTick,
                                PoseStack poseStack,
                                MultiBufferSource buffer,
                                int packedLight,
                                int packedOverlay);

    public void onModelsReload() {}
}
