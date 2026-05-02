package com.p1nero.tcrcore.item.custom;

import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.TCRCapabilityProvider;
import com.p1nero.tcrcore.capability.TCRPlayer;
import com.p1nero.tcrcore.utils.FTBTeamUtils;
import com.p1nero.tcrcore.utils.WorldUtil;
import com.yesman.epicskills.registry.entry.EpicSkillsSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 多目标共鸣石，可同时搜索多个结构，全部成功时触发回调
 */
public class MultiResonanceStoneItem extends Item {

    /**
     * 单个搜索目标的配置属性
     */
    public static class TargetProperties {
        public final ResourceLocation targetStructure;
        public final ResourceKey<Level> dimension;
        public final int y;
        public final BiConsumer<BlockPos, ServerPlayer> callback;
        public final Predicate<ServerPlayer> predicate;
        public boolean ignoreFounded = true;

        public TargetProperties(ResourceLocation targetStructure, int y, ResourceKey<Level> dimension,
                                Predicate<ServerPlayer> predicate, BiConsumer<BlockPos, ServerPlayer> callback) {
            this.targetStructure = targetStructure;
            this.y = y;
            this.dimension = dimension;
            this.predicate = predicate;
            this.callback = callback;
        }

        public TargetProperties ignoreFounded(boolean ignoreFounded) {
            this.ignoreFounded = ignoreFounded;
            return this;
        }
    }

    private final List<TargetProperties> targets;
    private final Consumer<ServerPlayer> finalCallback;

    public MultiResonanceStoneItem(Properties properties, List<TargetProperties> targets, Consumer<ServerPlayer> finalCallback) {
        super(properties);
        this.targets = List.copyOf(targets); // 不可变副本
        this.finalCallback = finalCallback;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && !itemStack.getOrCreateTag().getBoolean("searching")) {
//            //仅队长可用
//            ServerPlayer leader = FTBTeamUtils.getTeamLeader(serverPlayer);
//            if(leader != null && leader != serverPlayer) {
//                serverPlayer.displayClientMessage(TCRCoreMod.getInfo("only_team_leader_can_use").withStyle(ChatFormatting.RED), false);
//                return InteractionResultHolder.fail(itemStack);
//            }

            // 开始搜索，防止重复使用
            itemStack.getOrCreateTag().putBoolean("searching", true);

            // 开始搜索音效
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    BuiltInRegistries.SOUND_EVENT.wrapAsHolder(EpicSkillsSounds.GAIN_ABILITY_POINTS.get()),
                    SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, player.getRandom().nextInt()));

            List<BlockPos> foundPositions = new ArrayList<>();
            boolean allSucceeded = true;

            // 依次处理每个目标
            for (TargetProperties prop : targets) {
                // 1. 检查前置条件
                if (!prop.predicate.test(serverPlayer)) {
                    player.displayClientMessage(TCRCoreMod.getInfo("can_not_do_this_too_early"), false);
                    allSucceeded = false;
                    break;
                }
                if (!level.dimension().equals(prop.dimension)) {
                    player.displayClientMessage(TCRCoreMod.getInfo("resonance_stone_wrong_dimension", prop.dimension.location().toString()).withStyle(ChatFormatting.RED), false);
                    allSucceeded = false;
                    break;
                }

                // 2. 搜索结构位置
                BlockPos pos = null;
                try {
                    pos = WorldUtil.getNearbyStructurePos(serverPlayer, prop.targetStructure.toString(), prop.y, prop.ignoreFounded);
                } catch (Exception e) {
                    TCRCoreMod.LOGGER.error("TCRCore : Error finding structure [{}]: ", prop.targetStructure, e);
                    player.displayClientMessage(TCRCoreMod.getInfo("resonance_search_failed", prop.targetStructure).withStyle(ChatFormatting.RED), false);
                    allSucceeded = false;
                    break;
                }

                if (pos == null) {
                    player.displayClientMessage(TCRCoreMod.getInfo("resonance_search_failed", prop.targetStructure).withStyle(ChatFormatting.RED), false);
                    allSucceeded = false;
                    break;
                }

                // 3. 处理Y坐标（表面修正）
                if (prop.y == ResonanceStoneItem.SURFACE) {
                    pos = WorldUtil.getSurfaceBlockPos(serverPlayer.serverLevel(), pos);
                }

                foundPositions.add(pos);
            }

            if (allSucceeded) {
                // 全部搜索成功，执行所有效果和回调
                TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(player);

                // 为每个目标播放粒子引导
                for (BlockPos pos : foundPositions) {
                    tcrPlayer.playDirectionParticle(player.getEyePosition(), new Vec3(pos.getX(), player.getEyeY(), pos.getZ()));
                }

                // 如果没有小地图mod，显示所有坐标
                if (!TCRCoreMod.isXaeroMapLoaded() && !TCRCoreMod.isJourneyMapLoaded()) {
                    for (int i = 0; i < targets.size(); i++) {
                        TargetProperties prop = targets.get(i);
                        BlockPos pos = foundPositions.get(i);
                        ResonanceStoneItem.handleNoXaeroMap(Component.literal(prop.targetStructure.toString()), pos, serverPlayer);
                    }
                }

                // 依次执行每个目标的回调
                for (int i = 0; i < targets.size(); i++) {
                    targets.get(i).callback.accept(foundPositions.get(i), serverPlayer);
                }

                // 执行最终回调
                finalCallback.accept(serverPlayer);

                // 消耗物品
                itemStack.shrink(1);

                // 成功音效
                serverPlayer.connection.send(new ClientboundSoundPacket(
                        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(EpicSkillsSounds.GAIN_ABILITY_POINTS.get()),
                        SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, player.getRandom().nextInt()));
            }

            // 清除搜索状态
            itemStack.getOrCreateTag().putBoolean("searching", false);
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
        }
        return InteractionResultHolder.pass(itemStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        super.appendHoverText(itemStack, level, list, flag);
        list.add(TCRCoreMod.getInfo("resonance_stone_usage").withStyle(ChatFormatting.GRAY));
        // 显示所有目标维度（去重后显示）
        targets.stream()
                .map(prop -> prop.dimension.location())
                .distinct()
                .forEach(dimLoc -> {
                    String dimensionNameKey = Util.makeDescriptionId("travelerstitles", dimLoc);
                    if (Language.getInstance().has(dimensionNameKey)) {
                        list.add(Component.translatable(dimensionNameKey));
                    } else {
                        list.add(Component.literal(dimLoc.toString()));
                    }
                });
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return true;
    }
}