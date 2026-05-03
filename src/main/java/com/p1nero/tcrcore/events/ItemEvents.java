package com.p1nero.tcrcore.events;

import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.PlayerDataManager;
import com.p1nero.tcrcore.utils.ItemUtils;
import net.genzyuro.uniqueaccessories.registry.UAItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.commons.lang3.SystemUtils;


@Mod.EventBusSubscriber(modid = TCRCoreMod.MOD_ID)
public class ItemEvents {

    @SubscribeEvent
    public static void onItemDesc(ItemTooltipEvent event) {

        //F3+H按不了，气笑了
        if(!FMLEnvironment.production && SystemUtils.IS_OS_MAC) {
            event.getToolTip().add(1, Component.literal(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()).toString()));
        }
        if(ItemUtils.additionalInfoItems.contains(event.getItemStack().getItem())) {
            event.getToolTip().add(1, Component.translatable(event.getItemStack().getItem().getDescriptionId() + ".tcr_info"));
        }

        if((!PlayerDataManager.finalBossKilled.get(event.getEntity()) || event.getItemStack().is(UAItems.STARVED_WOLF_SKULL.get())) && PlayerEventListeners.illegalItems.contains(event.getItemStack().getItem())) {
            event.getToolTip().add(1, TCRCoreMod.getInfo("illegal_item_tip2"));
        }

    }

}
