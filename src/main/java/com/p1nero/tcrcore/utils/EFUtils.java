package com.p1nero.tcrcore.utils;

import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.PlayTitlePacket;
import com.yesman.epicskills.skilltree.SkillTree;
import com.yesman.epicskills.world.capability.SkillTreeProgression;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import yesman.epicfight.skill.Skill;

public class EFUtils {

    public static void unlockSkillOnSkillTree(ServerPlayer serverPlayer, ResourceLocation resourceKey, Skill skill) {
        unlockSkillOnSkillTree(serverPlayer, ResourceKey.create(SkillTree.SKILL_TREE_REGISTRY_KEY, resourceKey), skill);
    }

    public static void unlockSkillOnSkillTree(ServerPlayer serverPlayer, ResourceKey<SkillTree> resourceKey, Skill skill) {
        final ServerPlayer finalServerPlayer = serverPlayer;
        serverPlayer.getCapability(SkillTreeProgression.SKILL_TREE_PROGRESSION).ifPresent(skillTreeProgression -> {
            skillTreeProgression.unlockNode(resourceKey, skill, finalServerPlayer);
        });
        PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_SKILL), serverPlayer);
        serverPlayer.displayClientMessage(TCRCoreMod.getInfo("unlock_new_skill", Component.translatable(skill.getTranslationKey()).withStyle(ChatFormatting.GOLD)), false);
        EntityUtils.playLocalSound(serverPlayer, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
    }

}
