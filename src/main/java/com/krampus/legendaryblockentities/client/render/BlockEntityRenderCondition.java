package com.krampus.legendaryblockentities.client.render;

import com.krampus.legendaryblockentities.util.duck.AppearanceStateHolder;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface BlockEntityRenderCondition {

    BlockEntityRenderCondition STATE_GREATER_THAN_1 = entity -> {
        if (entity instanceof AppearanceStateHolder holder) {
            return holder.lbe$getModelState() > 0;
        }
        return false;
    };

    BlockEntityRenderCondition CHEST = STATE_GREATER_THAN_1;
    BlockEntityRenderCondition BELL = STATE_GREATER_THAN_1;
    BlockEntityRenderCondition SHULKER_BOX = STATE_GREATER_THAN_1;

    BlockEntityRenderCondition NEVER = entity -> false;
    BlockEntityRenderCondition ALWAYS = entity -> true;

    boolean shouldRender(BlockEntity entity);
}
