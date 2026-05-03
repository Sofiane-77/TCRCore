package com.p1nero.tcrcore.utils;

import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.AddXaeroWaypointPacket;
import com.p1nero.tcrcore.network.packet.clientbound.RemoveXaeroWaypointPacket;
import dev.ftb.mods.ftbteams.api.Team;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import xaero.common.XaeroMinimapSession;
import xaero.common.core.IXaeroMinimapClientPlayNetHandler;
import xaero.common.minimap.waypoints.Waypoint;
import xaero.common.minimap.waypoints.WaypointVisibilityType;
import xaero.common.minimap.waypoints.WaypointsManager;
import xaero.hud.minimap.waypoint.WaypointColor;
import xaero.hud.minimap.waypoint.WaypointPurpose;
import xaero.map.controls.ControlsRegister;
import xaero.minimap.XaeroMinimap;

import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class XaeroWaypointUtils {

    public static void sendWaypoint(ServerPlayer player, String key, Component displayName, BlockPos pos, WaypointColor color) {
        sendWaypoint(player, key, displayName, pos, color, WaypointVisibilityType.LOCAL);
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, WaypointColor color, WaypointVisibilityType type) {
        sendWaypoint(player, key, Component.empty(), pos, color, type);
    }

    public static void sendWaypoint(ServerPlayer player, String key, BlockPos pos, WaypointColor color) {
        sendWaypoint(player, key, pos, color, WaypointVisibilityType.LOCAL);
    }

    public static void sendWaypoint(ServerPlayer player, String key, Component displayName, BlockPos pos, WaypointColor color, WaypointVisibilityType type) {
        PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new AddXaeroWaypointPacket(key, displayName, pos, color, type), player);
        //全队共享任务标点
        Team team = FTBTeamUtils.getTeam(player);
        if(team != null) {
            team.getOnlineMembers().forEach(serverPlayer -> {
                if(serverPlayer != player) {
                    PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new AddXaeroWaypointPacket(key, displayName, pos, color, type), serverPlayer);
                }
            });
        }
    }

    public static void removeWaypoint(ServerPlayer player, String key, BlockPos pos) {
        PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new RemoveXaeroWaypointPacket(key, pos), player);
        //全队共享任务标点
        Team team = FTBTeamUtils.getTeam(player);
        if(team != null) {
            team.getOnlineMembers().forEach(serverPlayer -> {
                if(serverPlayer != player) {
                    PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new RemoveXaeroWaypointPacket(key, pos), serverPlayer);
                }
            });
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void addWayPoint(BlockPos pos, String name, @Nullable Component displayName, @Nullable WaypointColor color, WaypointVisibilityType type){
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            ArrayList<Waypoint> waypoints = getWaypoints(Minecraft.getInstance().player);
            if(waypoints.stream().anyMatch((waypoint -> isEqualWaypoint(waypoint, name, pos)))){
                return;
            }
            String text;
            if(displayName != null && !displayName.getContents().toString().equals("empty")) {
                text = displayName.getString();
            } else {
                text = name;
            }
            Waypoint instant = new Waypoint(pos.getX(), pos.getY() + 2, pos.getZ(), text, text.substring(0, 2), color == null ? xaero.hud.minimap.waypoint.WaypointColor.getRandom() : color , WaypointPurpose.NORMAL, false);
            instant.setVisibility(type);
            waypoints.add(instant);
            save(Minecraft.getInstance().player);
            Minecraft.getInstance().player.displayClientMessage(TCRCoreMod.getInfo("map_pos_marked_press_to_open", ControlsRegister.keyOpenMap.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GOLD)), false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void removeWayPoint(BlockPos pos, String name){
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            getWaypoints(Minecraft.getInstance().player).removeIf(waypoint -> isEqualWaypoint(waypoint, name, pos));
            save(Minecraft.getInstance().player);
        }
    }

    public static boolean isEqualWaypoint(Waypoint waypoint, String name, BlockPos pos) {
        return waypoint.getName().equals(name) && waypoint.getX() == pos.getX() && waypoint.getY() == pos.getY() + 2 && waypoint.getZ() == pos.getZ();
    }

    @OnlyIn(Dist.CLIENT)
    public static ArrayList<Waypoint> getWaypoints(LocalPlayer localPlayer){
        IXaeroMinimapClientPlayNetHandler clientLevel = (IXaeroMinimapClientPlayNetHandler) (localPlayer.connection);
        XaeroMinimapSession session = clientLevel.getXaero_minimapSession();
        WaypointsManager waypointsManager = session.getWaypointsManager();
        return waypointsManager.getWaypoints().getList();
    }

    @OnlyIn(Dist.CLIENT)
    public static WaypointsManager getWaypointManager(LocalPlayer localPlayer){
        IXaeroMinimapClientPlayNetHandler clientLevel = (IXaeroMinimapClientPlayNetHandler) (localPlayer.connection);
        XaeroMinimapSession session = clientLevel.getXaero_minimapSession();
        return session.getWaypointsManager();
    }

    @OnlyIn(Dist.CLIENT)
    public static void save(LocalPlayer localPlayer) {
        try {
            XaeroMinimap.instance.getSettings().saveWaypoints(getWaypointManager(localPlayer).getCurrentWorld());
        } catch (IOException ignored) {

        }
    }

}
