package com.krampus.legendaryblockentities;

import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public final class LegendaryBlockEntityRegistry {
    public static final Map<Block, Pair<BlockEntityRenderCondition, BlockEntityRendererOverride>> ENTITIES = new HashMap<>();
    public static final Set<Block> BLOCKS = new HashSet<>();
    public static final Set<BlockEntityType<?>> BLOCK_ENTITY_TYPES = new HashSet<>();

    public record DynamicBinding(Function<BlockState, ResourceLocation> baseModel, boolean rotateByFacing, int yOffset) {}

    public static final Map<Block, DynamicBinding> DYNAMIC_INJECT = new ConcurrentHashMap<>();

    private LegendaryBlockEntityRegistry() {}

    public static void register(Block block,
                                BlockEntityType<?> type,
                                BlockEntityRenderCondition condition,
                                BlockEntityRendererOverride renderer) {
        ENTITIES.put(block, Pair.of(condition, renderer));
        BLOCKS.add(block);
        BLOCK_ENTITY_TYPES.add(type);
    }

    public static void bindDynamicModel(Block block, Function<BlockState, ResourceLocation> baseModel) {
        bindDynamicModel(block, baseModel, true, 0);
    }

    public static void bindDynamicModel(Block block, Function<BlockState, ResourceLocation> baseModel,
                                        boolean rotateByFacing) {
        bindDynamicModel(block, baseModel, rotateByFacing, 0);
    }

    public static void bindDynamicModel(Block block, Function<BlockState, ResourceLocation> baseModel,
                                        boolean rotateByFacing, int yOffset) {
        DYNAMIC_INJECT.put(block, new DynamicBinding(baseModel, rotateByFacing, yOffset));
    }
}
