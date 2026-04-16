package com.p1nero.tcrcore.mixin.ba_bt;

import com.brass_amber.ba_bt.worldGen.structures.CoreTower;
import com.brass_amber.ba_bt.worldGen.structures.TowerStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.mojang.datafixers.util.Pair;

@Mixin(CoreTower.class)
public abstract class CoreTowerMixin extends Structure implements TowerStructure {

    protected CoreTowerMixin(StructureSettings p_226558_) {
        super(p_226558_);
    }

    @Inject(method = "getTowerType", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$getTowerType(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }

    @Shadow(remap = false)
    public abstract void checkVariant(Structure.GenerationContext context, BlockPos blockpos);

    /**
     * 優化後的檢測邏輯
     * 使用固定點位採樣代替原版的 findBiomeHorizontal 圓形掃描
     */
    @Inject(method = "isSpawnableChunk", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$isSpawnableChunk(Structure.GenerationContext generationContext, CallbackInfoReturnable<Pair<Boolean, Integer>> cir) {
        ChunkPos chunkPos = generationContext.chunkPos();
        ChunkGenerator chunkGen = generationContext.chunkGenerator();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int middleHeight = chunkGen.getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, generationContext.heightAccessor(), generationContext.randomState()
        );

        Holder<Biome> centerBiome = generationContext.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(centerX), QuartPos.fromBlock(middleHeight), QuartPos.fromBlock(centerZ), generationContext.randomState().sampler()
        );

        if (!generationContext.validBiome().test(centerBiome)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        if (tcr$isOceanNearbyFast(generationContext, centerX, centerZ, 77)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        BlockPos blockpos = chunkPos.getMiddleBlockPosition(middleHeight);
        this.checkVariant(generationContext, blockpos);
        cir.setReturnValue(Pair.of(true, middleHeight));
    }

    /**
     * 快速海洋採樣器
     * 只檢查 9 個點，而不是原版的幾千個點
     */
    @Unique
    private boolean tcr$isOceanNearbyFast(Structure.GenerationContext context, int x, int z, int radius) {
        BiomeSource source = context.biomeSource();
        var sampler = context.randomState().sampler();
        int seaLevel = context.chunkGenerator().getSeaLevel();

        // 定義採樣偏移量 (中心, 四正, 四隅)
        int[][] samples = {
                {0, 0}, {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius},
                {radius, radius}, {-radius, -radius}, {radius, -radius}, {-radius, radius}
        };

        for (int[] offset : samples) {
            Holder<Biome> biome = source.getNoiseBiome(
                    QuartPos.fromBlock(x + offset[0]),
                    QuartPos.fromBlock(seaLevel),
                    QuartPos.fromBlock(z + offset[1]),
                    sampler
            );

            // 如果採樣點中有任何一個是海洋，返回 true
            if (biome.is(BiomeTags.IS_OCEAN)) {
                return true;
            }
        }
        return false;
    }

}
