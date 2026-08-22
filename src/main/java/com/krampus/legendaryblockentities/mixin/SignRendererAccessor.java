package com.krampus.legendaryblockentities.mixin;

import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SignRenderer.class)
public interface SignRendererAccessor {
    @Accessor("signModels")
    Map<WoodType, SignRenderer.SignModel> lbe$getSignModels();
}
