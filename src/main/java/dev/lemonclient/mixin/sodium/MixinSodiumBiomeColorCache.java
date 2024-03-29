package dev.lemonclient.mixin.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lemonclient.systems.modules.Modules;
import dev.lemonclient.systems.modules.render.Ambience;
import me.jellysquid.mods.sodium.client.world.biome.BiomeColorCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BiomeColorCache.class, remap = false)
public class MixinSodiumBiomeColorCache {
    @Unique
    private Ambience ambience;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        ambience = Modules.get().get(Ambience.class);
    }

    @ModifyExpressionValue(method = "updateColorBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getGrassColorAt(DD)I", remap = true))
    private int modify_getGrassColorAt(int color) {
        return ambience.isActive() && ambience.customGrassColor.get() ? ambience.grassColor.get().getPacked() : color;
    }

    @ModifyExpressionValue(method = "updateColorBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getFoliageColor()I", remap = true))
    private int modify_getFoliageColor(int color) {
        return ambience.isActive() && ambience.customFoliageColor.get() ? ambience.foliageColor.get().getPacked() : color;
    }

    @ModifyExpressionValue(method = "updateColorBuffers", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getWaterColor()I", remap = true))
    private int modify_getWaterColor(int color) {
        return ambience.isActive() && ambience.customWaterColor.get() ? ambience.waterColor.get().getPacked() : color;
    }
}
