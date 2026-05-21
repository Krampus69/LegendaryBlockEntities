package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.util.duck.AppearanceStateHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.progwml6.ironchest.common.block.regular.entity.AbstractIronChestBlockEntity", remap = false)
public class IronChestBlockEntityMixin implements AppearanceStateHolder {
    @Unique private int lbe$modelState = 0;

    @Inject(method = "lidAnimateTick", at = @At("TAIL"), remap = false, require = 0)
    private static void lbe$tickLid(Level level, BlockPos pos, BlockState state,
                                    @Coerce BlockEntity blockEntity, CallbackInfo ci) {
        if (!(blockEntity instanceof AppearanceStateHolder holder)) return;
        if (!(blockEntity instanceof LidBlockEntity lid)) return;

        boolean open = lid.getOpenNess(1f) > 0f;
        int newState = open ? 1 : 0;
        if (holder.lbe$getModelState() != newState) {
            holder.lbe$setModelStateAndRebuild(newState, level, pos);
        }
    }

    @Override public int lbe$getModelState() { return this.lbe$modelState; }
    @Override public void lbe$setModelState(int state) { this.lbe$modelState = state; }
}