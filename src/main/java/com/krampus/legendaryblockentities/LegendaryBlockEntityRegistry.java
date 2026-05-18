package com.krampus.legendaryblockentities;

import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class LegendaryBlockEntityRegistry {
    public static final Map<BlockEntityType<?>, Pair<BlockEntityRenderCondition, BlockEntityRendererOverride>> ENTITIES = new HashMap<>();
    public static final Set<Block> BLOCKS = new HashSet<>();

    private LegendaryBlockEntityRegistry() {}

    public static void register(Block block,
                                BlockEntityType<?> type,
                                BlockEntityRenderCondition condition,
                                BlockEntityRendererOverride renderer) {
        ENTITIES.put(type, Pair.of(condition, renderer));
        BLOCKS.add(block);
    }
}