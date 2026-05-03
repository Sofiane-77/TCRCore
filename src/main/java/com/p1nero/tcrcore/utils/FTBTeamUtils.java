package com.p1nero.tcrcore.utils;

import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.TCRCapabilityProvider;
import com.p1nero.tcrcore.capability.TCRPlayer;
import com.p1nero.tcrcore.capability.TCRQuestManager;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.RefreshClientQuestsPacket;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 用于FTBTeams的一些操作
 */
public class FTBTeamUtils {

    @Nullable
    public static ServerPlayer getTeamLeader(ServerPlayer player) {
        Team team = getTeam(player);
        if(team == null) {
            return null;
        }
        UUID ownerId = team.getOwner();
        return (ServerPlayer) player.serverLevel().getPlayerByUUID(ownerId);
    }

    @Nullable
    public static Team getTeam(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
    }

    public static void onlineTeamMembersDoWithSelf(ServerPlayer player, Consumer<ServerPlayer> consumer) {
        onlineTeamMembersDo(player, consumer, false);
    }

    public static void onlineTeamMembersDo(ServerPlayer player, Consumer<ServerPlayer> consumer) {
        onlineTeamMembersDo(player, consumer, true);
    }

    public static void onlineTeamMembersDo(ServerPlayer player, Consumer<ServerPlayer> consumer, boolean ignoreSelf) {
        if(!ignoreSelf) {
            consumer.accept(player);//防止没团队？虽然一个人默认就一个团队，但是以防万一
        }
        FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(team -> {
            team.getOnlineMembers().forEach(member -> {
                if(member == player) {
                    return;
                }
                consumer.accept(member);
            });
        });
    }

    /**
     * 同步团队进度给新人，前提是默认团队所有人进度都一样= =
     */

    public static boolean syncDataFromTeam(ServerPlayer serverPlayer) {
        return syncDataFromTeam(serverPlayer, getTeam(serverPlayer));
    }

    public static boolean syncDataFromTeam(ServerPlayer serverPlayer, Team team) {
        if(serverPlayer == null || team == null) {
            return false;
        }
        TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(serverPlayer);
        if(!team.getOnlineMembers().isEmpty()) {
            ServerPlayer oldPlayer = new ArrayList<>(team.getOnlineMembers()).get(0);
            if(oldPlayer == serverPlayer) {
                return false;
            }
            ServerPlayer toBroadcast;
            if(tcrPlayer.copyFromFTBTeamMember(oldPlayer)) {
                toBroadcast = serverPlayer;
            } else {
                toBroadcast = oldPlayer;
            }
            toBroadcast.displayClientMessage(TCRCoreMod.getInfo("team_progress_synced").withStyle(ChatFormatting.GOLD), false);
            TCRQuestManager.ensureQuest(toBroadcast);
            tcrPlayer.syncToClient(toBroadcast);
            PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new RefreshClientQuestsPacket(), toBroadcast);
            return true;
        }
        return false;
    }

    /**
     * 把进度同步给团队其他人，比如用于祈福后
     */
    public static void syncDataToTeamMembers(ServerPlayer serverPlayer) {
        TCRPlayer self = TCRCapabilityProvider.getTCRPlayer(serverPlayer);
        FTBTeamsAPI.api().getManager().getTeamForPlayer(serverPlayer).ifPresent(team -> {
            team.getOnlineMembers().forEach(member -> {
                if(member != serverPlayer) {
                    TCRPlayer memberData = TCRCapabilityProvider.getTCRPlayer(member);
                    memberData.copyFromFTBTeamMember(self);
                    memberData.syncToClient(member);
                }
            });
        });
    }

}
