package com.p1nero.tcrcore.skills;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener;

import java.util.UUID;

public class FireAvoidSkill extends Skill {
    private static final UUID EVENT_UUID = UUID.fromString("11bd2c59-fe77-11ed-be56-0242ac191981");

    public FireAvoidSkill(SkillBuilder<? extends Skill> builder) {
        super(builder);
    }

    @Override
    public void onInitiate(SkillContainer container) {
        super.onInitiate(container);
        container.getExecutor().getEventListener().addEventListener(PlayerEventListener.EventType.TAKE_DAMAGE_EVENT_ATTACK, EVENT_UUID, (event) -> {
            if(event.getDamageSource().is(DamageTypeTags.IS_FIRE) || event.getDamageSource().is(DamageTypes.LAVA)) {
                event.setCanceled(true);
                event.setResult(AttackResult.ResultType.MISSED);
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer container) {
        super.onRemoved(container);
        container.getExecutor().getEventListener().removeListener(PlayerEventListener.EventType.DODGE_SUCCESS_EVENT, EVENT_UUID);
    }

    @Override
    public void updateContainer(SkillContainer container) {
        super.updateContainer(container);
        container.getExecutor().getOriginal().clearFire();
    }
}
