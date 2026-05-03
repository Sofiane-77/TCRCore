package com.p1nero.tcrcore.network.packet.serverbound;

import com.p1nero.battle_field1.worldgen.PBF1Dimensions;
import com.p1nero.dialog_lib.network.packet.BasePacket;
import com.p1nero.tcrcore.utils.WorldUtils;
import com.p1nero.tcrcore.worldgen.TCRDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.merlin204.wraithon.util.PositionTeleporter;

public record ExitSpectatorPacket() implements BasePacket {

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    public static ExitSpectatorPacket decode(FriendlyByteBuf buf){
        return new ExitSpectatorPacket();
    }

    @Override
    public void execute(Player player) {
        if(player instanceof ServerPlayer serverPlayer){
            if(serverPlayer.level().dimension() == PBF1Dimensions.SANCTUM_OF_THE_BATTLE_LEVEL_KEY) {
                if(serverPlayer.isSpectator()) {
                    ServerLevel sanctum = serverPlayer.server.getLevel(TCRDimensions.SANCTUM_LEVEL_KEY);
                    if(sanctum != null) {
                        player.changeDimension(sanctum, new PositionTeleporter(new BlockPos(WorldUtils.START_POS)));
                    }
                    serverPlayer.setGameMode(GameType.SURVIVAL);
                }
            }
        }
    }
}
