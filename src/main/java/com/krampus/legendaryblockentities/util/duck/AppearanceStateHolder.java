package com.krampus.legendaryblockentities.util.duck;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;

public interface AppearanceStateHolder {
    int lbe$getModelState();
    void lbe$setModelState(int state);

    default void lbe$setModelStateAndRebuild(int state, Level level, BlockPos pos) {
        if (this.lbe$getModelState() == state) return;
        this.lbe$setModelState(state);
        if (level != null && level.isClientSide()) {
            LevelRenderer lr = Minecraft.getInstance().levelRenderer;
            if (lr != null) {
                Reflect.dirtySync(lr, pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4);
            }
        }
    }

    final class Reflect {
        private static Method SET_SECTION_DIRTY_4;
        static {
            try {
                SET_SECTION_DIRTY_4 = LevelRenderer.class.getDeclaredMethod(
                        "setSectionDirty", int.class, int.class, int.class, boolean.class);
                SET_SECTION_DIRTY_4.setAccessible(true);
            } catch (NoSuchMethodException e) {
                SET_SECTION_DIRTY_4 = null;
            }
        }
        static void dirtySync(LevelRenderer lr, int sx, int sy, int sz) {
            try {
                if (SET_SECTION_DIRTY_4 != null) SET_SECTION_DIRTY_4.invoke(lr, sx, sy, sz, true);
                else lr.setSectionDirty(sx, sy, sz);
            } catch (ReflectiveOperationException ignored) {
                lr.setSectionDirty(sx, sy, sz);
            }
        }
    }
}
