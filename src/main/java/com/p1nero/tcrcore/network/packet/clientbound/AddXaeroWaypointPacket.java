package com.p1nero.tcrcore.network.packet.clientbound;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tcrcore.utils.XaeroWaypointUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import xaero.common.minimap.waypoints.WaypointVisibilityType;
import xaero.hud.minimap.waypoint.WaypointColor;

public record AddXaeroWaypointPacket(String name, Component displayName, BlockPos pos, @Nullable WaypointColor color, WaypointVisibilityType type) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeComponent(displayName);
        buf.writeBlockPos(pos);
        if(color == null) {
            buf.writeUtf("null");
        } else {
            buf.writeUtf(color.name());
        }
        buf.writeEnum(type);
    }

    public static AddXaeroWaypointPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        Component displayName = buf.readComponent();
        BlockPos blockPos = buf.readBlockPos();
        String color = buf.readUtf();
        WaypointVisibilityType type = buf.readEnum(WaypointVisibilityType.class);
        return new AddXaeroWaypointPacket(name, displayName, blockPos, color.equals("null") ? null : WaypointColor.valueOf(color), type);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            XaeroWaypointUtils.addWayPoint(pos, name, displayName, color, type);
        }
    }
}
