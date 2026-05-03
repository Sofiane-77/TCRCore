package com.p1nero.tcrcore.utils;

import com.mojang.datafixers.util.Pair;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.worldgen.TCRDimensions;
import com.yungnickyoung.minecraft.yungsapi.criteria.SafeStructureLocationPredicate;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.utils.math.Vec2i;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldUtils {
    //维度翻译名
    public static final MutableComponent REAL_NAME = Component.translatable("travelerstitles.tcrcore.real");
    public static final MutableComponent SANCTUM_NAME = Component.translatable("travelerstitles.tcrcore.sanctum");
    public static final MutableComponent OVERWORLD_NAME = Component.translatable("travelerstitles.minecraft.overworld");
    public static final MutableComponent AETHER_NAME = Component.translatable("travelerstitles.aether.the_aether");
    public static final MutableComponent NETHER_NAME = Component.translatable("travelerstitles.minecraft.the_nether");
    public static final MutableComponent END_NAME = Component.translatable("travelerstitles.minecraft.the_end");
    public static final MutableComponent SAMSARA_NAME = Component.translatable("travelerstitles.pbf1.sanctum_of_the_battle1");

    //祭坛定位
    public static final Vec3i DESERT_EYE_ALTAR_POS = new Vec3i(-140, 83, -176);
    public static final Vec3i ABYSS_EYE_ALTAR_POS = new Vec3i(-128, 83, -206);
    public static final Vec3i CURSED_EYE_ALTAR_POS = new Vec3i(-128, 83, -176);
    public static final Vec3i FLAME_EYE_ALTAR_POS = new Vec3i(-116, 83, -176);
    public static final Vec3i MECH_EYE_ALTAR_POS = new Vec3i(-104, 83, -176);
    public static final Vec3i STORM_EYE_ALTAR_POS = new Vec3i(-140, 83, -206);
    public static final Vec3i VOID_EYE_ALTAR_POS = new Vec3i(-116, 83, -206);
    public static final Vec3i MONST_EYE_ALTAR_POS = new Vec3i(-104, 83, -206);

    //一些关键定位
    public static final Vec3i BED_POS = new Vec3i(0, 64, 0);
    public static final Vec3i ORNN_POS = new Vec3i(-236, 80, -99);
    public static final BlockPos ORNN_BLOCK_POS = new BlockPos(-236, 80, -99);
    public static final BlockPos SMITH_BLOCK_POS = new BlockPos(-236, 77, -100);
    public static final Vec3i AINE_POS = new Vec3i(-115, 80, -127);
    public static final BlockPos AINE_BLOCK_POS = new BlockPos(-115, 80, -127);
    public static final BlockPos ARCANE_ANVIL_BLOCK_POS = new BlockPos(-115, 77, -127);
    public static final Vec3i FERRY_GIRL_POS = new Vec3i(80, 74, -133);
    public static final BlockPos FERRY_GIRL_BLOCK_POS = new BlockPos(80, 74, -133);
    public static final Vec3i FERRY_GIRL_PORTAL_POS = new Vec3i(80, 73, -138);
    public static final Vec3i START_POS = new Vec3i(-3, 75, -190);
    public static final Vec3i GODNESS_STATUE_POS = new Vec3i(-176, 86, -118);
    public static final Vec3 GOLEM_CENTER_POS_VEC3 = new Vec3(78, 75, -190);
    public static final Vec3i GOLEM_CENTER_POS_VEC3I = new Vec3i(78, 75, -190);
    public static final Vec3 CENTER_POS = new Vec3(-19, 75, -79);
    public static final Vec3 CHRONOS_SOL_POS = new Vec3(-211, 85, -191);
    public static final BlockPos CHRONOS_SOL_BLOCK_POS = new BlockPos(-211, 85, -191);

    public static final String LAND_GOLEM = "ba_bt:land_tower";
    public static final String BONE_CHIMERA_STRUCTURE = "dodosmobs:jungle_prison";
    public static final String OCEAN_GOLEM = "ba_bt:ocean_tower";
    public static final String RIBBIT_VILLAGE = "ribbits:ribbit_village";
    public static final String AQUAMIRAE_SHIP_STRUCTURE = "aquamirae:ship";
    public static final String CORE_GOLEM = "ba_bt:core_tower";
    public static final String NETHER_KEY_1 = "irons_spellbooks:citadel";
    public static final String NETHER_KEY_2 = "betterfortresses:fortress";
    public static final String NETHER_GOLEM = "tcrcore:gate_of_disaster";
    public static final String AETHER_KEY_1 = "aether:silver_dungeon";
    public static final String AETHER_KEY_2 = "aether:gold_dungeon";
    public static final String SKY_GOLEM = "lost_aether_content:platinum_dungeon";
    public static final String STRONG_HOLD = "tlc:lost_castle";

    public static List<MapColor> surfaceMaterials = Arrays.asList(MapColor.WATER, MapColor.ICE);

    private static final Pattern LOCATE_PATTERN = Pattern.compile(".*?\\[\\s*(-?\\d+)\\s*,\\s*~\\s*,\\s*(-?\\d+)\\s*\\].*");

    public static MutableComponent getStructureName(String id) {
        return Component.translatable(Util.makeDescriptionId("structure", ResourceLocation.parse(id)));
    }

    public static boolean inMainLand(Entity entity) {
        return entity.level().dimension() == TCRDimensions.SANCTUM_LEVEL_KEY;
    }

    public static boolean inReal(Entity entity) {
        return entity.level().dimension() == TCRDimensions.REAL_LEVEL_KEY;
    }

    public static boolean inMainLandRange(Entity entity) {
        return entity.position().subtract(CENTER_POS).horizontalDistance() < 250;
    }

    public static boolean isInStructure(LivingEntity entity, String structure) {
        if(entity.level().isClientSide) {
            return false;
        }
        return new SafeStructureLocationPredicate(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(structure))).matches(((ServerLevel) entity.level()), entity.getX(), entity.getY(), entity.getZ());
    }

    public static boolean isInStructure(ServerLevel serverLevel, Vec3 pos, String structure) {
        return new SafeStructureLocationPredicate(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.parse(structure))).matches(serverLevel, pos.x(), pos.y(), pos.z());
    }


    /**
     * 获取结构位置
     */
    @Nullable
    public static BlockPos getNearbyStructurePos(ServerPlayer serverPlayer, String structureId, int y) {
        return getNearbyStructurePos(serverPlayer.serverLevel(), serverPlayer.getOnPos(), structureId, y);
    }

    @Nullable
    public static BlockPos getNearbyStructurePos(ServerPlayer serverPlayer, String structureId, int y, boolean ignoreFounded) {
        return getNearbyStructurePos(serverPlayer.serverLevel(), serverPlayer.getOnPos(), structureId, y, ignoreFounded);
    }

    @Nullable
    public static BlockPos getNearbyStructurePos(ServerLevel serverLevel, BlockPos playerPos, String structureId, int y) {
        return getNearbyStructurePos(serverLevel, playerPos, structureId, y, true);
    }

    @Nullable
    public static BlockPos getNearbyStructurePos(ServerLevel serverLevel, BlockPos playerPos, String structureId, int y, boolean ignoreFounded) {
        ResourceLocation structureResourceLocation = ResourceLocation.tryParse(structureId);
        if (structureResourceLocation == null) {
            return null;
        }

        ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, structureResourceLocation);
        Registry<Structure> structureRegistry = serverLevel.registryAccess().registryOrThrow(Registries.STRUCTURE);

        var structureHolderOpt = structureRegistry.getHolder(structureKey);
        if (structureHolderOpt.isEmpty()) {
            return null;
        }

        HolderSet<Structure> structureSet = HolderSet.direct(structureHolderOpt.get());

        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();

        Pair<BlockPos, Holder<Structure>> result = chunkGenerator.findNearestMapStructure(
                serverLevel,
                structureSet,
                playerPos,
                100,
                ignoreFounded//跳过已找？
        );

        if (result != null) {
            BlockPos structurePos = result.getFirst();
            return new BlockPos(structurePos.getX(), y, structurePos.getZ());
        }

        return null;
    }

    /**
     * 获取结构位置
     */
    @Nullable
    public static Vec2i getNearbyStructurePos(ServerLevel serverLevel, Vec3 position, String structureId) {
        String output = getCommandOutput(serverLevel, position, "/locate structure " + structureId);
        Matcher matcher = LOCATE_PATTERN.matcher(output);
        if(matcher.find()) {
            try {
                String xStr = matcher.group(1).trim();
                String zStr = matcher.group(2).trim();
                return new Vec2i(Integer.parseInt(xStr), Integer.parseInt(zStr));
            } catch (NumberFormatException ignored) {
                TCRCoreMod.LOGGER.error(output);
            }
        }
        return null;
    }

    public static BlockPos getNearbyStructurePosByCommand(ServerPlayer serverPlayer, String structureId, int y) {
        return getNearbyStructurePosByCommand(serverPlayer.serverLevel(), serverPlayer.getOnPos(), structureId, y);
    }

    public static BlockPos getNearbyStructurePosByCommand(ServerLevel serverLevel, BlockPos playerPos, String structureId, int y) {
        Vec2i posXZ = getNearbyStructurePos(serverLevel, playerPos.getCenter(), structureId);
        if(posXZ != null) {
            return new BlockPos(posXZ.x, y, posXZ.y);
        }
        return null;
    }

//    /**
//     * 获取结构位置
//     */
//    @Nullable
//    public static Vec2i getNearbyStructurePos(ServerPlayer serverPlayer, String structureId) {
//        String output = getCommandOutput(serverPlayer.serverLevel(), serverPlayer.position(), "/locate structure " + structureId);
//        Matcher matcher = LOCATE_PATTERN.matcher(output);
//        if(matcher.find()) {
//            try {
//                String xStr = matcher.group(1).trim();
//                String zStr = matcher.group(2).trim();
//                return new Vec2i(Integer.parseInt(xStr), Integer.parseInt(zStr));
//            } catch (NumberFormatException ignored) {
//            }
//        }
//        return null;
//    }

    public static String getCommandOutput(ServerLevel serverLevel, @Nullable Vec3 vec, String command) {
        BaseCommandBlock baseCommandBlock = new BaseCommandBlock() {
            @Override
            public @NotNull ServerLevel getLevel() {
                return serverLevel;
            }

            @Override
            public void onUpdated() {
            }

            @Override
            public @NotNull Vec3 getPosition() {
                return Objects.requireNonNullElseGet(vec, () -> new Vec3(0, 0, 0));
            }

            @Override
            public @NotNull CommandSourceStack createCommandSourceStack() {
                return new CommandSourceStack(this, getPosition(), Vec2.ZERO, serverLevel, 2, "dev", Component.literal("dev"), serverLevel.getServer(), null);
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public boolean performCommand(Level pLevel) {
                if (!pLevel.isClientSide) {
                    this.setSuccessCount(0);
                    MinecraftServer server = this.getLevel().getServer();
                    try {
                        this.setLastOutput(null);
                        CommandSourceStack commandSourceStack = this.createCommandSourceStack().withCallback((context, success, i) -> {
                            if (success) {
                                this.setSuccessCount(this.getSuccessCount() + 1);
                            }
                        });
                        server.getCommands().performPrefixedCommand(commandSourceStack, this.getCommand());
                    } catch (Throwable ignored) {
                    }
                }
                return true;
            }
        };

        baseCommandBlock.setCommand(command);
        baseCommandBlock.setTrackOutput(true);
        baseCommandBlock.performCommand(serverLevel);

        return baseCommandBlock.getLastOutput().getString();
    }


    public static BlockPos getSurfaceBlockPos(ServerLevel serverLevel, BlockPos pos) {
        return getSurfaceBlockPos(serverLevel, pos.getX(), pos.getZ());
    }

    public static BlockPos getSurfaceBlockPos(ServerLevel serverLevel, int x, int z) {
        int height = serverLevel.getHeight();
        int minY = serverLevel.getMinBuildHeight();
        BlockPos pos = new BlockPos(x, height, z);
        for (int y = height; y > minY; y--) {
            BlockState blockState = serverLevel.getBlockState(pos);
            MapColor mapColor = blockState.getMapColor(serverLevel, pos);
            if (blockState.getLightBlock(serverLevel, pos) >= 15 || surfaceMaterials.contains(mapColor)) {
                return pos.above().immutable();
            }
            pos = pos.below();
        }

        return new BlockPos(x, height - 1, z);
    }

}
