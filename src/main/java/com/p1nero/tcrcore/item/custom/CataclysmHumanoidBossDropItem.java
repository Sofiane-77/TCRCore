package com.p1nero.tcrcore.item.custom;

import com.p1nero.tcr_bosses.entity.cataclysm.BaseBossEntity;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.utils.WorldUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public class CataclysmHumanoidBossDropItem extends SimpleDescriptionItem {
    private final Supplier<EntityType<? extends BaseBossEntity>> supplier;
    private EntityType<? extends BaseBossEntity> cache;

    public CataclysmHumanoidBossDropItem(Properties properties, Supplier<EntityType<? extends BaseBossEntity>> supplier) {
        super(properties, true);
        this.supplier = supplier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @Nullable Level level, @NotNull List<Component> list, @NotNull TooltipFlag flag) {
        if (cache == null) {
            cache = supplier.get();
        }
        list.add(TCRCoreMod.getInfo("cataclysm_humanoid_drop_desc", WorldUtils.SAMSARA_NAME.withStyle(ChatFormatting.GOLD), cache.getDescription().copy().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
    }
}
