package com.p1nero.tcrcore.network.packet.clientbound;

import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tcrcore.utils.XaeroWaypointUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record RemoveXaeroWaypointPacket(String name, BlockPos pos) implements BasePacket {
    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
    }

    public static RemoveXaeroWaypointPacket decode(FriendlyByteBuf buf) {
        String name = buf.readUtf();
        BlockPos blockPos = buf.readBlockPos();
        return new RemoveXaeroWaypointPacket(name, blockPos);
    }

    @Override
    public void execute(@Nullable Player playerEntity) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            XaeroWaypointUtils.removeWayPoint(pos, name);
        }
    }
}
