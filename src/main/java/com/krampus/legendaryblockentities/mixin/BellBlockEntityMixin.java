package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.util.duck.AppearanceStateHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BellBlockEntity.class)
public abstract class BellBlockEntityMixin implements AppearanceStateHolder {
    @Unique private int lbe$modelState = 0;

    @Inject(method = "clientTick", at = @At("HEAD"))
    private static void lbe$listenForRing(
            Level level, BlockPos pos, BlockState state, BellBlockEntity bell,
            CallbackInfo ci) {

        AppearanceStateHolder holder = (AppearanceStateHolder) bell;
        boolean shaking = bell.shaking;
        int currentState = holder.lbe$getModelState();
        int newState = shaking ? 1 : currentState == 1 ? 2 : 0;
        if (currentState != newState) {
            holder.lbe$setModelStateAndRebuild(newState, level, pos);
        }
    }

    @Override public int lbe$getModelState() { return this.lbe$modelState; }
    @Override public void lbe$setModelState(int state) { this.lbe$modelState = state; }
}
