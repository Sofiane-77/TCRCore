package com.p1nero.tcrcore.mixin.aquamirae;

import com.obscuria.aquamirae.common.entities.Maw;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Maw.class)
public class MawMixin {

    /**
     * Fuck maw
     */
    @Inject(method = "randomMawItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$randomMawItem(CallbackInfo ci) {
        ci.cancel();
    }

}
