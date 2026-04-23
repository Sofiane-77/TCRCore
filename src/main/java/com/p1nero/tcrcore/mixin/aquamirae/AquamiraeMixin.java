package com.p1nero.tcrcore.mixin.aquamirae;

import com.obscuria.aquamirae.Aquamirae;
import net.minecraftforge.event.TickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 防止在海洋塔也冷冻，判断结构感觉消耗太大了干脆直接杀了
 */
@Mixin(Aquamirae.class)
public class AquamiraeMixin {

    @Inject(method = "onPlayerTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$onPlayerTick(TickEvent.PlayerTickEvent event, CallbackInfo ci) {
        ci.cancel();
    }

}
