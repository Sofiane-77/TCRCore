package com.p1nero.tcrcore.dialog.extension;

import com.github.L_Ender.cataclysm.init.ModItems;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.api.entity.EntityDialogueExtension;
import com.p1nero.dialog_lib.api.entity.IEntityDialogueExtension;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.dpr.DodgeParryRewardMod;
import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.PlayerDataManager;
import com.p1nero.tcrcore.capability.TCRQuestManager;
import com.p1nero.tcrcore.capability.TCRQuests;
import com.p1nero.tcrcore.gameassets.TCRSkills;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.PlayTitlePacket;
import com.p1nero.tcrcore.utils.FTBTeamUtils;
import com.p1nero.tcrcore.utils.ItemUtils;
import com.yesman.epicskills.skilltree.SkillTree;
import com.yesman.epicskills.world.capability.SkillTreeProgression;
import com.yungnickyoung.minecraft.ribbits.entity.RibbitEntity;
import com.yungnickyoung.minecraft.ribbits.module.EntityTypeModule;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@EntityDialogueExtension(modId = TCRCoreMod.MOD_ID)
public class RibbitDialogExtension implements IEntityDialogueExtension<RibbitEntity> {

    @Override
    public EntityType<RibbitEntity> getEntityType() {
        return EntityTypeModule.RIBBIT.get();
    }

    @Override
    public boolean canInteractWith(Player player, RibbitEntity ribbitEntity) {
        return true;
    }

    /**
     * 拿紫水晶块的时候不能对话，其他全由自己的对话接管
     */
    @Override
    public InteractionResult shouldCancelInteract(Player player, RibbitEntity currentTalking, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        return (player.isSecondaryUseActive() && itemStack.is(Items.AMETHYST_SHARD)) ? null : InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    @Override
    public CompoundTag getServerData(ServerPlayer player, RibbitEntity currentTalking, InteractionHand hand, CompoundTag senderData) {
        if(player.getMainHandItem().is(Items.AMETHYST_BLOCK)) {
            senderData.putBoolean("hasAmethyst", true);
            senderData.putInt("amethystCount", player.getMainHandItem().getCount());
        }
        boolean canTrade = currentTalking.isAlive() && !currentTalking.isBaby() && !currentTalking.isTrading() && !currentTalking.isSleeping() && !currentTalking.getOffers().isEmpty();
        senderData.putBoolean("canTrade", canTrade);
        return senderData;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogScreen(StreamDialogueScreenBuilder streamDialogueScreenBuilder, LocalPlayer localPlayer, RibbitEntity ribbitEntity, CompoundTag compoundTag) {
        DialogueComponentBuilder builder = new DialogueComponentBuilder(ribbitEntity, TCRCoreMod.MOD_ID);
        boolean hasAmethyst = compoundTag.getBoolean("hasAmethyst");
        int count = compoundTag.getInt("amethystCount");
        boolean canTrade = compoundTag.getBoolean("canTrade");
        DialogNode root = new DialogNode(builder.ans(0))
                .addLeaf(builder.opt(0))
                .addLeaf(builder.opt(1));

        if(canTrade) {
            root.addLeaf(builder.opt(2), 2);
        }

        if(TCRQuestManager.hasQuest(localPlayer, TCRQuests.RIBBITS_QUEST)) {
            DialogNode aboutEye = new DialogNode(builder.ans(1), builder.opt(3, ModItems.ABYSS_EYE.get().getDescription()))
                    .addLeaf(builder.opt(-2), 3);
            root.addChild(aboutEye);
        } else if(TCRQuestManager.hasQuest(localPlayer, TCRQuests.GIVE_AMETHYST_BLOCK_TO_RIBBITS)){
            if(hasAmethyst) {
                if(count < 12) {
                    DialogNode tradeFailed = new DialogNode(builder.ans(3), builder.opt(4, Items.AMETHYST_BLOCK.getDescription()))
                            .addLeaf(builder.opt(-2));
                    root.addChild(tradeFailed);
                } else {
                    DialogNode tradeSuccess = new DialogNode(builder.ans(2),builder.opt(4, Items.AMETHYST_BLOCK.getDescription()))
                            .addChild(new DialogNode(builder.ans(4), builder.opt(-1))
                                    .addChild(new DialogNode(builder.ans(5), builder.opt(-1))
                                            .addLeaf(builder.opt(5), 1)));
                    root.addChild(tradeSuccess);
                }
            }
        }
        return streamDialogueScreenBuilder.buildWith(root);
    }

    @Override
    public void handleNpcInteraction(RibbitEntity ribbitEntity, ServerPlayer serverPlayer, int i) {
        //解锁避水咒
        if(i == 1) {
            TCRQuests.GIVE_AMETHYST_BLOCK_TO_RIBBITS.finish(serverPlayer, true);
            serverPlayer.getMainHandItem().shrink(12);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (player -> {
                ItemUtils.addItemEntity(player, artifacts.registry.ModItems.CHARM_OF_SINKING.get(), 1);

                player.getCapability(SkillTreeProgression.SKILL_TREE_PROGRESSION).ifPresent(skillTreeProgression -> {
                    ResourceKey<SkillTree> resourceKey = ResourceKey.create(SkillTree.SKILL_TREE_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DodgeParryRewardMod.MOD_ID, "passive"));
                    skillTreeProgression.unlockTree(resourceKey, player);
                    skillTreeProgression.unlockNode(resourceKey, TCRSkills.WATER_AVOID, player);
                });
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_SKILL), player);
                player.displayClientMessage(TCRCoreMod.getInfo("unlock_new_skill", Component.translatable(TCRSkills.WATER_AVOID.getTranslationKey()).withStyle(ChatFormatting.AQUA)), false);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
                PlayerDataManager.waterAvoidUnlocked.put(player, true);
            }));
        }
        if(i == 2) {
            //开始交易
            ribbitEntity.getOffers();
            ribbitEntity.setTradingPlayer(serverPlayer);
            ribbitEntity.openTradingScreen(serverPlayer, ribbitEntity.getDisplayName(), 0);
        }
        if(i == 3) {
            TCRQuests.RIBBITS_QUEST.finish(serverPlayer, true);
            TCRQuests.GIVE_AMETHYST_BLOCK_TO_RIBBITS.start(serverPlayer);
        }
        removeConservingPlayer(ribbitEntity);
    }

}
