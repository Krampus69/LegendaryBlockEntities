package com.krampus.legendaryblockentities.mixin;

import com.krampus.legendaryblockentities.util.duck.SignTextStateHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin implements SignTextStateHolder {

    @Shadow public abstract SignText getFrontText();

    @Shadow public abstract SignText getBackText();

    @Unique private SignText lbe$cachedFront;
    @Unique private SignText lbe$cachedBack;
    @Unique private boolean lbe$cachedFiltered;
    @Unique private boolean lbe$hasText;

    @Override
    public boolean lbe$hasRenderableText() {
        SignText front = this.getFrontText();
        SignText back = this.getBackText();
        boolean filtered = Minecraft.getInstance().isTextFilteringEnabled();

        if (front != this.lbe$cachedFront || back != this.lbe$cachedBack
                || filtered != this.lbe$cachedFiltered) {
            this.lbe$cachedFront = front;
            this.lbe$cachedBack = back;
            this.lbe$cachedFiltered = filtered;
            this.lbe$hasText = lbe$scan(front, filtered) || lbe$scan(back, filtered);
        }
        return this.lbe$hasText;
    }

    @Unique
    private static boolean lbe$scan(SignText text, boolean filtered) {
        if (text == null) return false;
        for (Component line : text.getMessages(filtered)) {
            if (line != null && !line.getString().isEmpty()) return true;
        }
        return false;
    }
}
