package com.p1nero.tcrcore.mixin;

import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.item.TCRItems;
import com.p1nero.tcrcore.utils.EntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    @Shadow
    private static boolean inPortalDimension(Level p_49249_) {
        return false;
    }

    @Inject(method = "onPlace", at = @At("HEAD"), cancellable = true)
    private void tcr$onPlace(BlockState p_49279_, Level level, BlockPos pos, BlockState p_49282_, boolean p_49283_, CallbackInfo ci) {
        if (!p_49282_.is(p_49279_.getBlock())) {
            if (inPortalDimension(level)) {
                Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, pos, Direction.Axis.X);
                optional = net.minecraftforge.event.ForgeEventFactory.onTrySpawnPortal(level, pos, optional);
                if (optional.isPresent()) {
                    EntityUtils.nearPlayerDo(level, pos.getCenter(), 10, (player -> {
                        player.displayClientMessage(TCRCoreMod.getInfo("please_use_custom_flint_and_steel", TCRItems.CORE_FLINT.get().getDescription()), true);
                    }));
                    ci.cancel();
                }
            }
        }
    }

}
