package com.krampus.legendaryblockentities.client.render;

import com.krampus.legendaryblockentities.client.event.ModelEvents;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ShulkerBoxBlockEntityRendererOverride extends BlockEntityRendererOverride {
    private static final RandomSource DUMMY_RANDOM = RandomSource.create();

    @Override
    public void render(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity,
                       float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (!(blockEntity instanceof ShulkerBoxBlockEntity shulker)) return;

        DyeColor color = shulker.getColor();
        BakedModel lid = (color == null)
                ? ModelEvents.resolve("minecraft", "block/shulker_box_lid")
                : ModelEvents.resolve("minecraft", "block/" + color.getName() + "_shulker_box_lid");
        if (lid == null) return;

        BlockState state = blockEntity.getBlockState();
        Direction facing = Direction.UP;
        if (state.getBlock() instanceof ShulkerBoxBlock) {
            facing = state.getValue(ShulkerBoxBlock.FACING);
        }
        float progress = shulker.getProgress(partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(facing.getRotation());
        poseStack.mulPose(Axis.YP.rotationDegrees(270f * progress));
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0, progress * 0.5f, 0);

        RenderType layer = ItemBlockRenderTypes.getRenderType(state, false);
        VertexConsumer vc = buffer.getBuffer(layer);
        for (Direction side : Direction.values()) {
            for (BakedQuad quad : lid.getQuads(null, side, DUMMY_RANDOM)) {
                vc.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
            }
        }
        for (BakedQuad quad : lid.getQuads(null, null, DUMMY_RANDOM)) {
            vc.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }
}
