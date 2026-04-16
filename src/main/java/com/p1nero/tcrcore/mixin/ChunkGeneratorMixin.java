package com.p1nero.tcrcore.mixin;

import com.kamikaguya.ash_of_sin_no_more_too_many_structures.config.NoMoreTooManyStructuresConfig;
import com.kamikaguya.ash_of_sin_no_more_too_many_structures.mixin.StructureManagerAccessor;
import com.kamikaguya.ash_of_sin_no_more_too_many_structures.util.NoMoreTooManyStructuresPlacementValidator;
import com.kamikaguya.ash_of_sin_no_more_too_many_structures.util.NoMoreTooManyStructuresSavedData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ChunkGenerator.class})
public class ChunkGeneratorMixin {
    private static final Logger LOGGER = LogManager.getLogger();

    @WrapOperation(
        method = {"tryGenerateStructure"},
        at = {@At(
    value = "INVOKE",
    target = "Lnet/minecraft/world/level/StructureManager;setStartForStructure(Lnet/minecraft/core/SectionPos;Lnet/minecraft/world/level/levelgen/structure/Structure;Lnet/minecraft/world/level/levelgen/structure/StructureStart;Lnet/minecraft/world/level/chunk/StructureAccess;)V"
)}
    )
    private void filterStructure(StructureManager structureManager, SectionPos sectionPos, Structure structure, StructureStart pendingStructure, StructureAccess structureAccess, Operation<Void> original) {
        BoundingBox box = pendingStructure.getBoundingBox();
        if (box == null) {
            original.call(structureManager, sectionPos, structure, pendingStructure, structureAccess);
        } else {
            ResourceLocation id = this.tcr$getStructureKey(structure);
            if (id == null) {
                if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                    LOGGER.warn("Structure key is null, skipping detection for {}", structure);
                }

                original.call(structureManager, sectionPos, structure, pendingStructure, structureAccess);
            } else {
                if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                    LOGGER.debug("Structure id: {}", id);
                }

                if (NoMoreTooManyStructuresConfig.isStructureBlacklisted(id)) {
                    if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                        LOGGER.debug("Structure {} is blacklisted, skipping generation.", id);
                    }

                }  else {
                    LevelAccessor levelAccessor = ((StructureManagerAccessor)structureManager).getLevel();
                    ServerLevel serverLevel = null;
                    if (levelAccessor instanceof ServerLevel) {
                        serverLevel = (ServerLevel)levelAccessor;
                    } else if (levelAccessor instanceof WorldGenLevel) {
                        serverLevel = ((WorldGenLevel)levelAccessor).getLevel();
                    }

                    if (serverLevel == null) {
                        original.call(structureManager, sectionPos, structure, pendingStructure, structureAccess);
                    } else {
                        ChunkPos chunkPos = pendingStructure.getChunkPos();
                        int yCenter = (box.minY() + box.maxY()) / 2;
                        BlockPos center = new BlockPos(chunkPos.getMinBlockX() + 8, yCenter, chunkPos.getMinBlockZ() + 8);
                        if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                            LOGGER.debug("Center: {}, BoundingBox: {}", center, box);
                        }

                        boolean canPlace = NoMoreTooManyStructuresPlacementValidator.canPlaceStructure(serverLevel, box, id.toString(), center);
                        if (NoMoreTooManyStructuresConfig.isStructureWhitelisted(id)) {
                            if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                                LOGGER.debug("Structure {} is skipped", id);
                            }
                            canPlace = true;
                        }
                        LOGGER.debug("canPlace = {}", canPlace);
                        if (!canPlace) {
                            if (NoMoreTooManyStructuresConfig.DEBUG.get()) {
                                LOGGER.debug("Skipping generation of {}", id);
                            }

                        } else {
                            NoMoreTooManyStructuresSavedData data = serverLevel.getDataStorage().computeIfAbsent(NoMoreTooManyStructuresSavedData::load, NoMoreTooManyStructuresSavedData::create, "no_more_too_many_structures_data");
                            AABB aabb = new AABB(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
                            data.addPlacedObject(aabb, id.toString());
                            original.call(structureManager, sectionPos, structure, pendingStructure, structureAccess);
                            if (pendingStructure.isValid()) {
                                data.addPlacedObject(aabb, id.toString());
                            } else {
                                LOGGER.warn("Structure {} at {} was marked as generated but is invalid after generation", id, center);
                            }
                        }
                    }
                }
            }
        }
    }

    @Unique
    private ResourceLocation tcr$getStructureKey(Structure structure) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            LOGGER.warn("Server is null, cannot get structure key");
            return null;
        } else {
            Registry<Structure> registry = server.registryAccess().registry(Registries.STRUCTURE).orElse(null);
            if (registry == null) {
                LOGGER.warn("Structure registry not found");
                return null;
            } else {
                ResourceLocation key = registry.getKey(structure);
                if (key == null) {
                    LOGGER.warn("Structure key is null for {}", structure);
                }

                return key;
            }
        }
    }
}
