package com.p1nero.tcrcore.item.custom;

import com.p1nero.battle_field1.worldgen.PBF1Dimensions;
import com.p1nero.tcrcore.utils.WorldUtils;
import com.yesman.epicskills.registry.entry.EpicSkillsSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RetracementStoneItem extends SimpleDescriptionItem {

    public RetracementStoneItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if(level.dimension() != PBF1Dimensions.SANCTUM_OF_THE_BATTLE_LEVEL_KEY) {
            return InteractionResultHolder.fail(itemstack);
        }
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
        if(level instanceof ServerLevel serverLevel) {
            List<Entity> newList = new ArrayList<>();
            serverLevel.getAllEntities().forEach(newList::add);
            for(Entity entity : newList) {
                if(!(entity instanceof Player)) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, entity.getX(), entity.getY(), entity.getZ(), 10, 0.0D, 0.1D, 0.0D, 0.01);
                    serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1);
                    entity.discard();
                }
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        player.playSound(EpicSkillsSounds.GAIN_ABILITY_POINTS.get(), 1.0F, 1.0F);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        list.add(Component.translatable(this.getDescriptionId() + ".usage", WorldUtils.SAMSARA_NAME).withStyle(ChatFormatting.GRAY));
    }
}
