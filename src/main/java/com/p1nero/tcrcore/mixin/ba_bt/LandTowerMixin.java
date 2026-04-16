package com.p1nero.tcrcore.mixin.ba_bt;

import com.brass_amber.ba_bt.util.BTTags;
import com.brass_amber.ba_bt.worldGen.structures.LandTower;
import com.brass_amber.ba_bt.worldGen.structures.TowerStructure;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandTower.class)
public abstract class LandTowerMixin extends Structure implements TowerStructure {

    @Shadow(remap = false)
    private int towerType;

    protected LandTowerMixin(StructureSettings p_226558_) {
        super(p_226558_);
    }

    @Inject(method = "getTowerTypeConversion", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$getTowerTypeConversion(CallbackInfoReturnable<String[]> cir) {
        cir.setReturnValue(new String[]{"normal", "icy"});
    }

    /**
     * 重寫地形與海洋檢測邏輯
     * 優化點：減少採樣點，移除 findBiomeHorizontal
     */
    @Inject(method = "isSpawnableChunk", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$isSpawnableChunk(Structure.GenerationContext context, CallbackInfoReturnable<Pair<Boolean, Integer>> cir) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator chunkGen = context.chunkGenerator();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        // 1. 快速地形採樣：僅採樣 5 個點（中心 + 四角）而不是原版的 324 個點
        int centerH = chunkGen.getFirstOccupiedHeight(centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int h1 = chunkGen.getFirstOccupiedHeight(centerX - 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int h2 = chunkGen.getFirstOccupiedHeight(centerX + 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int h3 = chunkGen.getFirstOccupiedHeight(centerX - 8, centerZ + 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int h4 = chunkGen.getFirstOccupiedHeight(centerX + 8, centerZ - 8, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

        int minH = Math.min(centerH, Math.min(Math.min(h1, h2), Math.min(h3, h4)));
        int maxH = Math.max(centerH, Math.max(Math.max(h1, h2), Math.max(h3, h4)));

        // 平坦度判斷 (原版閾值為 15)
        if (maxH - minH > 15 || minH > 215) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        // 2. 生物群系標籤檢測
        Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(centerX), QuartPos.fromBlock(centerH), QuartPos.fromBlock(centerZ), context.randomState().sampler());
        if (!context.validBiome().test(biome)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        // 3. 快速海洋採樣 (半徑 24)
        if (tcr$isWaterNearbyFast(context, centerX, centerZ, 24)) {
            cir.setReturnValue(Pair.of(false, 0));
            return;
        }

        // 通過檢測，設置變體
        tcr$fastCheckVariant(context, new BlockPos(centerX, centerH, centerZ), biome);

        int usableHeight = minH + (maxH - minH) / 3;
        if (this.towerType == 2) usableHeight -= 2;

        cir.setReturnValue(Pair.of(true, usableHeight));
    }

    /**
     * 替代原版的 checkVariant
     * 直接利用當前位置的 Biome Holder 進行標籤判斷，完全避免橫向搜索。
     */
    @Unique
    private void tcr$fastCheckVariant(Structure.GenerationContext context, BlockPos pos, Holder<Biome> biome) {
        WorldgenRandom random = context.random();
        random.setLargeFeatureSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        if (biome.is(BTTags.Biomes.LAND_TOWER_SNOWY_BIOMES)) {
            this.towerType = 1; // Snowy
        } else {
            this.towerType = 0; // Normal
        }
    }

    @Unique
    private boolean tcr$isWaterNearbyFast(Structure.GenerationContext context, int x, int z, int radius) {
        BiomeSource source = context.biomeSource();
        var sampler = context.randomState().sampler();
        int seaLevel = context.chunkGenerator().getSeaLevel();

        // 採樣 5 個點檢查是否有海洋/沙灘
        int[][] samples = {{0, 0}, {radius, 0}, {-radius, 0}, {0, radius}, {0, -radius}};
        for (int[] offset : samples) {
            Holder<Biome> b = source.getNoiseBiome(QuartPos.fromBlock(x + offset[0]), QuartPos.fromBlock(seaLevel), QuartPos.fromBlock(z + offset[1]), sampler);
            if (b.is(BTTags.Biomes.AVOID_OCEAN_BEACH)) return true;
        }
        return false;
    }
}