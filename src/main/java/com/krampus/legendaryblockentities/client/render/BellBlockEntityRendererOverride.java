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
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BellBlockEntityRendererOverride extends BlockEntityRendererOverride {
    private static final RandomSource DUMMY_RANDOM = RandomSource.create();

    @Override
    public void render(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity,
                       float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        BakedModel bellModel = ModelEvents.bellBodyModel;
        if (bellModel == null) return;
        if (!(blockEntity instanceof BellBlockEntity bell)) return;

        float ringTicks = bell.ticks + partialTick;
        float pitch = 0f, roll = 0f;
        if (bell.shaking) {
            float swing = Mth.sin(ringTicks / (float) Math.PI) / (4f + ringTicks / 3f);
            Direction hit = bell.clickDirection;
            if (hit == Direction.NORTH)      pitch = -swing;
            else if (hit == Direction.SOUTH) pitch =  swing;
            else if (hit == Direction.EAST)  roll  = -swing;
            else if (hit == Direction.WEST)  roll  =  swing;
        }

        poseStack.pushPose();
        poseStack.translate(0.5f, 12f / 16f, 0.5f);
        poseStack.mulPose(Axis.XP.rotation(pitch));
        poseStack.mulPose(Axis.ZP.rotation(roll));
        poseStack.translate(-0.5f, -12f / 16f, -0.5f);

        BlockState state = blockEntity.getBlockState();
        RenderType layer = ItemBlockRenderTypes.getRenderType(state, false);
        VertexConsumer vc = buffer.getBuffer(layer);
        for (Direction side : Direction.values()) {
            for (BakedQuad quad : bellModel.getQuads(null, side, DUMMY_RANDOM)) {
                vc.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
            }
        }
        for (BakedQuad quad : bellModel.getQuads(null, null, DUMMY_RANDOM)) {
            vc.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }
}
