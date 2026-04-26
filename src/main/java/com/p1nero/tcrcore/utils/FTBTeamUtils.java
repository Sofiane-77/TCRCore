package com.p1nero.tcrcore.utils;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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

    public static Team getTeam(ServerPlayer player) {
        return FTBTeamsAPI.api().getManager().getTeamForPlayer(player).orElse(null);
    }

}
