package com.p1nero.tcrcore.mixin.epicfight;

import com.p1nero.tcrcore.utils.ISSUtils;
import com.p1nero.tcrcore.utils.ItemUtil;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.magic.SpellSelectionManager;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.skill.BasicAttack;
import yesman.epicfight.skill.SkillContainer;

@Mixin(BasicAttack.class)
public class BasicAttackMixin {

    @Inject(method = "executeOnServer", at = @At("TAIL"), remap = false)
    private void tcr$executeOnServer(SkillContainer skillContainer, FriendlyByteBuf args, CallbackInfo ci) {
        ServerPlayer serverPlayer = skillContainer.getServerExecutor().getOriginal();
        //触发魔纹技能
        if(ItemUtil.isBetterMagicWeaponItems(serverPlayer.getMainHandItem())) {
            boolean dashAttack = serverPlayer.isSprinting();
            boolean airAttack = !serverPlayer.onGround() && !serverPlayer.isInWater();
            //仅限冲刺和跳跃攻击可以
            if(!dashAttack && !airAttack) {
                return;
            }
            SpellSelectionManager ssm = new SpellSelectionManager(serverPlayer);
            SpellSelectionManager.SelectionOption spellItem = ssm.getSelection();
            if (spellItem != null) {
                SpellData spellData = ssm.getSelectedSpellData();
                if (spellData != SpellData.EMPTY) {
                    MagicData playerMagicData = MagicData.getPlayerMagicData(serverPlayer);
                    if (playerMagicData.isCasting()) {
                        return;
                    }
                    int level = spellData.getSpell().getLevelFor(spellData.getLevel(), serverPlayer);
                    int newLevel = level / 2;
                    if (newLevel > 0) {
                        int castTime = 1;
                        if(spellData.getSpell().getCastType() == CastType.CONTINUOUS) {
                            AnimationPlayer animationPlayer = skillContainer.getServerExecutor().getAnimator().getPlayerFor(null);
                            if(animationPlayer != null) {
                                float time = animationPlayer.getRealAnimation().get().getTotalTime();
                                castTime = (int) (time * 20);//s转tick
                                if(castTime < 1) {
                                    castTime = 10;
                                }
                            }
                        }
                        ISSUtils.attemptInitiateCast(spellData.getSpell(), ItemStack.EMPTY, newLevel, serverPlayer.level(), serverPlayer, spellItem.getCastSource(), spellItem.slot, false, castTime);
                    }
                }
            }
        }
    }

    /**
     * Fuck Stamina
     */
    @Inject(method = "checkConsumption", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$checkConsumption(SkillContainer container, boolean dash, boolean air, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}
