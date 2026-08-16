package com.krampus.legendaryblockentities.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class RotatedBakedModel implements BakedModel {
    private final BakedModel wrapped;
    private final IQuadTransformer transformer;
    private final Direction[] sourceSide;
    private final Map<List<BakedQuad>, List<BakedQuad>> cache = new IdentityHashMap<>();

    public RotatedBakedModel(BakedModel wrapped, int yAngle) {
        this(wrapped, 0, yAngle);
    }

    public RotatedBakedModel(BakedModel wrapped, int xAngle, int yAngle) {
        this.wrapped = wrapped;
        if (xAngle == 0 && yAngle == 0) {
            this.transformer = null;
            this.sourceSide = null;
        } else {
            this.transformer = QuadTransformers.applying(centered(xAngle, yAngle));
            this.sourceSide = inverseFaceMap(BlockModelRotation.by(xAngle, yAngle).getRotation().getMatrix());
        }
    }

    private static Direction[] inverseFaceMap(Matrix4f rotation) {
        Matrix4f rot = new Matrix4f(rotation);
        Direction[] inverse = new Direction[6];
        Vector4f v = new Vector4f();
        for (Direction d : Direction.values()) {
            v.set(d.getStepX(), d.getStepY(), d.getStepZ(), 0f);
            rot.transform(v);
            inverse[nearestAxis(v.x(), v.y(), v.z()).ordinal()] = d;
        }
        return inverse;
    }

    private static Direction nearestAxis(float x, float y, float z) {
        float ax = Math.abs(x), ay = Math.abs(y), az = Math.abs(z);
        if (ax >= ay && ax >= az) return x >= 0 ? Direction.EAST : Direction.WEST;
        if (ay >= az) return y >= 0 ? Direction.UP : Direction.DOWN;
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private Direction sourceSide(@Nullable Direction side) {
        return (side == null || sourceSide == null) ? side : sourceSide[side.ordinal()];
    }

    private static Transformation centered(int xAngle, int yAngle) {
        Matrix4f rot = new Matrix4f(BlockModelRotation.by(xAngle, yAngle).getRotation().getMatrix());
        Matrix4f m = new Matrix4f()
                .translate(0.5f, 0.5f, 0.5f)
                .mul(rot)
                .translate(-0.5f, -0.5f, -0.5f);
        return new Transformation(m);
    }

    private List<BakedQuad> rotate(List<BakedQuad> in) {
        if (transformer == null || in.isEmpty()) return in;
        synchronized (cache) {
            return cache.computeIfAbsent(in, src -> {
                List<BakedQuad> out = new ArrayList<>(src.size());
                for (BakedQuad q : src) {
                    BakedQuad copy = new BakedQuad(
                            q.getVertices().clone(), q.getTintIndex(),
                            q.getDirection(), q.getSprite(), q.isShade());
                    transformer.processInPlace(copy);
                    Direction rotated = faceFromGeometry(copy.getVertices());
                    out.add(rotated == copy.getDirection() ? copy : new BakedQuad(
                            copy.getVertices(), copy.getTintIndex(), rotated, copy.getSprite(), copy.isShade()));
                }
                return out;
            });
        }
    }

    private static Direction faceFromGeometry(int[] v) {
        float x0 = f(v, 0, 0), y0 = f(v, 0, 1), z0 = f(v, 0, 2);
        float x1 = f(v, 1, 0), y1 = f(v, 1, 1), z1 = f(v, 1, 2);
        float x2 = f(v, 2, 0), y2 = f(v, 2, 1), z2 = f(v, 2, 2);
        float x3 = f(v, 3, 0), y3 = f(v, 3, 1), z3 = f(v, 3, 2);
        float dx0 = x2 - x0, dy0 = y2 - y0, dz0 = z2 - z0, dx1 = x3 - x1, dy1 = y3 - y1, dz1 = z3 - z1;
        float nx = dy0 * dz1 - dz0 * dy1, ny = dz0 * dx1 - dx0 * dz1, nz = dx0 * dy1 - dy0 * dx1;
        float ax = Math.abs(nx), ay = Math.abs(ny), az = Math.abs(nz);
        if (ax >= ay && ax >= az) return nx >= 0 ? Direction.EAST : Direction.WEST;
        if (ay >= ax && ay >= az) return ny >= 0 ? Direction.UP : Direction.DOWN;
        return nz >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static float f(int[] v, int vertex, int component) {
        return Float.intBitsToFloat(v[vertex * IQuadTransformer.STRIDE + component]);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand) {
        return rotate(wrapped.getQuads(state, sourceSide(side), rand));
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData data,
                                             @Nullable RenderType renderType) {
        return rotate(wrapped.getQuads(state, sourceSide(side), rand, data, renderType));
    }

    @Override
    public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos,
                                           @NotNull BlockState state, @NotNull ModelData modelData) {
        return wrapped.getModelData(level, pos, state, modelData);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
                                                      @NotNull ModelData data) {
        return wrapped.getRenderTypes(state, rand, data);
    }

    @Override public boolean useAmbientOcclusion() { return wrapped.useAmbientOcclusion(); }
    @Override public boolean isGui3d() { return wrapped.isGui3d(); }
    @Override public boolean usesBlockLight() { return wrapped.usesBlockLight(); }
    @Override public boolean isCustomRenderer() { return wrapped.isCustomRenderer(); }
    @Override @SuppressWarnings("deprecation") public @NotNull TextureAtlasSprite getParticleIcon() { return wrapped.getParticleIcon(); }
    @Override public @NotNull ItemTransforms getTransforms() { return wrapped.getTransforms(); }
    @Override public @NotNull ItemOverrides getOverrides() { return wrapped.getOverrides(); }
}
