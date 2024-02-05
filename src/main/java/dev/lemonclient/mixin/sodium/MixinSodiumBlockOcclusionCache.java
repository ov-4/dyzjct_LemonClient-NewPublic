package dev.lemonclient.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Xray;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockOcclusionCache.class, remap = false)
public class MixinSodiumBlockOcclusionCache {
    @Unique
    private Xray xray;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        xray = Modules.get().get(Xray.class);
    }

    @ModifyReturnValue(method = "shouldDrawSide", at = @At("RETURN"))
    private boolean shouldDrawSide(boolean original, BlockState state, BlockView view, BlockPos pos, Direction facing) {
        if (xray.isActive()) {
            return xray.modifyDrawSide(state, view, pos, facing, original);
        }

        return original;
    }
}