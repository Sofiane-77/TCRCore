package com.p1nero.tcrcore.mixin.mmmmmmmmmmmm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.p1nero.tcrcore.entity.custom.tutorial_golem.TutorialGolem;
import net.mehvahdjukaar.dummmmmmy.network.ClientBoundDamageNumberMessage;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientBoundDamageNumberMessage.class)
public class ClientBoundDamageNumberMessageMixin {

    @WrapOperation(method = "handle", at = @At(value = "INVOKE", target = "Lnet/mehvahdjukaar/dummmmmmy/network/ClientBoundDamageNumberMessage;spawnParticle(Lnet/minecraft/world/entity/Entity;I)V"), remap = false)
    private void tcr$handle(ClientBoundDamageNumberMessage instance, Entity entity, int animationPos, Operation<Void> original) {
        if(entity instanceof TutorialGolem tutorialGolem) {
            original.call(instance, entity, tutorialGolem.getNextNumberPos());
        } else {
            original.call(instance, entity, animationPos);
        }
    }
}
