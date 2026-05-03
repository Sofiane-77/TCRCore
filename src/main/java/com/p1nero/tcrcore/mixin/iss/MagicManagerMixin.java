package com.p1nero.tcrcore.mixin.iss;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import org.spongepowered.asm.mixin.Mixin;

/**
 * 仅主城才能回蓝
 * OUT
 */
@Mixin(MagicManager.class)
public class MagicManagerMixin {

//    @Inject(method = "regenPlayerMana", at = @At("HEAD"), cancellable = true, remap = false)
//    private void tcr$regenPlayerMana(ServerPlayer serverPlayer, MagicData playerMagicData, CallbackInfoReturnable<Boolean> cir) {
//        if(!WorldUtil.inMainLand(serverPlayer)) {
//            cir.setReturnValue(false);
//        }
//    }

}
