package com.krampus.legendaryblockentities.client.render;

import com.krampus.legendaryblockentities.mixin.SignRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

public class SignBlockEntityRendererOverride extends BlockEntityRendererOverride {

    @Override
    public void render(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity,
                       float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof SignBlockEntity)) return;

        SignRenderer.SignModel model = null;
        Object raw = renderer;
        if (raw instanceof SignRenderer signRenderer
                && blockEntity.getBlockState().getBlock() instanceof SignBlock signBlock) {
            model = ((SignRendererAccessor) signRenderer).lbe$getSignModels().get(signBlock.type());
        }

        if (model == null) {
            renderer.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        boolean wasVisible = model.root.visible;
        model.root.visible = false;
        try {
            renderer.render(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
        } finally {
            model.root.visible = wasVisible;
        }
    }
}
