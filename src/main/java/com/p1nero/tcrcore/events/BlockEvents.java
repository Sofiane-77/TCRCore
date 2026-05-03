package com.p1nero.tcrcore.events;

import com.brass_amber.ba_bt.block.block.BTSpawnerBlock;
import com.brass_amber.ba_bt.init.BTItems;
import com.github.L_Ender.cataclysm.init.ModBlocks;
import com.p1nero.cataclysm_dimension.worldgen.CataclysmDimensions;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.save_data.TCRDimSaveData;
import com.p1nero.tcrcore.utils.ItemUtil;
import com.p1nero.tcrcore.utils.WorldUtil;
import net.mehvahdjukaar.supplementaries.common.block.blocks.BookPileBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.violetmoon.quark.content.mobs.entity.Forgotten;
import org.violetmoon.quark.content.mobs.module.ForgottenModule;

@Mod.EventBusSubscriber(modid = TCRCoreMod.MOD_ID)
public class BlockEvents {
    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event) {
        if(event.getPlayer() == null || event.getPlayer().isCreative()) {
            return;
        }
        if(event.getPlayer().level() instanceof ServerLevel serverLevel) {
            //主城不可破坏，除了书堆= =
            if(WorldUtil.inMainLand(event.getPlayer()) || WorldUtil.inReal(event.getPlayer())) {
                if(event.getState().getBlock() instanceof BookPileBlock) {
                    return;
                }
                event.setCanceled(true);
            }
            //不可破坏神像
            if(event.getState().is(ModBlocks.GODDESS_STATUE.get()) && WorldUtil.inMainLand(event.getPlayer())) {
                EntityType.LIGHTNING_BOLT.spawn(serverLevel, event.getPos(), MobSpawnType.MOB_SUMMONED);
                event.setCanceled(true);
            }
            if(CataclysmDimensions.LEVELS.contains(event.getPlayer().level().dimension())) {
                if(!TCRDimSaveData.get(serverLevel).isBossKilled()) {
                    event.getPlayer().displayClientMessage(TCRCoreMod.getInfo("dim_block_no_interact_no_drop"), true);
                    event.setCanceled(true);
                    serverLevel.destroyBlock(event.getPos(), false);
                }
            }
            //在呱呱村庄打爆紫水晶块概率出遗忘者
            if(WorldUtil.isInStructure(event.getPlayer(), WorldUtil.RIBBIT_VILLAGE) && serverLevel.isDay()){
                if(event.getState().is(Blocks.AMETHYST_BLOCK)) {
                    if(serverLevel.random.nextInt(serverLevel.isDay() ? 5 : 100) == 1) {
                        Forgotten forgotten = ForgottenModule.forgottenType.spawn(serverLevel, event.getPos(), MobSpawnType.SPAWNER);
                        if(forgotten != null) {
                            forgotten.setTarget(event.getPlayer());
                            forgotten.setGlowingTag(true);
                        }
                    }
                }
            }

            //挖刷怪笼直接得钥匙，防止坏掉
            if(event.getState().getBlock() instanceof BTSpawnerBlock) {
                if(WorldUtil.isInStructure(serverLevel, event.getPos().getCenter(), WorldUtil.LAND_GOLEM)) {
                    ItemUtil.addItemEntity(serverLevel, event.getPos(), BTItems.LAND_MONOLITH_KEY.get().getDefaultInstance());
                }
                if(WorldUtil.isInStructure(serverLevel, event.getPos().getCenter(), WorldUtil.OCEAN_GOLEM)) {
                    ItemUtil.addItemEntity(serverLevel, event.getPos(), BTItems.OCEAN_MONOLITH_KEY.get().getDefaultInstance());
                }
                if(WorldUtil.isInStructure(serverLevel, event.getPos().getCenter(), WorldUtil.CORE_GOLEM)) {
                    ItemUtil.addItemEntity(serverLevel, event.getPos(), BTItems.CORE_MONOLITH_KEY.get().getDefaultInstance());
                }
            }

        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if(event.getEntity() instanceof Player player && player.isCreative()) {
            return;
        }
        if(event.getEntity() != null) {
            //主城保护，除了书堆= =
            if(WorldUtil.inMainLand(event.getEntity()) || WorldUtil.inReal(event.getEntity())) {
                if(event.getState().getBlock() instanceof BookPileBlock) {
                    return;
                }
                event.setCanceled(true);
            }
            //幻境禁止摆放
            if(event.getEntity().level() instanceof ServerLevel serverLevel) {
                if(CataclysmDimensions.LEVELS.contains(serverLevel.dimension())) {
                    if(!TCRDimSaveData.get(serverLevel).isBossKilled()) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

}
