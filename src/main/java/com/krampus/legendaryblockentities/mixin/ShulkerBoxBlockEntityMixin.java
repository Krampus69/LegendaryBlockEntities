package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.util.duck.AppearanceStateHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxBlockEntity.class)
public abstract class ShulkerBoxBlockEntityMixin implements AppearanceStateHolder {
    @Unique private int lbe$modelState = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private static void lbe$listenForOpenClose(Level level, BlockPos pos, BlockState state,
                                               ShulkerBoxBlockEntity blockEntity, CallbackInfo ci) {
        AppearanceStateHolder holder = (AppearanceStateHolder) blockEntity;
        boolean lidMoving = blockEntity.getProgress(0f) > 0f;
        int currentState = holder.lbe$getModelState();
        int newState = lidMoving ? 1 : currentState == 1 ? 2 : 0;
        if (currentState != newState) {
            holder.lbe$setModelStateAndRebuild(newState, level, pos);
        }
    }

    @Override public int lbe$getModelState() { return this.lbe$modelState; }
    @Override public void lbe$setModelState(int state) { this.lbe$modelState = state; }
}
