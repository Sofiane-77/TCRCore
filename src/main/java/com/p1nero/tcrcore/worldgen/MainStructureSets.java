package com.p1nero.tcrcore.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.Set;

public interface MainStructureSets {

    ResourceKey<StructureSet> LAND_TOWER = register("ba_bt:land_tower_set");
    ResourceKey<StructureSet> CHIMERA = register("dodosmobs:jungle_prison");
    ResourceKey<StructureSet> OCEAN_TOWER = register("ba_bt:ocean_tower_set");
    ResourceKey<StructureSet> RIBBITS = register("ribbits:ribbit_village");
    ResourceKey<StructureSet> SHIP = register("aquamirae:ship");
    ResourceKey<StructureSet> CORE_TOWER = register("ba_bt:core_tower_set");
    ResourceKey<StructureSet> NETHER = register("tcrcore:nether_main_structure_set");
    ResourceKey<StructureSet> AETHER = register("tcrcore:aether_main_structure_set");
    ResourceKey<StructureSet> END_GATEWAY = register("tlc:structure_lost_castle_set");

    Set<ResourceKey<StructureSet>> NEED_SEPARATE_OVERWORLD = Set.of(
            LAND_TOWER,
            CHIMERA,
            OCEAN_TOWER,
            RIBBITS,
            CORE_TOWER,
            END_GATEWAY
    );
    Set<ResourceKey<StructureSet>> NEED_SEPARATE_NETHER = Set.of(
            NETHER
    );
    Set<ResourceKey<StructureSet>> NEED_SEPARATE_AETHER = Set.of(
            AETHER
    );
    private static ResourceKey<StructureSet> register(String pName) {
        return ResourceKey.create(Registries.STRUCTURE_SET, ResourceLocation.parse(pName));
    }

}
