package com.p1nero.tcrcore.mixin.iss;

import com.p1nero.tcrcore.utils.ItemUtil;
import io.redspace.ironsspellbooks.gui.overlays.CastBarOverlay;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CastBarOverlay.class)
public class CastBarOverlayMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$render(ForgeGui gui, GuiGraphics guiHelper, float partialTick, int screenWidth, int screenHeight, CallbackInfo ci) {
        if(ClientMagicData.isCasting() && ClientMagicData.getCastDuration() <= 1) {
            if(Minecraft.getInstance().player != null && ItemUtil.isBetterMagicWeaponItems(Minecraft.getInstance().player.getMainHandItem())) {
                ci.cancel();
            }
        }
    }

}
