package com.p1nero.tcrcore.mixin.ba_bt;

import com.brass_amber.ba_bt.entity.block.BTAbstractObelisk;
import com.brass_amber.ba_bt.entity.block.BTOceanObelisk;
import com.brass_amber.ba_bt.util.BTStatics;
import com.brass_amber.ba_bt.util.BTUtil;
import com.brass_amber.ba_bt.util.GolemType;
import com.google.common.collect.ImmutableList;
import com.p1nero.tcrcore.TCRCoreMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(BTOceanObelisk.class)
public abstract class BTOceanObeliskMixin extends BTAbstractObelisk  {

    @Shadow(remap = false)
    @Mutable
    @Final
    private List<Block> avoidBlocks;

    public BTOceanObeliskMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "gatherAreaBlocks", at = @At("HEAD"), remap = false)
    private void tcr$gatherAreaBlocks(CallbackInfo ci) {
        if(!this.avoidBlocks.contains(Blocks.CHEST)) {
            this.avoidBlocks = ImmutableList.<Block>builder().addAll(BTStatics.towerBlocks.get(GolemType.OCEAN.ordinal())).add(Blocks.CHEST).build();
        }
    }

    @Inject(method = "removeAreaBlocks", at = @At("HEAD"), remap = false, cancellable = true)
    private void tcr$removeAreaBlocks(CallbackInfo ci) {
        int removeSize = this.toRemove.size();
        if (removeSize > 0) {
            for(int i = Math.min(removeSize, 2048); i > 0 ; --i) {
                this.level().setBlock(this.toRemove.remove(0), Blocks.WATER.defaultBlockState(), 2);
            }
            BTUtil.doNoOutputCommand(this, "/kill @e[distance=0..100,type=item]");
        } else {
            this.generationState = BTAbstractObelisk.GenerationState.ADD_AREA_FEATURES;
        }
        if(this.tickCount % 10 == 0) {
            level().players().forEach(player -> {
                player.displayClientMessage(TCRCoreMod.getInfo("ocean_tower_breaking"), true);
            });
        }
        ci.cancel();
    }
}
