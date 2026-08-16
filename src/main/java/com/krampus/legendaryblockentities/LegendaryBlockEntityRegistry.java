package com.krampus.legendaryblockentities;

import com.krampus.legendaryblockentities.client.render.BlockEntityRenderCondition;
import com.krampus.legendaryblockentities.client.render.BlockEntityRendererOverride;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

    public record Rot(int x, int y) {
        public static final Rot NONE = new Rot(0, 0);
    }

    public record DynamicBinding(Function<BlockState, ResourceLocation> baseModel,
                                 Function<BlockState, Rot> rotation) {}

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
        bindDynamicModel(block, baseModel,
                state -> new Rot(0, norm((rotateByFacing ? facingYAngle(state) : 0) + yOffset)));
    }

    public static void bindDynamicModel(Block block, Function<BlockState, ResourceLocation> baseModel,
                                        Function<BlockState, Rot> rotation) {
        DYNAMIC_INJECT.put(block, new DynamicBinding(baseModel, rotation));
    }

    public static int facingYAngle(BlockState state) {
        if (!state.hasProperty(HorizontalDirectionalBlock.FACING)) return 0;
        return switch (state.getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
    }

    public static int norm(int degrees) {
        return ((degrees % 360) + 360) % 360;
    }
}
