package com.p1nero.tcrcore.entity.custom.fake_npc.fake_boss;

import com.p1nero.dialog_lib.api.entity.goal.LookAtConservingPlayerGoal;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.client.sound.TCRSounds;
import com.p1nero.tcrcore.entity.custom.fake_npc.FakeNPCEntity;
import com.p1nero.tcrcore.utils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

/**
 * 只会哦咩爹多，哦完就似
 */
public class FakeBossNpc extends FakeNPCEntity {

    public FakeBossNpc(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookAtConservingPlayerGoal<>(this));
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public void tick() {
        super.tick();
        if(tickCount % 35 == 0) {
            if(!level().isClientSide) {
                level().playSound(null, getX(), getY(), getZ(), TCRSounds.CLAP.get(), SoundSource.PLAYERS, 0.1F, 1.0F);
            }
        }
        if(tickCount < 60) {
            this.getLookControl().setLookAt(new BlockPos(WorldUtils.BED_POS).above(1).getCenter());
        }
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if(player.getUUID().equals(this.getOwnerUUID()) || (!FMLEnvironment.production && player.isCreative())) {
            if( player instanceof ServerPlayer serverPlayer) {
                this.sendDialogTo(serverPlayer);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.FAIL;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogueScreen(CompoundTag compoundTag) {
        StreamDialogueScreenBuilder builder = new StreamDialogueScreenBuilder(this, TCRCoreMod.MOD_ID);
        builder.start(Component.literal("\n").append(TCRCoreMod.getInfo("congratulation")))
                .addFinalOption(Component.literal("..."), 1);
        return builder.build();
    }

    @Override
    public void handleNpcInteraction(ServerPlayer player, int i) {
//        if(i == 1) {
//            this.discard();
//            if (this.level() instanceof ServerLevel serverLevel) {
//                serverLevel.sendParticles(
//                        ParticleTypes.POOF,                     // 粒子类型：烟雾
//                        this.getX(),                             // X坐标
//                        this.getY() + this.getBbHeight() * 0.5,  // Y坐标（实体中部）
//                        this.getZ(),                             // Z坐标
//                        30,                                      // 粒子数量
//                        0.5, 0.5, 0.5,                           // 偏移范围（在实体周围半格内随机）
//                        0.1                                      // 粒子运动速度（轻微扩散）
//                );
//                serverLevel.sendParticles(
//                        ParticleTypes.CHERRY_LEAVES,
//                        this.getX(),
//                        this.getY() + this.getBbHeight() * 0.5,
//                        this.getZ(),
//                        20,
//                        0.6, 0.6, 0.6,
//                        0.0
//                );
//                EntityUtil.playLocalSound(player, EpicSkillsSounds.GAIN_ABILITY_POINTS.get());
//            }
//        }
        setConversingPlayer(null);
    }

    public boolean shouldRender(Player player) {
        return player.getUUID().equals(this.getOwnerUUID()) || (player.isCreative() && !FMLEnvironment.production);
    }

    @Override
    public @NotNull Component getName() {
        return  Component.literal("???");
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
