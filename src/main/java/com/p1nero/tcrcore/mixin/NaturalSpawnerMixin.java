package com.p1nero.tcrcore.mixin;

import com.p1nero.battle_field1.worldgen.PBF1Dimensions;
import com.p1nero.tcrcore.worldgen.TCRDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NaturalSpawner.class)
public class NaturalSpawnerMixin {

    @Inject(method = "spawnForChunk", at = @At("HEAD"), cancellable = true)
    private static void tcr$spawnForChunk(ServerLevel pLevel, LevelChunk pChunk, NaturalSpawner.SpawnState pSpawnState, boolean pSpawnFriendlies, boolean pSpawnMonsters, boolean pForcedDespawn, CallbackInfo ci) {
        if(pLevel.dimension() == TCRDimensions.SANCTUM_LEVEL_KEY || pLevel.dimension() == TCRDimensions.REAL_LEVEL_KEY || pLevel.dimension() == PBF1Dimensions.SANCTUM_OF_THE_BATTLE_LEVEL_KEY) {
            ci.cancel();
        }
    }

}
