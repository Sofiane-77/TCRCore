package com.p1nero.tcrcore.mixin.ba_bt;

import com.brass_amber.ba_bt.block.blockentity.BTChestBlockEntity;
import com.brass_amber.ba_bt.entity.block.BTAbstractObelisk;
import com.brass_amber.ba_bt.util.GolemType;
import com.obscuria.aquamirae.Aquamirae;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BTAbstractObelisk.class)
public abstract class BTObeliskMixin extends Entity {

    @Shadow(remap = false)
    protected BTChestBlockEntity golemChest;

    @Shadow(remap = false)
    protected Block chestBlock;

    @Shadow(remap = false)
    protected Block golemChestBlock;

    @Shadow(remap = false)
    protected GolemType golemType;

    @Shadow(remap = false)
    protected int checkLayer;

    public BTObeliskMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    /**
     * 替换为战利品箱子
     */
    @Inject(method = "checkPos", at = @At("TAIL"), remap = false)
    private void tcr$checkPos(BlockPos toCheck, Level level, CallbackInfo ci) {
        Block block = level.getBlockState(toCheck).getBlock();
        if (block == this.chestBlock) {
            ResourceLocation layerLoot = switch (this.golemType) {
                case LAND -> checkLayer < 4 ? BuiltInLootTables.SIMPLE_DUNGEON : BuiltInLootTables.WOODLAND_MANSION;
                case OCEAN -> checkLayer < 4 ? BuiltInLootTables.SHIPWRECK_TREASURE : BuiltInLootTables.BURIED_TREASURE;
                case CORE -> checkLayer < 4 ? BuiltInLootTables.NETHER_BRIDGE : BuiltInLootTables.BASTION_TREASURE;
                default -> BuiltInLootTables.END_CITY_TREASURE;
            };
            BlockState chest = Blocks.CHEST.defaultBlockState();
            level.setBlock(toCheck, chest, 3);
            if (level.getBlockEntity(toCheck) instanceof ChestBlockEntity chestBlockEntity && level instanceof ServerLevel serverLevel) {
                chestBlockEntity.setLootTable(layerLoot, serverLevel.getSeed());
            }
        } else if (block == this.golemChestBlock) {
            ResourceLocation golemLoot = switch (this.golemType) {
                case LAND -> ResourceLocation.parse("tlc:chests/throne");
                case OCEAN -> ResourceLocation.fromNamespaceAndPath(Aquamirae.MODID, "chests/frozen_chest");
                case CORE -> BuiltInLootTables.BASTION_TREASURE;
                default -> BuiltInLootTables.END_CITY_TREASURE;
            };
            BlockState chest = Blocks.CHEST.defaultBlockState();
            level.setBlock(toCheck, chest, 3);
            if (level.getBlockEntity(toCheck) instanceof ChestBlockEntity chestBlockEntity && level instanceof ServerLevel serverLevel) {
                chestBlockEntity.setLootTable(golemLoot, serverLevel.getSeed());
            }
        }
        this.golemChest = null;
    }

    /**
     * 无需在意
     */
    @Inject(method = "checkSpawners", at = @At("HEAD"), cancellable = true, remap = false)
    private void tcr$checkSpawners(Level level, CallbackInfo ci) {
        ci.cancel();
    }

}
