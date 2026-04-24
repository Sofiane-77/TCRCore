package com.p1nero.tcrcore.gameassets;

import com.hm.efn.client.sound.EFNSounds;
import com.hm.efn.entity.EFNVFXManagers;
import com.p1nero.p1nero_ec.effect.PECEffects;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.client.sound.TCRSounds;
import com.p1nero.tcrcore.utils.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import reascer.wom.animation.attacks.UltimateAttackAnimation;
import reascer.wom.gameasset.WOMSkills;
import reascer.wom.gameasset.WOMSounds;
import reascer.wom.gameasset.colliders.WOMWeaponColliders;
import reascer.wom.main.WeaponsOfMinecraft;
import reascer.wom.particle.WOMParticles;
import reascer.wom.skill.WOMSkillDataKeys;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.Joint;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.property.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.collider.MultiOBBCollider;
import yesman.epicfight.api.collider.OBBCollider;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.api.utils.TimePairList;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.skill.SkillDataKey;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageTypeTags;
import yesman.epicfight.world.damagesource.StunType;

import java.util.Random;
import java.util.Set;

@Mod.EventBusSubscriber(modid = TCRCoreMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TCRAnimations {
    public static AnimationManager.AnimationAccessor<DodgeAnimation> STEP_B;
    public static AnimationManager.AnimationAccessor<DodgeAnimation> STEP_F;
    public static AnimationManager.AnimationAccessor<DodgeAnimation> STEP_L;
    public static AnimationManager.AnimationAccessor<DodgeAnimation> STEP_R;
    public static AnimationManager.AnimationAccessor<AttackAnimation> TSUNAMI;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SCYTHE_HARVEST;
    public static AnimationManager.AnimationAccessor<StaticAnimation> CLAP;
    public static AnimationManager.AnimationAccessor<AttackAnimation> SOLAR_BRASERO_OBSCURIDAD;

    @SubscribeEvent
    public static void registerAnimations(AnimationManager.AnimationRegistryEvent event) {
        event.newBuilder(TCRCoreMod.MOD_ID, (builder) -> {
            AssetAccessor<? extends HumanoidArmature> biped = Armatures.BIPED;
            CLAP = builder.nextAccessor("biped/living/clap", staticAnimationAnimationAccessor ->
                    new StaticAnimation(true, staticAnimationAnimationAccessor, biped));
            STEP_F = builder.nextAccessor("dodge/step_forward", (accessor) ->
                    new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
                            .addState(EntityState.LOCKON_ROTATE, true)
                            .newTimePair(0.0F, 0.2F)
                            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
                            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
                            .addEvents(AnimationEvent.InTimeEvent.create(0.0F,
                                    Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT)));
            STEP_B = builder.nextAccessor("dodge/step_backward", (accessor) ->
                    new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
                            .addState(EntityState.LOCKON_ROTATE, true)
                            .newTimePair(0.0F, 0.2F)
                            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
                            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
                            .addEvents(AnimationEvent.InTimeEvent.create(0.0F,
                                    Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT)));
            STEP_L = builder.nextAccessor("dodge/step_left", (accessor) ->
                    new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
                            .addState(EntityState.LOCKON_ROTATE, true)
                            .newTimePair(0.0F, 0.2F)
                            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
                            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
                            .addEvents(AnimationEvent.InTimeEvent.create(0.0F,
                                    Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT)));
            STEP_R = builder.nextAccessor("dodge/step_right", (accessor) ->
                    new DodgeAnimation(0.1F, 0.35F, accessor, 0.6F, 1.65F, Armatures.BIPED)
                            .addState(EntityState.LOCKON_ROTATE, true)
                            .newTimePair(0.0F, 0.2F)
                            .addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false)
                            .addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false)
                            .addEvents(AnimationEvent.InTimeEvent.create(0.0F,
                                    Animations.ReusableSources.PLAY_STEPPING_SOUND, AnimationEvent.Side.CLIENT)));
            TSUNAMI = builder.nextAccessor("skill/tsunami_reinforced", (accessor) ->
                    (new AttackAnimation(0.1F, 0.2F, 0.35F, 0.65F, 0.8F, ColliderPreset.BIPED_BODY_COLLIDER, Armatures.BIPED.get().rootJoint, accessor, Armatures.BIPED))
                            .addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.adder(10.0F))
                            .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_BEGIN, MoveCoordFunctions.RAW_COORD_WITH_X_ROT)
                            .addProperty(AnimationProperty.ActionAnimationProperty.COORD_SET_TICK, null)
                            .addProperty(AnimationProperty.ActionAnimationProperty.MOVE_VERTICAL, true)
                            .addProperty(AnimationProperty.ActionAnimationProperty.NO_GRAVITY_TIME, TimePairList.create(0.15F, 0.85F))
                            .addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, Animations.ReusableSources.CONSTANT_ONE)
                            .addProperty(AnimationProperty.StaticAnimationProperty.POSE_MODIFIER, Animations.ReusableSources.ROOT_X_MODIFIER)
                            .addEvents(AnimationProperty.StaticAnimationProperty.ON_END_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESTORE_BOUNDING_BOX, AnimationEvent.Side.BOTH), AnimationEvent.SimpleEvent.create(((livingEntityPatch, assetAccessor, animationParameters) -> {
                                if (livingEntityPatch.getOriginal() instanceof Player player) {
                                    if (player.isInWater()) {
                                        player.setSwimming(true);
                                        player.setSprinting(true);
                                    }
                                }
                            }), AnimationEvent.Side.BOTH))
                            .addEvents(AnimationProperty.StaticAnimationProperty.TICK_EVENTS, AnimationEvent.SimpleEvent.create(Animations.ReusableSources.RESIZE_BOUNDING_BOX, AnimationEvent.Side.BOTH)
                                    .params(EntityDimensions.scalable(0.6F, 1.0F)))
                            .newTimePair(0.0F, 0.8F)
                            .addStateRemoveOld(EntityState.ATTACK_RESULT, (source -> AttackResult.ResultType.BLOCKED))
                            .addEvents(AnimationEvent.InPeriodEvent.create(0.35F, 1.0F, (entitypatch, animation, params) -> {
                                Vec3 pos = entitypatch.getOriginal().position();
                                for (int x = -1; x <= 1; x += 2) {
                                    for (int z = -1; z <= 1; z += 2) {
                                        Vec3 rand = (new Vec3(Math.random() * x, Math.random(), Math.random() * z)).normalize().scale(2.0F);
                                        entitypatch.getOriginal().level().addParticle(EpicFightParticles.TSUNAMI_SPLASH.get(), pos.x + rand.x, pos.y + rand.y - 1.0F, pos.z + rand.z, rand.x * 0.1, rand.y * 0.1, rand.z * 0.1);
                                    }
                                }
                            }, AnimationEvent.Side.CLIENT))
                            .addEvents(new AnimationEvent[]{AnimationEvent.InTimeEvent.create(0.35F, (entitypatch, animation, params) ->
                                    entitypatch.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 0.0F, 0.0F), AnimationEvent.Side.CLIENT),
                                    AnimationEvent.InTimeEvent.create(0.35F, (entitypatch, animation, params) ->
                                            entitypatch.setAirborneState(true), AnimationEvent.Side.SERVER)}));
            AnimationProperty.PlaybackSpeedModifier SKILL = (self, entityPatch, speed, prevElapsedTime, elapsedTime) -> 1.0F;

            MultiOBBCollider SCYTHE_COLLIDER = new MultiOBBCollider(new OBBCollider(0.25F, 0.25F, 1.3, 0.0F, 0.0F, 1.0F), new OBBCollider(0.25F, 0.25F, 1.3, 0.0F, 0.0F, 1.0F), new OBBCollider(0.7, 0.9, 0.7, 0.0F, -0.5F, -0.45), new OBBCollider(0.7, 0.9, 0.7, 0.0F, -0.5F, -0.45));

            Joint mainHand = Armatures.BIPED.get().toolR;
            SCYTHE_HARVEST = builder.nextAccessor("biped/scythe/skill/scythe_harvest", (accessor) -> (new AttackAnimation(0.1F, accessor, Armatures.BIPED, (new AttackAnimation.Phase(0.0F, 0.66F, 0.81F, 0.81F, 0.81F, mainHand, SCYTHE_COLLIDER)).addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent) EFNSounds.WHOOSH_HEAVY_4.get()), (new AttackAnimation.Phase(0.81F, 0.81F, 1.0F, 1.0F, 1.0F, mainHand, SCYTHE_COLLIDER)).addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, (SoundEvent) EFNSounds.WHOOSH_HEAVY_4.get()), (new AttackAnimation.Phase(1.0F, 1.05F, 1.21F, 2.13F, 2.13F, mainHand, SCYTHE_COLLIDER)).addProperty(AnimationProperty.AttackPhaseProperty.SWING_SOUND, EFNSounds.WHOOSH_HEAVY_2.get()))).addProperty(AnimationProperty.StaticAnimationProperty.PLAY_SPEED_MODIFIER, SKILL).newTimePair(0.0F, 1.63F).addStateRemoveOld(EntityState.CAN_BASIC_ATTACK, false).newTimePair(0.0F, 1.83F).addStateRemoveOld(EntityState.CAN_SKILL_EXECUTION, false).addEvents(new AnimationEvent[]{EFNVFXManagers.summonScytheComboVFX(EFNVFXManagers.CRIMSON_SLASH, 25, 0.5F, -0.5F, 0.0F, 4.0F, new Vec3f(0.0F, 0.0F, 17.0F)), EFNVFXManagers.summonScytheComboVFX(EFNVFXManagers.CRIMSON_SLASH, 35, 0.5F, -0.5F, 0.0F, 4.0F, new Vec3f(0.0F, 0.0F, -17.0F))}));

            SOLAR_BRASERO_OBSCURIDAD = builder.nextAccessor("biped/skill/solar_brasero_obscuridad", (accessor) -> (new AttackAnimation(0.2F, accessor, biped, new AttackAnimation.Phase(0.0F, 0.55F, 0.65F, 0.75F, Float.MAX_VALUE, (biped.get()).rootJoint, WOMWeaponColliders.SOLAR_INFIERNO)).addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(1.0F)).addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(0.5F)).addProperty(AnimationProperty.AttackPhaseProperty.MAX_STRIKES_MODIFIER, ValueModifier.multiplier(4.0F)).addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.NONE).addProperty(AnimationProperty.AttackPhaseProperty.SOURCE_TAG, Set.of(EpicFightDamageTypeTags.WEAPON_INNATE)).addProperty(AnimationProperty.AttackPhaseProperty.PARTICLE, WOMParticles.SOLAR_POLVORA_HIT).addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, (SoundEvent) WOMSounds.SOLAR_HIT.get()).addProperty(AnimationProperty.AttackAnimationProperty.BASIS_ATTACK_SPEED, 1.6F).addProperty(AnimationProperty.AttackAnimationProperty.FIXED_MOVE_DISTANCE, true).addEvents(

                    AnimationEvent.InTimeEvent.create(0.55F, (entitypatch, self, params) -> {
                        if (entitypatch instanceof ServerPlayerPatch serverPlayerPatch) {
                            serverPlayerPatch.modifyLivingMotionByCurrentItem(false);
                            entitypatch.getOriginal().level().playSound(null, entitypatch.getOriginal(), WOMSounds.SOLAR_HIT.get(), SoundSource.MASTER, 0.7F, 0.5F);
                        }

                    }, AnimationEvent.Side.SERVER),

                    AnimationEvent.InTimeEvent.create(0.55F, (entitypatch, self, params) -> {
                        OpenMatrix4f transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(0.0F), (Armatures.BIPED.get()).toolR);
                        transformMatrix.translate(new Vec3f(-0.2F, 0.0F, 0.4F));
                        OpenMatrix4f.mul((new OpenMatrix4f()).rotate(-org.joml.Math.toRadians((entitypatch.getOriginal()).yBodyRotO + 180.0F), new Vec3f(0.0F, 1.0F, 0.0F)), transformMatrix, transformMatrix);
                        int n = 70;
                        double r = 0.1;

                        for (int i = 0; i < n; ++i) {
                            double theta = (Math.PI * 2D) * (new Random()).nextDouble();
                            double phi = org.joml.Math.acos(2.0F * (new Random()).nextDouble() - 1.0F);
                            double x = r * org.joml.Math.sin(phi) * org.joml.Math.cos(theta);
                            double y = r * org.joml.Math.sin(phi) * org.joml.Math.sin(theta);
                            double z = r * org.joml.Math.cos(phi);
                            entitypatch.getOriginal().level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, transformMatrix.m30 + (entitypatch.getOriginal()).getX(), transformMatrix.m31 + (entitypatch.getOriginal()).getY() + ((new Random()).nextFloat() * 2.9F), transformMatrix.m32 + (entitypatch.getOriginal()).getZ(), ((float) x), ((float) y), ((float) z));
                            if (i % 2 == 0) {
                                entitypatch.getOriginal().level().addParticle(ParticleTypes.LAVA, transformMatrix.m30 + (entitypatch.getOriginal()).getX(), transformMatrix.m31 + (entitypatch.getOriginal()).getY() + ((new Random()).nextFloat() * 2.9F), transformMatrix.m32 + (entitypatch.getOriginal()).getZ(), ((float) x), ((float) y), ((float) z));
                            }
                        }

                        transformMatrix = entitypatch.getArmature().getBoundTransformFor(entitypatch.getAnimator().getPose(0.0F), (Armatures.BIPED.get()).toolR);
                        OpenMatrix4f.mul((new OpenMatrix4f()).rotate(-org.joml.Math.toRadians((entitypatch.getOriginal()).yBodyRotO + 180.0F), new Vec3f(0.0F, 1.0F, 0.0F)), transformMatrix, transformMatrix);
                        transformMatrix.translate(new Vec3f(0.0F, 0.0F, -0.3F));
                        n = 80;
                        r = 0.4;
                        double t = 0.01;

                        for (int i = 0; i < n; ++i) {
                            double theta = (Math.PI * 2D) * (new Random()).nextDouble();
                            double phi = ((new Random()).nextDouble() - 0.5F) * Math.PI * t / r;
                            double x = r * org.joml.Math.cos(phi) * org.joml.Math.cos(theta);
                            double y = r * org.joml.Math.cos(phi) * org.joml.Math.sin(theta);
                            double z = r * org.joml.Math.sin(phi);
                            Vec3f direction = new Vec3f((float) x, (float) y, (float) z);
                            OpenMatrix4f rotation = (new OpenMatrix4f()).rotate(org.joml.Math.toRadians(-((LivingEntity) entitypatch.getOriginal()).yBodyRotO + 90.0F), new Vec3f(0.0F, 1.0F, 0.0F));
                            rotation.rotate(org.joml.Math.toRadians(110.0F), new Vec3f(1.0F, 0.0F, 0.0F));
                            OpenMatrix4f.transform3v(rotation, direction, direction);
                            entitypatch.getOriginal().level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, transformMatrix.m30 + (entitypatch.getOriginal()).getX(), transformMatrix.m31 + ((LivingEntity) entitypatch.getOriginal()).getY(), transformMatrix.m32 + ((LivingEntity) entitypatch.getOriginal()).getZ(), direction.x, direction.y, direction.z);
                        }

                        for (int i = 0; i < n; ++i) {
                            double theta = (Math.PI * 2D) * (new Random()).nextDouble();
                            double phi = ((new Random()).nextDouble() - 0.5F) * Math.PI * t / r;
                            double x = r * org.joml.Math.cos(phi) * org.joml.Math.cos(theta);
                            double y = r * org.joml.Math.cos(phi) * org.joml.Math.sin(theta);
                            double z = r * org.joml.Math.sin(phi);
                            Vec3f direction = new Vec3f((float) x, (float) y, (float) z);
                            OpenMatrix4f rotation = (new OpenMatrix4f()).rotate(org.joml.Math.toRadians(-(entitypatch.getOriginal()).yBodyRotO + 90.0F), new Vec3f(0.0F, 1.0F, 0.0F));
                            rotation.rotate(org.joml.Math.toRadians(70.0F), new Vec3f(1.0F, 0.0F, 0.0F));
                            OpenMatrix4f.transform3v(rotation, direction, direction);
                            entitypatch.getOriginal().level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, transformMatrix.m30 + (entitypatch.getOriginal()).getX(), transformMatrix.m31 + ((LivingEntity) entitypatch.getOriginal()).getY(), transformMatrix.m32 + (entitypatch.getOriginal()).getZ(), direction.x, direction.y, direction.z);
                        }

                    }, AnimationEvent.Side.CLIENT),
                    AnimationEvent.InTimeEvent.create(0.55F, (entityPatch, animation, params) -> {
                        entityPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                        entityPatch.getOriginal().addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                        entityPatch.getOriginal().addEffect(new MobEffectInstance(PECEffects.SOUL_INCINERATOR.get(), 400, 1));
                    }, AnimationEvent.Side.SERVER)))
                    .setResourceLocation(WeaponsOfMinecraft.MODID, "biped/skill/solar_brasero_obscuridad"));

        });
    }
}
