package com.p1nero.tcrcore.dialog.extension;

import com.brass_amber.ba_bt.init.BTItems;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.api.entity.EntityDialogueExtension;
import com.p1nero.dialog_lib.api.entity.IEntityDialogueExtension;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.tcr_bosses.entity.TCRBossEntities;
import com.p1nero.tcr_bosses.entity.custom.nether.evening_ghost.EveningGhostEntity;
import com.p1nero.tcrcore.TCRCoreMod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

@EntityDialogueExtension(modId = TCRCoreMod.MOD_ID)
public class EveningGhostDialogExtension implements IEntityDialogueExtension<EveningGhostEntity> {
    @Override
    public EntityType<EveningGhostEntity> getEntityType() {
        return TCRBossEntities.EVENING_GHOST.get();
    }

    @Override
    public boolean canInteractWith(Player player, EveningGhostEntity entity) {
        return !entity.shouldAttack(player);
    }

    @Override
    public DialogueScreen getDialogScreen(StreamDialogueScreenBuilder streamDialogueScreenBuilder, LocalPlayer localPlayer, EveningGhostEntity entity, CompoundTag compoundTag) {
        DialogueComponentBuilder builder = new DialogueComponentBuilder(entity, TCRCoreMod.MOD_ID);
        DialogNode root = new DialogNode(builder.ans(0), builder.opt(-1));
        DialogNode whoAreU = new DialogNode(builder.ans(1), builder.opt(0))
                .addChild(root)
                .addLeaf(builder.opt(2));
        DialogNode about = new DialogNode(builder.ans(2), builder.opt(1, BTItems.NETHER_MONOLITH_KEY.get().getDescription()))
                .addLeaf(builder.opt(3), 1)
                .addLeaf(builder.opt(2));

        root.addChild(whoAreU)
                .addChild(about)
                .addLeaf(builder.opt(2));

        return streamDialogueScreenBuilder.buildWith(root);
    }

    @Override
    public void handleNpcInteraction(EveningGhostEntity entity, ServerPlayer serverPlayer, int i) {
        if(i == 1) {
            entity.setInFighting(true);
            entity.setFightDelay(61);
            entity.getTags().add("started");
        }
        setConservingPlayer(null, entity);
    }
}
