package com.p1nero.tcrcore.entity.custom.tutorial_humanoid;

import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.entity.custom.tutorial_golem.TutorialGolem;
import com.p1nero.tcrcore.utils.WorldUtils;
import net.kenddie.fantasyarmor.item.FAArmorItems;
import net.kenddie.fantasyarmor.item.armor.FAArmorSet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class TutorialHumanoid extends PathfinderMob {

    private int hasTargetTick;

    public TutorialHumanoid(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0f)
                .add(Attributes.ATTACK_DAMAGE, 0.01f)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.3f)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .build();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(source.getEntity() instanceof LivingEntity living) {
            this.setTarget(living);
        }
        return super.hurt(source, value);
    }

    @Override
    public void die(@NotNull DamageSource damageSource) {
        if(damageSource.isCreativePlayer()) {
            super.die(damageSource);
        }
        this.setHealth(this.getMaxHealth());
    }

    @Override
    public void tick() {
        super.tick();
        if(this.getMainHandItem().isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
            this.setItemSlot(EquipmentSlot.HEAD, FAArmorItems.getArmorItem(FAArmorSet.TWINNED, ArmorItem.Type.HELMET).get().getDefaultInstance());
            this.setItemSlot(EquipmentSlot.CHEST, FAArmorItems.getArmorItem(FAArmorSet.TWINNED, ArmorItem.Type.CHESTPLATE).get().getDefaultInstance());
            this.setItemSlot(EquipmentSlot.LEGS, FAArmorItems.getArmorItem(FAArmorSet.TWINNED, ArmorItem.Type.LEGGINGS).get().getDefaultInstance());
            this.setItemSlot(EquipmentSlot.FEET, FAArmorItems.getArmorItem(FAArmorSet.TWINNED, ArmorItem.Type.BOOTS).get().getDefaultInstance());
        }
        if(!level().isClientSide) {
            if(this.distanceToSqr(WorldUtils.GOLEM_CENTER_POS_VEC3) > 70 * 70) {
                Vec3 dir = WorldUtils.GOLEM_CENTER_POS_VEC3.subtract(this.position()).normalize();
                Vec3 targetPos = this.position().add(dir.scale(30));
                this.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0F);
            }
            if(this.getTarget() instanceof TutorialGolem) {
                this.setTarget(null);
            }
            if(this.getTarget() != null) {
                hasTargetTick++;
            }
            if(hasTargetTick > 200) {
                hasTargetTick = 0;
                this.setTarget(null);
            }

            if(this.getTarget() instanceof ServerPlayer serverPlayer && this.tickCount % 40 == 0) {
                serverPlayer.displayClientMessage(TCRCoreMod.getInfo("after_heal_stop_attack"), true);
            }
        }
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public boolean removeWhenFarAway(double p_21542_) {
        return false;
    }
}
