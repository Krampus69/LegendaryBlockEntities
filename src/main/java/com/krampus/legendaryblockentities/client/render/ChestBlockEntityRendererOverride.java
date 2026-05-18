package com.krampus.legendaryblockentities.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.function.Function;
import java.util.function.Supplier;

public class ChestBlockEntityRendererOverride extends BlockEntityRendererOverride {
    private static final RandomSource DUMMY_RANDOM = RandomSource.create();

    private BakedModel[] models = null;
    private final Supplier<BakedModel[]> modelGetter;
    private final Function<BlockEntity, Integer> modelSelector;

    public ChestBlockEntityRendererOverride(Supplier<BakedModel[]> modelGetter,
                                            Function<BlockEntity, Integer> modelSelector) {
        this.modelGetter = modelGetter;
        this.modelSelector = modelSelector;
    }

    @Override
    public void render(BlockEntityRenderer<BlockEntity> renderer, BlockEntity blockEntity,
                       float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight, int packedOverlay) {
        if (models == null) models = modelGetter.get();
        if (!(blockEntity instanceof LidBlockEntity)) return;

        BlockState state = blockEntity.getBlockState();
        LidBlockEntity chest = getAnimationProgress(blockEntity, partialTick);

        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        Direction facing = state.getValue(ChestBlock.FACING);
        poseStack.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
        poseStack.translate(-0.5f, 0, -0.5f);

        final float yPivot = 9f / 16f;
        final float zPivot = 15f / 16f;
        poseStack.translate(0, yPivot, zPivot);

        float openness = chest.getOpenNess(partialTick);
        float curved = 1f - openness;
        curved = 1f - (curved * curved * curved);
        poseStack.mulPose(Axis.XP.rotationDegrees(curved * 90f));

        poseStack.translate(0, -yPivot, -zPivot);

        BakedModel model = models[modelSelector.apply(blockEntity)];
        renderBakedModel(buffer, state, poseStack, model, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private static void renderBakedModel(MultiBufferSource buffer, BlockState state,
                                         PoseStack poseStack, BakedModel model,
                                         int packedLight, int packedOverlay) {
        RenderType layer = ItemBlockRenderTypes.getRenderType(state, false);
        VertexConsumer consumer = buffer.getBuffer(layer);
        for (Direction side : Direction.values()) {
            for (BakedQuad quad : model.getQuads(null, side, DUMMY_RANDOM)) {
                consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
            }
        }
        for (BakedQuad quad : model.getQuads(null, null, DUMMY_RANDOM)) {
            consumer.putBulkData(poseStack.last(), quad, 1f, 1f, 1f, packedLight, packedOverlay);
        }
    }

    public static LidBlockEntity getAnimationProgress(BlockEntity blockEntity, float partialTick) {
        LidBlockEntity chest = (LidBlockEntity) blockEntity;
        BlockState state = blockEntity.getBlockState();

        if (state.hasProperty(ChestBlock.TYPE) && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockEntity neighbor = null;
            BlockPos pos = blockEntity.getBlockPos();
            Direction facing = state.getValue(ChestBlock.FACING);
            switch (state.getValue(ChestBlock.TYPE)) {
                case LEFT -> neighbor = blockEntity.getLevel().getBlockEntity(pos.relative(facing.getClockWise()));
                case RIGHT -> neighbor = blockEntity.getLevel().getBlockEntity(pos.relative(facing.getCounterClockWise()));
                default -> {}
            }
            if (neighbor instanceof LidBlockEntity n
                    && n.getOpenNess(partialTick) > chest.getOpenNess(partialTick)) {
                chest = n;
            }
        }
        return chest;
    }

    @Override
    public void onModelsReload() {
        this.models = null;
    }
}
