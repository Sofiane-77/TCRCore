package com.p1nero.tcrcore.item.custom;

import com.p1nero.tcrcore.utils.EntityUtils;
import com.p1nero.tcrcore.utils.WorldUtils;
import com.yesman.epicskills.registry.entry.EpicSkillsSounds;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.network.SyncManaPacket;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 蓝瓶，使用后可回1/3蓝
 */
public class BlueBottleItem extends SimpleDescriptionItem{

    public static final String DATA_KEY = "count";

    public static final float RATE = 0.33F;

    public BlueBottleItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        //在主城则直接回满
        if(WorldUtils.inMainLand(player)) {
            itemStack.getOrCreateTag().putInt(DATA_KEY, 3);
            if(player instanceof ServerPlayer serverPlayer) {
                EntityUtils.playLocalSound(serverPlayer, EpicSkillsSounds.GAIN_ABILITY_POINTS.get());
            }
            return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
        }
        int count = itemStack.getOrCreateTag().getInt(DATA_KEY);
        if(count > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack itemStack) {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, @NotNull Level level, @NotNull LivingEntity entity) {
        int count = itemStack.getOrCreateTag().getInt(DATA_KEY);
        if(count > 0 && entity instanceof ServerPlayer player) {
            double maxMana = player.getAttribute(AttributeRegistry.MAX_MANA.get()).getValue();
            MagicData magicData = MagicData.getPlayerMagicData(player);
            magicData.addMana((float) (maxMana * RATE));
            PacketDistributor.sendToPlayer(player, new SyncManaPacket(magicData));
            itemStack.getOrCreateTag().putInt(DATA_KEY, count - 1);
        }
        return super.finishUsingItem(itemStack, level, entity);
    }

    public int getUseDuration(@NotNull ItemStack item) {
        return 40;
    }

    public @NotNull SoundEvent getDrinkingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    public @NotNull SoundEvent getEatingSound() {
        return SoundEvents.HONEY_DRINK;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack itemStack) {
        return itemStack.getOrCreateTag().getInt(DATA_KEY) > 0;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack itemStack) {
        return super.getName(itemStack).copy().append("[ ×" + itemStack.getOrCreateTag().getInt(DATA_KEY) + " ]");
    }
}
