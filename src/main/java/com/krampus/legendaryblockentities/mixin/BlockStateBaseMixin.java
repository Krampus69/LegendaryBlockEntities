package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.LegendaryBlockEntityRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Shadow public abstract Block getBlock();

    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void lbe$overrideRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        if (LegendaryBlockEntityRegistry.BLOCKS.contains(this.getBlock())) {
            cir.setReturnValue(RenderShape.MODEL);
        }
    }
}