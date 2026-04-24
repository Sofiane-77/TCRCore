package com.p1nero.tcrcore.mixin.epicfight;

import com.p1nero.tcrcore.capability.PlayerDataManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

@Mixin(SkillContainer.class)
public abstract class SkillContainerMixin {

    @Shadow(remap = false) public abstract Skill getSkill();

    @Inject(method = "requestCasting", at = @At("HEAD"), remap = false)
    private void tcr$executeOnServer(ServerPlayerPatch executor, FriendlyByteBuf buf, CallbackInfoReturnable<Boolean> cir){
        ServerPlayer serverPlayer = executor.getOriginal();
        if(this.getSkill().getCategory() == SkillCategories.WEAPON_INNATE) {
            if(!PlayerDataManager.weapon_innate_used.get(serverPlayer)) {
                PlayerDataManager.weapon_innate_used.put(serverPlayer, true);
            }
        }
    }
}
