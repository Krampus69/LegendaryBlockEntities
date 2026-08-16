package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.LegendaryBlockEntityRegistry;
import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    @Inject(method = "setupAndRender", at = @At("HEAD"), cancellable = true)
    private static <E extends BlockEntity> void lbe$routeToOverride(
            BlockEntityRenderer<? super E> renderer,
            E blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            CallbackInfo ci) {

        Block block = blockEntity.getBlockState().getBlock();

        if (!LegendaryBlockEntityRegistry.BLOCKS.contains(block)) return;

        Pair<BlockEntityRenderCondition, BlockEntityRendererOverride> entry =
                LegendaryBlockEntityRegistry.ENTITIES.get(block);

        if (entry.getFirst().shouldRender(blockEntity)) {
            Level level = blockEntity.getLevel();
            int packedLight = (level != null)
                    ? LevelRenderer.getLightColor(level, blockEntity.getBlockPos())
                    : 15728880;

            @SuppressWarnings({"unchecked", "rawtypes"})
            BlockEntityRenderer<BlockEntity> raw = (BlockEntityRenderer) renderer;
            entry.getSecond().render(
                    raw, blockEntity, partialTick, poseStack, buffer,
                    packedLight, OverlayTexture.NO_OVERLAY
            );
        }

        if (blockEntity.getLevel() != null) {
            ci.cancel();
        }
    }
}
