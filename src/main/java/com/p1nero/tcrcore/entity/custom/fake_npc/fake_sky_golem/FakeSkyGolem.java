package com.p1nero.tcrcore.entity.custom.fake_npc.fake_sky_golem;

import com.brass_amber.ba_bt.init.BTEntityType;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.DialogueScreenBuilder;
import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.TCRQuests;
import com.p1nero.tcrcore.entity.TCREntities;
import com.p1nero.tcrcore.entity.custom.fake_npc.FakeNPCEntity;
import com.p1nero.tcrcore.item.TCRItems;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.SetThirdPersonPacket;
import com.p1nero.tcrcore.utils.EntityUtils;
import com.p1nero.tcrcore.utils.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.shelmarow.combat_evolution.execution.ExecutionHandler;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FakeSkyGolem extends FakeNPCEntity {

    public FakeSkyGolem(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public FakeSkyGolem(ServerPlayer serverPlayer) {
        super(TCREntities.FAKE_SKY_GOLEM.get(), serverPlayer.level());
        this.setOwner(serverPlayer);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        return super.hurt(source, 999999);
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if(player.getUUID().equals(this.getOwnerUUID()) || (!FMLEnvironment.production && player.isCreative())) {
            if( player instanceof ServerPlayer serverPlayer) {
                this.sendDialogTo(serverPlayer);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        return InteractionResult.FAIL;
    }

    /**
     * 太抽象了，这个对话lib还是适合自用= =
     * 做两个选项对应同一个对话的这种对话还是太麻烦了，用这个lib
     * 但是目前懒得去升级一下
     */
    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogueScreen(CompoundTag compoundTag) {
        DialogueScreenBuilder builder = new DialogueScreenBuilder(this, TCRCoreMod.MOD_ID);
        DialogueComponentBuilder dBuilder = builder.getComponentBuildr();
        DialogNode root = builder.newNode(0);

        DialogNode c1 = builder.newNode(1, 0);
        DialogNode c2 = builder.newNode(1, 1);

        DialogNode c3 = builder.newNode(2, 2);
        DialogNode c4 = builder.newNode(2, 3);

        DialogNode c5 = new DialogNode(dBuilder.ans(3, TCRItems.DIVINE_FRAGMENT.get().getDescription()), dBuilder.opt(4));
        DialogNode c6 = new DialogNode(dBuilder.ans(3, TCRItems.DIVINE_FRAGMENT.get().getDescription()), dBuilder.opt(5));

        DialogNode c7 = new DialogNode(dBuilder.ans(5), dBuilder.opt(6));
        DialogNode c8 = new DialogNode(dBuilder.ans(5), dBuilder.opt(7));

        DialogNode talkBefore = new DialogNode(dBuilder.ans(6, BTEntityType.SKY_GOLEM.get().getDescription().copy().append("?")), dBuilder.opt(8));

        DialogNode soga = new DialogNode(dBuilder.ans(7, TCREntities.CHRONOS_SOL.get().getDescription()), dBuilder.opt(8));

        DialogNode back = new DialogNode(dBuilder.ans(7, TCREntities.CHRONOS_SOL.get().getDescription()), dBuilder.opt(8));

        DialogNode whoAmI = new DialogNode(dBuilder.ans(8), dBuilder.opt(9))
                .addChild(new DialogNode(dBuilder.ans(9), dBuilder.opt(-1))
                        .addChild(new DialogNode(dBuilder.ans(10), dBuilder.opt(-1))
                                .addChild(back)));

        DialogNode whatBlackTide = new DialogNode(dBuilder.ans(11), dBuilder.opt(10))
                .addChild(new DialogNode(dBuilder.ans(12), dBuilder.opt(-1))
                        .addChild(new DialogNode(dBuilder.ans(13), dBuilder.opt(-1))
                                .addChild(new DialogNode(dBuilder.ans(14), dBuilder.opt(-1))
                                        .addChild(back))));

        DialogNode next = new DialogNode(dBuilder.ans(15), dBuilder.opt(11))
                .addChild(new DialogNode(dBuilder.ans(16), dBuilder.opt(-1))
                        .addChild(new DialogNode(dBuilder.ans(17), dBuilder.opt(8))
                                .addChild(new DialogNode(dBuilder.ans(18), dBuilder.opt(12))
                                        .addLeaf(dBuilder.opt(13), 1))));

        soga.addChild(whoAmI).addChild(whatBlackTide).addChild(next);
        back.addChild(whoAmI).addChild(whatBlackTide).addChild(next);

        talkBefore.addChild(soga);

        c7.addChild(talkBefore);
        c8.addChild(talkBefore);

        c5.addChild(c7)
                .addChild(c8);
        c6.addChild(c7)
                .addChild(c8);

        c3.addChild(c5)
                .addChild(c6);
        c4.addChild(c5)
                .addChild(c6);

        c1.addChild(c3)
                .addChild(c4);
        c2.addChild(c3)
                .addChild(c4);

        root.addChild(c1)
                .addChild(c2);

        return builder.buildWith(root);
    }

    @Override
    public void handleNpcInteraction(ServerPlayer player, int i) {
        if(i == 1) {
            TCRQuests.TALK_TO_SKY_GOLEM.finish(player);
            canBeHurt = true;
            PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new SetThirdPersonPacket(), player);
            EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerPatch -> {
                playerPatch.toEpicFightMode(true);
            });
            ItemUtils.addItemEntity(player, ModItems.STORM_EYE.get(), 1, ChatFormatting.AQUA.getColor().intValue());
            player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE), SoundSource.PLAYERS, player.getX(), player.getY(), player.getZ(), 1.0F, 1.0F, player.getRandom().nextInt()));
            if(!ExecutionHandler.isHoldingWeapon(player)) {
                ItemUtils.addItemEntity(player, player.getMainHandItem().copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_SWORD.getDefaultInstance());
            }
            EntityUtils.entityForceExecuteToDie(player, this);
        }
        setConversingPlayer(null);
    }

    /**
     * 只对对应的玩家可见，以免串了进度，或者同时出现很多个
     */
    public boolean shouldRender(Player player) {
        return player.getUUID().equals(this.getOwnerUUID()) || (player.isCreative() && !FMLEnvironment.production);
    }

    @Override
    public @NotNull Component getName() {
        return  BTEntityType.SKY_GOLEM.get().getDescription().copy().append("?");
    }

}
