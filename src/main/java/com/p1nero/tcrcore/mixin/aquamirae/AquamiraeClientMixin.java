package com.p1nero.tcrcore.mixin.aquamirae;

import com.obscuria.aquamirae.AquamiraeClient;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AquamiraeClient.class)
public class AquamiraeClientMixin {
    @Inject(method = "playCorneliaMusic", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tcr$playCorneliaMusic(Player player, CallbackInfo ci) {
        ci.cancel();
    }
}
