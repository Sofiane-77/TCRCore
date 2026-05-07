package com.p1nero.tcrcore.mixin.epicfight;

import com.p1nero.tcr_bosses.entity.custom.BaseSmallBossEntity;
import com.p1nero.tcrcore.utils.EntityUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;

import java.util.List;

/**
 * 简单群怪优化
 */
@Mixin(value = AnimatedAttackGoal.class)
public abstract class AnimatedAttackGoalMixin <T extends MobPatch<?>> extends Goal {
    
    @Shadow(remap = false) @Final protected T mobpatch;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tcr$tick(CallbackInfo ci) {
        if(tcr$check()) {
            ci.cancel();
        }
    }

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void tcr$canUse(CallbackInfoReturnable<Boolean> cir) {
        if(tcr$check()) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean tcr$check() {
        List<Entity> list = EntityUtils.getNearByEntities(this.mobpatch.getOriginal(), 6);
        if(this.mobpatch.getOriginal() instanceof BaseSmallBossEntity) {
            return false;
        }
        if(list.stream().anyMatch(entity -> {
            if(entity instanceof Enemy) {
                LivingEntityPatch<?> livingEntityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
                if(livingEntityPatch == null) {
                    return false;
                }
                if(this.mobpatch.getTarget() != null && this.mobpatch.getTarget() == livingEntityPatch.getTarget()) {
                    if(this.mobpatch.getTarget().distanceTo(this.mobpatch.getOriginal()) < this.mobpatch.getTarget().distanceTo(livingEntityPatch.getOriginal())) {
                        return false;
                    }
                }
                return livingEntityPatch.getEntityState().inaction();
            }
            return false;
        })) {
            this.mobpatch.getEntityState().setState(EntityState.INACTION, true);
            Vec3 dir = this.mobpatch.getOriginal().getViewVector(1.0F).normalize().scale(-0.2F);
            this.mobpatch.getOriginal().setDeltaMovement(dir.x, this.mobpatch.getOriginal().getDeltaMovement().y, dir.z);
            return true;
        }
        return false;
    }

}
