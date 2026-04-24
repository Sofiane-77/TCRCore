package com.p1nero.tcrcore.mixin.ba_bt;

import com.brass_amber.ba_bt.util.BTTags;
import com.brass_amber.ba_bt.worldGen.structures.OceanTower;
import com.brass_amber.ba_bt.worldGen.structures.TowerStructure;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 简化定位，虽然位置不准但是搜索更快
 */
@Mixin(OceanTower.class)
public abstract class OceanTowerMixin extends Structure implements TowerStructure {

    protected OceanTowerMixin(StructureSettings p_226558_) {
        super(p_226558_);
    }

    @Shadow(remap = false)
    public abstract void checkVariant(Structure.GenerationContext context, BlockPos blockpos);

    /**
     * 优化海洋塔生成逻辑
     * 核心改进：用 5 点采样代替原版的半径 80 全量生物群系扫描。
     */
    @Inject(method = "isSpawnableChunk", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$isSpawnableChunk(Structure.GenerationContext generationContext, CallbackInfoReturnable<Pair<Boolean, Integer>> cir) {
        ChunkPos chunkPos = generationContext.chunkPos();
        ChunkGenerator chunkGen = generationContext.chunkGenerator();
        int seaLevel = chunkGen.getSeaLevel();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        // 1. 快速检查：如果中心点都不是海洋塔允许的生物群系，直接跳过
        Holder<Biome> centerBiome = generationContext.biomeSource().getNoiseBiome(
                QuartPos.fromBlock(centerX), 
                QuartPos.fromBlock(seaLevel), 
                QuartPos.fromBlock(centerZ), 
                generationContext.randomState().sampler()
        );

        if (!generationContext.validBiome().test(centerBiome)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        // 2. 边界检查：采样 80 格半径的四个方位，确保都在深海/海洋中
        // 原版是为了防止塔生成在离岸太近的地方，采样 5 个点足以代表周围环境
        if (!tcr$isSurroundedByOcean(generationContext, centerX, centerZ, seaLevel, 80)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        // 3. 成功通过检测，判定变体
        BlockPos spawnPos = chunkPos.getMiddleBlockPosition(seaLevel);
        this.checkVariant(generationContext, spawnPos);
        
        cir.setReturnValue(Pair.of(true, seaLevel));
    }

    /**
     * 内部方法：通过 5 点采样确保周围是合法的海洋生物群系
     */
    @Unique
    private boolean tcr$isSurroundedByOcean(Structure.GenerationContext context, int x, int z, int y, int radius) {
        BiomeSource source = context.biomeSource();
        var sampler = context.randomState().sampler();
        
        // 采样点：中心, 北, 南, 东, 西
        int[][] points = {
            {0, 0}, {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}
        };

        for (int[] p : points) {
            Holder<Biome> b = source.getNoiseBiome(
                QuartPos.fromBlock(x + p[0]), 
                QuartPos.fromBlock(y), 
                QuartPos.fromBlock(z + p[1]), 
                sampler
            );
            
            // 必须符合海洋塔生物群系标签 (通常是 Deep Ocean)
            if (!b.is(BTTags.Biomes.OCEAN_TOWER_BIOMES)) {
                return false;
            }
        }
        return true;
    }
}