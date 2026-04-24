package com.p1nero.tcrcore.dialog.extension;

import com.aetherteam.aether.client.AetherSoundEvents;
import com.aetherteam.aether.item.AetherItems;
import com.brass_amber.ba_bt.init.BTItems;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.api.entity.EntityDialogueExtension;
import com.p1nero.dialog_lib.api.entity.IEntityDialogueExtension;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.tcr_bosses.entity.TCRBossEntities;
import com.p1nero.tcr_bosses.entity.custom.aether.valkyrie.ValkyrieQueenEntity;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.utils.ItemUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@EntityDialogueExtension(modId = TCRCoreMod.MOD_ID)
public class ValkyrieQueenDialogExtension implements IEntityDialogueExtension<ValkyrieQueenEntity> {

    @Override
    public EntityType<ValkyrieQueenEntity> getEntityType() {
        return TCRBossEntities.VALKYRIE.get();
    }

    @Override
    public boolean canInteractWith(Player player, ValkyrieQueenEntity valkyrieQueenEntity) {
        return !valkyrieQueenEntity.shouldAttack(player);
    }

    @Override
    public void onPlayerInteract(Player player, ValkyrieQueenEntity currentTalking, InteractionHand hand) {
        IEntityDialogueExtension.super.onPlayerInteract(player, currentTalking, hand);
        player.playSound(AetherSoundEvents.ENTITY_VALKYRIE_QUEEN_INTERACT.get());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogScreen(StreamDialogueScreenBuilder streamDialogueScreenBuilder, LocalPlayer localPlayer, ValkyrieQueenEntity valkyrieQueenEntity, CompoundTag compoundTag) {
        int medals = localPlayer.getInventory().countItem(AetherItems.VICTORY_MEDAL.get());
        DialogueComponentBuilder builder = new DialogueComponentBuilder(valkyrieQueenEntity, TCRCoreMod.MOD_ID);
        DialogNode root = new DialogNode(builder.ans(0), builder.opt(-1));

        DialogNode whoAreU = new DialogNode(builder.ans(1, valkyrieQueenEntity.getDisplayName(), BTItems.SKY_MONOLITH_KEY.get().getDescription()), builder.opt(0))
                .addChild(root)
                .addLeaf(builder.opt(-2));

        DialogNode fight;

        if(medals < 10) {
            String cnt = medals + "/10";
            fight = new DialogNode(builder.ans(2, BTItems.SKY_MONOLITH_KEY.get().getDescription(), AetherItems.VICTORY_MEDAL.get().getDescription()), builder.opt(1, BTItems.SKY_MONOLITH_KEY.get().getDescription()))
                    .addLeaf(builder.opt(2, cnt));
        } else {
            fight = new DialogNode(builder.ans(2, BTItems.SKY_MONOLITH_KEY.get().getDescription(), AetherItems.VICTORY_MEDAL.get().getDescription()), builder.opt(1, BTItems.SKY_MONOLITH_KEY.get().getDescription()))
                    .addLeaf(builder.opt(3), 1)
                    .addLeaf(builder.opt(-2));
        }
        root.addChild(whoAreU).addChild(fight);
        return streamDialogueScreenBuilder.buildWith(root);
    }

    @Override
    public void handleNpcInteraction(ValkyrieQueenEntity valkyrieQueenEntity, ServerPlayer serverPlayer, int i) {
        if(i == 1) {
            ItemUtil.searchAndConsumeItem(serverPlayer,AetherItems.VICTORY_MEDAL.get(), 10);
            valkyrieQueenEntity.setInFighting(true);
            valkyrieQueenEntity.setFightDelay(61);
            valkyrieQueenEntity.getTags().add("started");
        }
        removeConservingPlayer(valkyrieQueenEntity);
    }
}
