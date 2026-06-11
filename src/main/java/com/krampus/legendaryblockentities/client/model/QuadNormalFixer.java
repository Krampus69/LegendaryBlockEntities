package com.krampus.legendaryblockentities.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.client.model.IQuadTransformer;

import java.util.ArrayList;
import java.util.List;

public final class QuadNormalFixer {

    private static final int STRIDE = IQuadTransformer.STRIDE;
    private static final float EPS = 1.0e-4f;

    private QuadNormalFixer() {}

    public static List<BakedQuad> fix(List<BakedQuad> in) {
        if (in.size() < 2) return in;

        float[] c = boxCenter(in);
        List<BakedQuad> out = null;
        for (int i = 0; i < in.size(); i++) {
            BakedQuad q = in.get(i);
            BakedQuad fixed = isInwardWound(q, c) ? reverseWinding(q) : q;
            if (fixed != q && out == null) out = new ArrayList<>(in.subList(0, i));
            if (out != null) out.add(fixed);
        }
        return out != null ? out : in;
    }

    private static boolean isInwardWound(BakedQuad quad, float[] c) {
        int[] v = quad.getVertices();
        if (v.length < 4 * STRIDE) return false;
        float[] n = normal(v);
        float cx = (f(v,0,0)+f(v,1,0)+f(v,2,0)+f(v,3,0)) * 0.25f;
        float cy = (f(v,0,1)+f(v,1,1)+f(v,2,1)+f(v,3,1)) * 0.25f;
        float cz = (f(v,0,2)+f(v,1,2)+f(v,2,2)+f(v,3,2)) * 0.25f;
        float dot = n[0]*(cx-c[0]) + n[1]*(cy-c[1]) + n[2]*(cz-c[2]);
        return dot < -EPS;
    }

    private static BakedQuad reverseWinding(BakedQuad quad) {
        int[] src = quad.getVertices();
        int[] dst = src.clone();
        for (int k = 0; k < STRIDE; k++) {
            dst[1*STRIDE+k] = src[3*STRIDE+k];
            dst[3*STRIDE+k] = src[1*STRIDE+k];
        }
        return new BakedQuad(dst, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
    }

    private static float[] boxCenter(List<BakedQuad> quads) {
        float minx=1e9f,miny=1e9f,minz=1e9f,maxx=-1e9f,maxy=-1e9f,maxz=-1e9f;
        for (BakedQuad q : quads) {
            int[] v = q.getVertices();
            if (v.length < 4 * STRIDE) continue;
            for (int vi = 0; vi < 4; vi++) {
                float x=f(v,vi,0), y=f(v,vi,1), z=f(v,vi,2);
                if (x<minx) minx=x; if (x>maxx) maxx=x;
                if (y<miny) miny=y; if (y>maxy) maxy=y;
                if (z<minz) minz=z; if (z>maxz) maxz=z;
            }
        }
        return new float[]{ (minx+maxx)*0.5f, (miny+maxy)*0.5f, (minz+maxz)*0.5f };
    }

    private static float[] normal(int[] v) {
        float x0=f(v,0,0),y0=f(v,0,1),z0=f(v,0,2);
        float x1=f(v,1,0),y1=f(v,1,1),z1=f(v,1,2);
        float x2=f(v,2,0),y2=f(v,2,1),z2=f(v,2,2);
        float x3=f(v,3,0),y3=f(v,3,1),z3=f(v,3,2);
        float dx0=x2-x0,dy0=y2-y0,dz0=z2-z0, dx1=x3-x1,dy1=y3-y1,dz1=z3-z1;
        return new float[]{ dy0*dz1-dz0*dy1, dz0*dx1-dx0*dz1, dx0*dy1-dy0*dx1 };
    }

    private static float f(int[] v, int vertex, int component) {
        return Float.intBitsToFloat(v[vertex * STRIDE + component]);
    }
}