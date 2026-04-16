package com.p1nero.tcrcore.mixin;

import com.windrinn.ash_of_sin_no_more_too_many_structures.config.NoMoreTooManyStructuresConfig;
import com.windrinn.ash_of_sin_no_more_too_many_structures.util.NoMoreTooManyStructuresPlacementValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoMoreTooManyStructuresPlacementValidator.class)
public class NoMoreTooManyStructuresPlacementValidatorMixin {

    @Inject(method = "canPlaceStructure", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tcr$canPlaceStructure(LevelAccessor level, BoundingBox box, String structureName, BlockPos center, CallbackInfoReturnable<Boolean> cir) {
        ResourceLocation id = ResourceLocation.tryParse(structureName);
        boolean isWhitelisted = id != null && NoMoreTooManyStructuresConfig.isStructureWhitelisted(id);
        if(isWhitelisted) {
            cir.setReturnValue(true);
        }
    }

}
