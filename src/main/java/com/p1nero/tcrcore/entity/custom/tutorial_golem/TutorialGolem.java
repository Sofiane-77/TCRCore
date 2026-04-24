package com.p1nero.tcrcore.entity.custom.tutorial_golem;

import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.PlayerDataManager;
import com.p1nero.tcrcore.entity.custom.tutorial_humanoid.TutorialHumanoid;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.PlayTitlePacket;
import com.p1nero.tcrcore.utils.WorldUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.LinkedList;
import java.util.Queue;

public class TutorialGolem extends IronGolem {

    private int damageNumberPos;

    private static final int DPS_WINDOW_TICKS = 60;
    private final Queue<DamageRecord> damageRecords = new LinkedList<>();

    private record DamageRecord(int tick, float damage) {}

    public TutorialGolem(EntityType<? extends IronGolem> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0f)
                .add(Attributes.ATTACK_DAMAGE, 0.01f)
                .add(Attributes.ATTACK_SPEED, 1.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.3f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 114514f)
                .build();
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (this.distanceToSqr(WorldUtil.GOLEM_CENTER_POS_VEC3) > 70 * 70) {
                Vec3 dir = WorldUtil.GOLEM_CENTER_POS_VEC3.subtract(this.position()).normalize();
                Vec3 targetPos = this.position().add(dir.scale(30));
                this.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, 1.0F);
            }

            // 清理超出时间窗口的伤害记录
            int currentTick = tickCount;
            while (!damageRecords.isEmpty() && currentTick - damageRecords.peek().tick > DPS_WINDOW_TICKS) {
                damageRecords.poll();
            }
        }
    }

    public void addTotalDamage(float value) {
        damageRecords.add(new DamageRecord(tickCount, value));
    }

    public float getTotalDamagePerSecond() {
        if (damageRecords.isEmpty()) {
            return 0.0F;
        }
        int currentTick = tickCount;
        float totalDamage = 0.0F;
        int earliestTick = Integer.MAX_VALUE;
        for (DamageRecord record : damageRecords) {
            totalDamage += record.damage;
            if (record.tick < earliestTick) {
                earliestTick = record.tick;
            }
        }
        int timeSpanTicks = Math.min(currentTick - earliestTick, DPS_WINDOW_TICKS);
        if (timeSpanTicks < 0) {
            return 0.0F;
        }
        float timeSeconds = timeSpanTicks / 20.0F;
        //不足1直接算总和，防止分母太小了
        if(timeSeconds < 1) {
            return totalDamage;
        }
        return totalDamage / timeSeconds;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(source.getEntity() instanceof ServerPlayer serverPlayer) {
            if(PlayerDataManager.locked.get(serverPlayer)) {
                this.addEffect(new MobEffectInstance(EpicFightMobEffects.INSTABILITY.get(), 200, 1));
            } else {
                return false;
            }
        } else {
            return false;
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
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 0.9D, 32.0F));
        this.goalSelector.addGoal(4, new GolemRandomStrollInVillageGoal(this, 0.6D));
        this.goalSelector.addGoal(5, new OfferFlowerGoal(this));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::shouldAttack));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    /**
     * 完成试炼了才不会追着玩家
     * 挨打就当作试炼中
     */
    private boolean shouldAttack(LivingEntity living) {
        if(this.hasEffect(EpicFightMobEffects.INSTABILITY.get())) {
            return true;
        }
        if(living instanceof TutorialHumanoid) {
            return false;
        }
        if(living instanceof ServerPlayer serverPlayer) {
            return !PlayerDataManager.dodged.get(serverPlayer) ||
                    !PlayerDataManager.parried.get(serverPlayer) ||
//                    !PlayerDataManager.weapon_innate_used.get(serverPlayer)||
                    !PlayerDataManager.locked.get(serverPlayer);
        }
        return false;
    }

    /**
     * 没完成就不断生提示
     */
    @Override
    public void baseTick() {
        super.baseTick();
        if(this.getTarget() instanceof ServerPlayer serverPlayer && this.tickCount % 40 == 0) {
            if(this.hasEffect(EpicFightMobEffects.INSTABILITY.get())) {
                serverPlayer.displayClientMessage(TCRCoreMod.getInfo("after_heal_stop_attack"), true);
                return;
            }
            if(!PlayerDataManager.dodged.get(serverPlayer)) {
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.DODGE_TUTORIAL), serverPlayer);
            } else if(!PlayerDataManager.parried.get(serverPlayer)) {
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.PARRY_TUTORIAL), serverPlayer);
//            } else if(!PlayerDataManager.weapon_innate_used.get(serverPlayer)) {
//                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(TCRCoreMod.getInfo("weapon_innate_tutorial")));
//                serverPlayer.displayClientMessage(TCRCoreMod.getInfo("weapon_innate_charge_tutorial"), true);
            } else if(!PlayerDataManager.locked.get(serverPlayer)) {
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.LOCK_TUTORIAL), serverPlayer);
            } else if (!PlayerDataManager.tutorial_passed.get(serverPlayer)){
                serverPlayer.connection.send(new ClientboundSetTitleTextPacket(TCRCoreMod.getInfo("you_pass")));
                serverPlayer.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), SoundSource.PLAYERS, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), 1.0F, 1.0F, serverPlayer.getRandom().nextInt()));
                this.setTarget(null);
                PlayerDataManager.tutorial_passed.put(serverPlayer, true);
            } else {
                this.setTarget(null);
            }
        }
    }

    public int getNextNumberPos() {
        return this.damageNumberPos++;
    }
}
