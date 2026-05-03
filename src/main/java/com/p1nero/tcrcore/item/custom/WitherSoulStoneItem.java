package com.p1nero.tcrcore.item.custom;

import com.p1nero.battle_field1.PBF1Mod;
import com.p1nero.battle_field1.worldgen.PBF1Dimensions;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.utils.WorldUtils;
import com.p1nero.tcrcore.worldgen.TCRDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.merlin204.wraithon.util.PositionTeleporter;

import java.util.List;
import java.util.Random;

public class WitherSoulStoneItem extends SimpleDescriptionItem {

    private final Random random = new Random();

    public WitherSoulStoneItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!player.isUsingItem() && !level.isClientSide) {
            level.playSound(null, player, SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.1F, 2.0F);
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack itemStack) {
        return 60;
    }

    public void onUseTick(Level level, LivingEntity entity, ItemStack itemStack, int remainingTicks) {
        if (level.isClientSide) {
            int duration = this.getUseDuration(itemStack);
            float progress = (float) (duration - remainingTicks) / (float) duration;
            boolean shouldMirror = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ^ entity.getMainArm() == HumanoidArm.RIGHT;
            Vec3 handOffset = new Vec3(shouldMirror ? (double) 0.3F : (double) -0.3F, (double) 1.0F, (double) 0.52F);
            handOffset = handOffset.yRot(-entity.getYRot() * ((float) Math.PI / 180F));
            handOffset = handOffset.zRot(entity.getXRot() * ((float) Math.PI / 180F));
            int maxParticles = Math.max(4, (int) (progress * 48.0F));
            if (remainingTicks % 5 == 0) {
                for (int i = 0; i < Math.min(4, maxParticles); ++i) {
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, entity.getX() + handOffset.x + (this.random.nextDouble() - (double) 0.5F) * (double) 0.5F, entity.getY() + handOffset.y + this.random.nextDouble(), entity.getZ() + handOffset.z + (this.random.nextDouble() - (double) 0.5F) * (double) 0.5F, (double) 0.0F, (double) 0.05F, (double) 0.0F);
                }

                if (progress >= 0.25F) {
                    for (int i = 0; i < maxParticles; ++i) {
                        level.addParticle(ParticleTypes.CRIMSON_SPORE, entity.getX() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, entity.getY() + this.random.nextDouble(), entity.getZ() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, (double) 0.0F, this.random.nextDouble() * (double) 0.5F, (double) 0.0F);
                    }
                }

                if (progress >= 0.5F) {
                    for (int i = 0; i < maxParticles; ++i) {
                        level.addParticle(ParticleTypes.REVERSE_PORTAL, entity.getX() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, entity.getY() + this.random.nextDouble(), entity.getZ() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, (double) 0.0F, this.random.nextDouble(), (double) 0.0F);
                    }
                }

                if (progress >= 0.75F) {
                    for (int i = 0; i < maxParticles / 3; ++i) {
                        level.addParticle(ParticleTypes.WITCH, entity.getX() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, entity.getY() + (double) 0.5F + this.random.nextDouble(), entity.getZ() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, (double) 0.0F, this.random.nextDouble(), (double) 0.0F);
                    }
                }
            }

            if (remainingTicks == 1) {
                for (int i = 0; i < maxParticles; ++i) {
                    level.addParticle(ParticleTypes.REVERSE_PORTAL, entity.getX() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, entity.getY() + this.random.nextDouble() + (double) 1.0F, entity.getZ() + (this.random.nextDouble() - (double) 0.5F) * (double) 1.5F, (this.random.nextDouble() - (double) 0.5F) * (double) 0.0F, this.random.nextDouble(), (this.random.nextDouble() - (double) 0.5F) * (double) 0.0F);
                }
            }
        }

    }

    /**
     * 处理传送
     */
    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if(entity instanceof ServerPlayer player) {
            player.getCooldowns().addCooldown(this, 100);
            ServerLevel serverLevel = player.serverLevel();
            ServerLevel sanctum = player.server.getLevel(TCRDimensions.SANCTUM_LEVEL_KEY);
            ServerLevel targetWorld = player.server.getLevel(PBF1Dimensions.SANCTUM_OF_THE_BATTLE_LEVEL_KEY);
            if(serverLevel.dimension().equals(PBF1Dimensions.SANCTUM_OF_THE_BATTLE_LEVEL_KEY) && sanctum != null) {
                player.changeDimension(sanctum, new PositionTeleporter(new BlockPos(WorldUtils.START_POS)));
            } else if(targetWorld != null){
                boolean hasNonCreativeOrSpectator = targetWorld.players().stream()
                        .anyMatch(p -> !p.isCreative() && !p.isSpectator());
                if(!player.isCreative() && !player.isSpectator() && hasNonCreativeOrSpectator) {
                    player.displayClientMessage(TCRCoreMod.getInfo("dim_max_players"), true);
                    player.setGameMode(GameType.SPECTATOR);
                }
                player.changeDimension(targetWorld, new PositionTeleporter(new BlockPos(PBF1Mod.START_POS)));
            }
        }

        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        list.add(Component.translatable(this.getDescriptionId() + ".usage", WorldUtils.SAMSARA_NAME.withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.BOW;
    }
}
