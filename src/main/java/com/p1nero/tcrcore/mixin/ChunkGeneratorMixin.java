package com.p1nero.tcrcore.mixin;

import com.aetherteam.aether.data.resources.registries.AetherDimensions;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.p1nero.tcrcore.worldgen.MainStructureSets;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

/**
 * 让结构和主线结构保持距离
 */
@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
//
//    @Unique
//    private List<StructureSet.StructureSelectionEntry> tcr$cacheOverworld = null, tcr$cacheNether = null, tcr$cacheAether = null;
//
//    @WrapMethod(method = "tryGenerateStructure")
//    private boolean tcr$tryGenerateStructure(StructureSet.StructureSelectionEntry pStructureSelectionEntry, StructureManager pStructureManager, RegistryAccess pRegistryAccess, RandomState pRandom, StructureTemplateManager pStructureTemplateManager, long pSeed, ChunkAccess pChunk, ChunkPos pChunkPos, SectionPos pSectionPos, Operation<Boolean> original) {
//        ServerLevel serverLevel = ((ServerLevel) ((StructureManagerAccessor) pStructureManager).getLevel());
//        boolean canGenerate = true;
//
//        if (serverLevel != null) {
//            Registry<StructureSet> structureSetRegistry = pRegistryAccess.registryOrThrow(Registries.STRUCTURE_SET);
//
//            if (serverLevel.dimension() == ServerLevel.OVERWORLD) {
//                if (tcr$cacheOverworld == null) {
//                    tcr$cacheOverworld = new ArrayList<>();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_OVERWORLD) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        tcr$cacheOverworld.addAll(holder.get().structures());
//                    }
//                }
//
//                if (tcr$cacheOverworld.contains(pStructureSelectionEntry)) {
//                    ChunkGeneratorStructureState state = serverLevel.getChunkSource().getGeneratorState();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_OVERWORLD) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        if (state.hasStructureChunkInRange(holder, pChunkPos.x, pChunkPos.z, 6)) {
//                            canGenerate = false;
//                            break;
//                        }
//                    }
//                }
//            } else if (serverLevel.dimension() == ServerLevel.NETHER) {
//                if (tcr$cacheNether == null) {
//                    tcr$cacheNether = new ArrayList<>();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_NETHER) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        tcr$cacheNether.addAll(holder.get().structures());
//                    }
//                }
//
//                if (tcr$cacheNether.contains(pStructureSelectionEntry)) {
//                    ChunkGeneratorStructureState state = serverLevel.getChunkSource().getGeneratorState();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_NETHER) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        if (state.hasStructureChunkInRange(holder, pChunkPos.x, pChunkPos.z, 6)) {
//                            canGenerate = false;
//                            break;
//                        }
//                    }
//                }
//            } else if (serverLevel.dimension() == AetherDimensions.AETHER_LEVEL) {
//                if (tcr$cacheAether == null) {
//                    tcr$cacheAether = new ArrayList<>();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_AETHER) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        tcr$cacheAether.addAll(holder.get().structures());
//                    }
//                }
//
//                if (tcr$cacheAether.contains(pStructureSelectionEntry)) {
//                    ChunkGeneratorStructureState state = serverLevel.getChunkSource().getGeneratorState();
//                    for (ResourceKey<StructureSet> key : MainStructureSets.NEED_SEPARATE_AETHER) {
//                        Holder<StructureSet> holder = structureSetRegistry.getHolderOrThrow(key);
//                        if (state.hasStructureChunkInRange(holder, pChunkPos.x, pChunkPos.z, 6)) {
//                            canGenerate = false;
//                            break;
//                        }
//                    }
//                }
//            }
//        }
//
//        return canGenerate ? original.call(pStructureSelectionEntry, pStructureManager, pRegistryAccess,
//                pRandom, pStructureTemplateManager, pSeed, pChunk, pChunkPos, pSectionPos) : false;
//    }
}