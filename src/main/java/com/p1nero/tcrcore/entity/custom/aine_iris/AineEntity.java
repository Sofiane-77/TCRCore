package com.p1nero.tcrcore.entity.custom.aine_iris;

import com.aetherteam.aether.block.AetherBlocks;
import com.obscuria.aquamirae.registry.AquamiraeEntities;
import com.obscuria.aquamirae.registry.AquamiraeItems;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.api.entity.custom.IEntityNpc;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.dpr.DodgeParryRewardMod;
import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcr_bosses.entity.TCRBossEntities;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.PlayerDataManager;
import com.p1nero.tcrcore.capability.TCRQuestManager;
import com.p1nero.tcrcore.capability.TCRQuests;
import com.p1nero.tcrcore.datagen.TCRAdvancementData;
import com.p1nero.tcrcore.datagen.TCRSkillTreeProvider;
import com.p1nero.tcrcore.entity.TCREntities;
import com.p1nero.tcrcore.gameassets.TCRSkills;
import com.p1nero.tcrcore.item.TCRItems;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.PlayTitlePacket;
import com.p1nero.tcrcore.utils.EntityUtil;
import com.p1nero.tcrcore.utils.FTBTeamUtils;
import com.p1nero.tcrcore.utils.ItemUtil;
import com.p1nero.tcrcore.utils.WorldUtil;
import com.yesman.epicskills.skilltree.SkillTree;
import com.yesman.epicskills.world.capability.SkillTreeProgression;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.player.KeyMappings;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import moe.plushie.armourers_workshop.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class AineEntity extends PathfinderMob implements IEntityNpc, GeoEntity, Merchant {
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final ResourceLocation MP_DESC = ResourceLocation.fromNamespaceAndPath(TCRCoreMod.MOD_ID, "textures/gui/mp_point.png");

    @Nullable
    private Player tradingPlayer;
    private MerchantOffers offers;
    private MerchantOffers iceOffers = new MerchantOffers();
    private MerchantOffers fireOffers = new MerchantOffers();
    private MerchantOffers lightningOffers = new MerchantOffers();
    private MerchantOffers holyOffers = new MerchantOffers();
    private MerchantOffers enderOffers = new MerchantOffers();
    private MerchantOffers bloodOffers = new MerchantOffers();
    private MerchantOffers evocationOffers = new MerchantOffers();
    private MerchantOffers natureOffers = new MerchantOffers();
    private MerchantOffers ballOffers = new MerchantOffers();

    public AineEntity(EntityType<? extends PathfinderMob> p_21683_, Level p_21684_) {
        super(p_21683_, p_21684_);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(FMLEnvironment.production) {
            return false;
        }
        if (source.getEntity() instanceof Player player && player.isCreative()) {
            player.displayClientMessage(Component.translatable("/summon " + ForgeRegistries.ENTITY_TYPES.getKey(this.getType())).withStyle(ChatFormatting.RED), false);
            this.discard();
        }
        return false;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        initOffers();
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = new CompoundTag();
            if (!PlayerDataManager.aineTalked.get(player)) {
                PlayerDataManager.aineTalked.put(player, true);
            }
            this.sendDialogTo(serverPlayer, tag);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogueScreen(CompoundTag serverData) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return null;
        }
        TCRQuestManager.Quest currentQuest = TCRQuestManager.getCurrentQuest(localPlayer);
        StreamDialogueScreenBuilder dialogueScreenBuilder = new StreamDialogueScreenBuilder(this, TCRCoreMod.MOD_ID);
        DialogueComponentBuilder dBuilder = dialogueScreenBuilder.getComponentBuildr();

        //-1继续
        //-2结束对话
        DialogNode root = new DialogNode(dBuilder.ans(0, localPlayer.getDisplayName()));//你来了！我正在阅读这个世界的智库？

        if (PlayerDataManager.gameCleared.get(localPlayer)) {
            root = new DialogNode(dBuilder.ans(-1, localPlayer.getDisplayName()));
        }

        DialogNode aboutChronos = new DialogNode(dBuilder.ans(1), dBuilder.opt(0, TCREntities.CHRONOS_SOL.get().getDescription()))
                .addLeaf(dBuilder.opt(-2));

        DialogNode aboutThisWorld = new DialogNode(dBuilder.ans(2), dBuilder.opt(1))
                .addLeaf(dBuilder.opt(-2));

        if (currentQuest.equals(TCRQuests.TALK_TO_AINE_0)) {
            if (PlayerDataManager.chronosTalked.get(localPlayer)) {
                root.addChild(aboutChronos);
                root.addChild(aboutThisWorld);
            }
            DialogNode aboutSkin = new DialogNode(dBuilder.ans(3), dBuilder.opt(2))
                    .addLeaf(dBuilder.opt(3), 1);
            root.addChild(aboutSkin);
        } else if (currentQuest.equals(TCRQuests.TALK_TO_AINE_CLOUDLAND)) {
            dialogueScreenBuilder.start(dBuilder.ans(4, localPlayer.getDisplayName()))
                    .addOption(5, 5)
                    .addOption(-1, 6)
                    .addOption(-1, 7)
                    .addOption(-1, 8)
                    .addFinalOption(-2, 2);
            return dialogueScreenBuilder.build();
        } else if (TCRQuests.TALK_TO_AINE_ECHO.equals(currentQuest)) {
            dialogueScreenBuilder.start(dBuilder.ans(4, localPlayer.getDisplayName()))
                    .addOption(dBuilder.opt(7, AquamiraeItems.SHIP_GRAVEYARD_ECHO.get().getDescription()), dBuilder.ans(9))
                    .addOption(-1, 10)
                    .thenExecute(3)
                    .addOption(dBuilder.opt(-1), dBuilder.ans(11, TCRBossEntities.MALEDICTUS_HUMANOID.get().getDescription(), TCREntities.CHRONOS_SOL.get().getDescription()))
                    .addOption(dBuilder.opt(8, AquamiraeEntities.CAPTAIN_CORNELIA.get().getDescription(), TCRBossEntities.MALEDICTUS_HUMANOID.get().getDescription()), dBuilder.ans(12, TCRBossEntities.MALEDICTUS_HUMANOID.get().getDescription(), com.github.L_Ender.cataclysm.init.ModItems.CURSED_EYE.get().getDescription()))
                    .addFinalOption(-2, 4);
            return dialogueScreenBuilder.build();
        } else if (TCRQuests.TALK_TO_AINE_MAGIC.equals(currentQuest)) {
            //学魔法
            dialogueScreenBuilder.start(dBuilder.ans(4, localPlayer.getDisplayName()))
                    .addOption(dBuilder.opt(7, TCRItems.NECROMANCY_SCROLL.get().getDescription()), dBuilder.ans(13, TCRItems.NECROMANCY_SCROLL.get().getDescription()))
                    .addOption(-1, 14)
                    .addOption(dBuilder.opt(-1), dBuilder.ans(15, TCRItems.NECROMANCY_SCROLL.get().getDescription()))
                    .addOption(-1, 16)
                    .addOption(-1, 17)
                    .addOption(-1, 18)
                    .addFinalOption(-2, 5);
            return dialogueScreenBuilder.build();
        } else if (TCRQuests.TALK_TO_AINE_MAGIC_2.equals(currentQuest)) {
            TCRCoreMod.LOGGER.debug("TALK_TO_AINE_MAGIC_2 Start");
            //NOTE 虽然不知道为嘛，但是我加了个try catch它就修好了= =，移除可能导致对话bug
            try {
                //介绍施法
                DialogNode learnt = new DialogNode(dBuilder.ans(22, TCRItems.MAGIC_BOTTLE.get().getDescription().copy().withStyle(ChatFormatting.AQUA), TCRItems.MAGIC_BOTTLE.get().getDescription().copy().withStyle(ChatFormatting.AQUA)), dBuilder.opt(9))
                        .addExecutable(dialogueScreen -> {
                            dialogueScreen.setPicture(null);
                        })
                        .addChild(new DialogNode(dBuilder.ans(23, TCRItems.MAGIC_BOTTLE.get().getDescription().copy().withStyle(ChatFormatting.AQUA)), dBuilder.opt(-1))
                                .addLeaf(dBuilder.opt(-2), 6));

                root = new DialogNode(dBuilder.ans(19, localPlayer.getDisplayName()), dBuilder.opt(10))
                        .addExecutable(dialogueScreen -> {
                            dialogueScreen.setPicture(null);
                        });

                DialogNode next = new DialogNode(dBuilder.ans(20, KeyMappings.SPELLBOOK_CAST_ACTIVE_KEYMAP.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GOLD)), dBuilder.opt(-1))
                        .addChild(new DialogNode(dBuilder.ans(21), dBuilder.opt(-1))
                                .addExecutable(dialogueScreen -> {
//                                dialogueScreen.setPicture(MP_DESC);
                                    //TODO 调整插图
                                })
                                .addChild(learnt)
                                .addChild(root));
                root.addChild(next);
            } catch (Exception e) {
                TCRCoreMod.LOGGER.error("Err!", e);
            }
            TCRCoreMod.LOGGER.debug("TALK_TO_AINE_MAGIC_2 END");

        } else if (TCRQuests.TALK_TO_AINE_1.equals(currentQuest)) {
            //聊聊最近的冒险（没啥用，纯增加氛围
            root = new DialogNode(dBuilder.ans(24, localPlayer.getDisplayName()));
            DialogNode easy = new DialogNode(dBuilder.ans(25), dBuilder.opt(11));
            DialogNode hard = new DialogNode(dBuilder.ans(25), dBuilder.opt(12));

            DialogNode next = new DialogNode(dBuilder.ans(26), dBuilder.opt(13, TCREntities.AINE.get().getDescription()))
                    .addChild(new DialogNode(dBuilder.ans(27), dBuilder.opt(14))
                            .addChild(new DialogNode(dBuilder.ans(28), dBuilder.opt(15))
                                    .addChild(new DialogNode(dBuilder.ans(29), dBuilder.opt(16))
                                            .addLeaf(dBuilder.opt(-2), 9))));
            easy.addChild(next);
            hard.addChild(next);

            root.addChild(easy)
                    .addChild(hard);
        } else if (TCRQuests.TALK_TO_AINE_SAMSARA.equals(currentQuest)) {
            //打开轮回绝境
            return dialogueScreenBuilder.start(dBuilder.ans(24, localPlayer.getDisplayName()))
                    .addOption(dBuilder.opt(7, TCRItems.WITHER_SOUL_STONE.get().getDescription()), dBuilder.ans(30, Items.GHAST_TEAR.getDescription().copy().withStyle(ChatFormatting.AQUA), WorldUtil.SAMSARA_NAME.copy().withStyle(ChatFormatting.GOLD)))
                    .addOption(dBuilder.opt(17, WorldUtil.SAMSARA_NAME), dBuilder.ans(31, WorldUtil.SAMSARA_NAME, WorldUtil.SAMSARA_NAME))
                    .addOption(-1, 32)
                    .addFinalOption(-2, 10)
                    .build();
        } else if (TCRQuests.TALK_TO_AINE_2.equals(currentQuest)) {
            return dialogueScreenBuilder.start(dBuilder.ans(33, localPlayer.getDisplayName(), WorldUtil.AETHER_NAME))
                    .addOption(dBuilder.ans(34, WorldUtil.AETHER_NAME, TCREntities.AINE.get().getDescription()), dBuilder.opt(18, TCREntities.AINE.get().getDescription()))
                    .addOption(dBuilder.ans(35), dBuilder.opt(13, TCREntities.AINE.get().getDescription()))
                    .addOption(dBuilder.ans(36, com.github.L_Ender.cataclysm.init.ModItems.VOID_EYE.get().getDescription(), localPlayer.getDisplayName()), dBuilder.opt(-1))
                    .addFinalOption(dBuilder.opt(-2), 11)
                    .build();
        } else if (TCRQuests.TALK_TO_AINE_GAME_CLEAR.equals(currentQuest)) {
            //后日谈，可以询问所有事
            root = new DialogNode(dBuilder.ans(37, localPlayer.getDisplayName()));
            DialogNode askRoot = new DialogNode(dBuilder.ans(38), dBuilder.opt(-1));

            DialogNode aboutYou = new DialogNode(dBuilder.ans(39), dBuilder.opt(19))
                    .addChild(askRoot);
            DialogNode aboutAngel = new DialogNode(dBuilder.ans(40), dBuilder.opt(20))
                    .addChild(askRoot);
            DialogNode aboutBlackTide = new DialogNode(dBuilder.ans(41), dBuilder.opt(21))
                    .addChild(new DialogNode(dBuilder.ans(42), dBuilder.opt(-1))
                            .addChild(askRoot));

            DialogNode aboutFuture = new DialogNode(dBuilder.ans(43), dBuilder.opt(22))
                    .addChild(askRoot);

            DialogNode c1 = new DialogNode(dBuilder.ans(49), dBuilder.opt(27))
                    .addLeaf(dBuilder.opt(-2), 12);
            DialogNode c2 = new DialogNode(dBuilder.ans(49), dBuilder.opt(28))
                    .addLeaf(dBuilder.opt(-2), 12);
            DialogNode c3 = new DialogNode(dBuilder.ans(49), dBuilder.opt(29))
                    .addLeaf(dBuilder.opt(-2), 12);

            DialogNode next = new DialogNode(dBuilder.ans(44), dBuilder.opt(23))
                    .addChild(new DialogNode(dBuilder.ans(45), dBuilder.opt(24))
                            .addChild(new DialogNode(dBuilder.ans(46, localPlayer.getDisplayName()), dBuilder.opt(25))
                                    .addChild(new DialogNode(dBuilder.ans(47), dBuilder.opt(25))
                                            .addChild(new DialogNode(dBuilder.ans(48), dBuilder.opt(26))
                                                    .addChild(c1)
                                                    .addChild(c2)
                                                    .addChild(c3)))));
            askRoot.addChild(aboutYou)
                    .addChild(aboutAngel)
                    .addChild(aboutBlackTide)
                    .addChild(aboutFuture)
                    .addChild(next);

            root.addChild(askRoot);
        } else {
            if (!PlayerDataManager.gameCleared.get(localPlayer)) {
                if (PlayerDataManager.chronosTalked.get(localPlayer)) {
                    root.addChild(aboutChronos);
                    root.addChild(aboutThisWorld);
                    root.addLeaf(dBuilder.opt(-2));
                }
            }
            if (TCRQuests.TALK_TO_AINE_MAGIC.isFinished(localPlayer)) {
                DialogNode learnMagic = new DialogNode(dBuilder.ans(-2), dBuilder.opt(-3))
                        .addLeaf(SchoolRegistry.ICE.get().getDisplayName(), -1)
                        .addLeaf(SchoolRegistry.FIRE.get().getDisplayName(), -2)
                        .addLeaf(SchoolRegistry.LIGHTNING.get().getDisplayName(), -3)
                        .addLeaf(SchoolRegistry.HOLY.get().getDisplayName(), -4)
                        .addLeaf(SchoolRegistry.ENDER.get().getDisplayName(), -5)
                        .addLeaf(SchoolRegistry.BLOOD.get().getDisplayName(), -6)
                        .addLeaf(SchoolRegistry.EVOCATION.get().getDisplayName(), -7)
                        .addLeaf(SchoolRegistry.NATURE.get().getDisplayName(), -8)
                        .addLeaf(ItemRegistry.UPGRADE_ORB.get().getDescription(), -9);
                root.addChild(learnMagic);
                root.addLeaf(dBuilder.opt(-4), 8);
            }
        }

        return dialogueScreenBuilder.buildWith(root);
    }

    @Override
    public void handleNpcInteraction(ServerPlayer serverPlayer, int code) {
        //初次对话&领取时装
        if (code == 1) {
            TCRQuests.TALK_TO_AINE_0.finish(serverPlayer);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (member -> {
                ItemUtil.addItemEntity(member, ModItems.SKIN_TEMPLATE.get(), 20, ChatFormatting.GOLD.getColor());
                ItemUtil.addItemEntity(member, ModItems.SKIN_LIBRARY_GLOBAL.get(), 1, ChatFormatting.GOLD.getColor());
                ItemUtil.addItemEntity(member, ModItems.SKIN_LIBRARY.get(), 1, ChatFormatting.GOLD.getColor());
                ItemUtil.addItemEntity(member, ModItems.SKINNING_TABLE.get(), 1, ChatFormatting.GOLD.getColor());
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_CHAPTER), member);
            }));
        }
        //聊幻境
        if (code == 2) {
            TCRQuests.TALK_TO_AINE_CLOUDLAND.finish(serverPlayer);
        }
        //播放解读海船回响特效
        if (code == 3) {
            Vec3 dis = this.position().subtract(serverPlayer.position());
            Vec3 pos = serverPlayer.position().add(dis.scale(0.5F));
            serverPlayer.serverLevel().sendParticles(
                    ParticleTypes.SOUL,
                    pos.x,
                    pos.y,
                    pos.z,
                    10,
                    0, 0.1, 0,
                    0.1f
            );
            EntityUtil.playLocalSound(serverPlayer, SoundEvents.BEACON_ACTIVATE);
            return;
        }

        if (code == 4) {
            TCRQuests.TALK_TO_AINE_ECHO.finish(serverPlayer);
            TCRQuests.TALK_TO_CHRONOS_4.start(serverPlayer);
        }

        //解锁魔法图鉴
        if (code == 5) {
            TCRQuests.TALK_TO_AINE_MAGIC.finish(serverPlayer);
            TCRQuests.TRY_TO_LEARN_MAGIC.start(serverPlayer);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (member -> {
                TCRAdvancementData.finishAdvancement("unlock_magic_and_boss", member);
                ItemUtil.addItemEntity(member, getSpellScroll(SpellRegistry.MAGIC_ARROW_SPELL.get()), ChatFormatting.AQUA.getColor());
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_CHAPTER), member);
            }));
        }

        //获得蓝瓶
        if (code == 6) {
            TCRQuests.TALK_TO_AINE_MAGIC_2.finish(serverPlayer);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (member -> {
                ItemUtil.addItemEntity(member, TCRItems.MAGIC_BOTTLE.get(), 1, ChatFormatting.AQUA.getColor());
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_SKILL), member);
                member.getCapability(SkillTreeProgression.SKILL_TREE_PROGRESSION).ifPresent(skillTreeProgression -> {
                    skillTreeProgression.unlockTree(TCRSkillTreeProvider.MAGIC, member);
                });
            }));
        }

        if (code == 8) {
            //打开奥术铁砧
            BlockState blockState = serverPlayer.level().getBlockState(WorldUtil.ARCANE_ANVIL_BLOCK_POS);
            serverPlayer.openMenu(blockState.getMenuProvider(serverPlayer.level(), WorldUtil.ARCANE_ANVIL_BLOCK_POS));
            serverPlayer.awardStat(Stats.INTERACT_WITH_ANVIL);
        }

        if (code == 9) {
            //闲聊
            TCRQuests.TALK_TO_AINE_1.finish(serverPlayer);
        }

        if (code == 10) {
            TCRQuests.TALK_TO_AINE_SAMSARA.finish(serverPlayer);
            TCRQuests.GO_TO_SAMSARA.start(serverPlayer);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (member -> {
                TCRAdvancementData.finishAdvancement("unlock_epic_boss", member);
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_CHAPTER), member);
            }));

        }

        if (code == 11) {
            TCRQuests.TALK_TO_AINE_2.finish(serverPlayer);
        }

        if (code == 12) {
            TCRQuests.TALK_TO_AINE_GAME_CLEAR.finish(serverPlayer);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(serverPlayer, (member -> {
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.TO_BE_CONTINUE), member);
                EntityUtil.playLocalSound(member, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
            }));
        }

        //法术交易
        if (code <= -1 && code >= -9) {
            switch (code) {
                case -1 -> offers = iceOffers;
                case -2 -> offers = fireOffers;
                case -3 -> offers = lightningOffers;
                case -4 -> offers = holyOffers;
                case -5 -> offers = enderOffers;
                case -6 -> offers = bloodOffers;
                case -7 -> offers = evocationOffers;
                case -8 -> offers = natureOffers;
                case -9 -> offers = ballOffers;
            }
            this.startTrade(serverPlayer);
        }

        this.setConversingPlayer(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {

            //位置矫正保险
            if (tickCount % 100 == 0) {
                BlockPos myPos = this.getOnPos();
                if (myPos.getX() != WorldUtil.AINE_POS.getX() || myPos.getZ() != WorldUtil.AINE_POS.getZ()) {
                    this.setPos(new BlockPos(WorldUtil.AINE_POS).getCenter());
                }
            }
            if (getConversingPlayer() != null && (getConversingPlayer().isRemoved() || getConversingPlayer().isDeadOrDying() || getConversingPlayer().distanceTo(this) > 5)) {
                setConversingPlayer(null);
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    protected <E extends AineEntity> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * 开始交易
     * 需要改变交易表则去重写 {@link #getOffers()}
     */
    public void startTrade(ServerPlayer serverPlayer) {
        setTradingPlayer(serverPlayer);
        openTradingScreen(serverPlayer, Component.empty(), 1);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        tradingPlayer = player;
    }

    @Nullable
    @Override
    public Player getTradingPlayer() {
        return tradingPlayer;
    }

    @Override
    public @NotNull MerchantOffers getOffers() {
        return offers;
    }

    public void initOffers() {
        iceOffers = new MerchantOffers();
        fireOffers = new MerchantOffers();
        lightningOffers = new MerchantOffers();
        holyOffers = new MerchantOffers();
        enderOffers = new MerchantOffers();
        bloodOffers = new MerchantOffers();
        evocationOffers = new MerchantOffers();
        natureOffers = new MerchantOffers();
        ballOffers = new MerchantOffers();
        // 冰霜
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 1),
                new ItemStack(Items.SNOWBALL, 1),
                getSpellScroll(SpellRegistry.FROSTBITE_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 2),
                new ItemStack(Items.SNOWBALL, 1),
                getSpellScroll(SpellRegistry.FROSTWAVE_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 3),
                new ItemStack(Items.ICE, 1),
                getSpellScroll(SpellRegistry.CONE_OF_COLD_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 1),
                new ItemStack(Items.SNOWBALL, 1),
                getSpellScroll(SpellRegistry.ICE_SPIKES_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 2),
                new ItemStack(Items.ICE, 1),
                getSpellScroll(SpellRegistry.ICE_TOMB_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 1),
                new ItemStack(Items.ARROW, 1),
                getSpellScroll(SpellRegistry.ICICLE_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 3),
                new ItemStack(Items.ICE, 1),
                getSpellScroll(SpellRegistry.RAY_OF_FROST_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 3),
                new ItemStack(Items.SNOWBALL, 1),
                getSpellScroll(SpellRegistry.SNOWBALL_SPELL.get()),
                142857, 0, 0.01f));
        iceOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 1),
                new ItemStack(Items.SNOWBALL, 10),
                getSpellScroll(SpellRegistry.ICE_BLOCK_SPELL.get()),
                142857, 0, 0.01f));

        // 炽焰
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 1),
                new ItemStack(Items.BLAZE_POWDER, 1),
                getSpellScroll(SpellRegistry.BLAZE_STORM_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 1),
                new ItemStack(Items.SPECTRAL_ARROW, 1),
                getSpellScroll(SpellRegistry.FIRE_ARROW_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 1),
                new ItemStack(Items.SPECTRAL_ARROW, 1),
                getSpellScroll(SpellRegistry.FIREBOLT_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 1),
                new ItemStack(Items.BLAZE_POWDER, 1),
                getSpellScroll(SpellRegistry.FIRE_BREATH_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 3),
                new ItemStack(Items.MAGMA_CREAM, 1),
                getSpellScroll(SpellRegistry.FIREBALL_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 3),
                new ItemStack(Items.MAGMA_CREAM, 1),
                getSpellScroll(SpellRegistry.FLAMING_BARRAGE_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 3),
                new ItemStack(Items.BLAZE_POWDER, 1),
                getSpellScroll(SpellRegistry.FLAMING_STRIKE_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 2),
                new ItemStack(Items.MAGMA_CREAM, 1),
                getSpellScroll(SpellRegistry.MAGMA_BOMB_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 3),
                new ItemStack(Items.BLAZE_POWDER, 1),
                getSpellScroll(SpellRegistry.RAISE_HELL_SPELL.get()),
                142857, 0, 0.01f));
        fireOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 1),
                new ItemStack(Items.BLAZE_POWDER, 1),
                getSpellScroll(SpellRegistry.SCORCH_SPELL.get()),
                142857, 0, 0.01f));

        // 唤魔
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 3),
                new ItemStack(Items.ARROW, 1),
                getSpellScroll(SpellRegistry.ARROW_VOLLEY_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 3),
                new ItemStack(Items.CREEPER_HEAD, 1),
                getSpellScroll(SpellRegistry.CHAIN_CREEPER_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.GHAST_TEAR, 1),
                getSpellScroll(SpellRegistry.FANG_STRIKE_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 2),
                new ItemStack(Items.GHAST_TEAR, 1),
                getSpellScroll(SpellRegistry.FANG_WARD_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.GUNPOWDER, 1),
                getSpellScroll(SpellRegistry.FIRECRACKER_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.GHAST_TEAR, 1),
                getSpellScroll(SpellRegistry.GUST_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.CREEPER_HEAD, 1),
                getSpellScroll(SpellRegistry.LOB_CREEPER_SPELL.get()),
                142857, 0, 0.01f));
        evocationOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 1),
                new ItemStack(Items.GHAST_TEAR, 1),
                getSpellScroll(SpellRegistry.SHIELD_SPELL.get()),
                142857, 0, 0.01f));

        // 雷霆
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.ASCENSION_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.BALL_LIGHTNING_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.CHAIN_LIGHTNING_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.ELECTROCUTE_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.LIGHTNING_BOLT_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.LIGHTNING_LANCE_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 2),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.SHOCKWAVE_SPELL.get()),
                142857, 0, 0.01f));
        lightningOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 2),
                new ItemStack(AetherBlocks.CRYSTAL_LEAVES.get(), 1),
                getSpellScroll(SpellRegistry.THUNDERSTORM_SPELL.get()),
                142857, 0, 0.01f));

        // 末影
        enderOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 1),
                new ItemStack(Items.DRAGON_BREATH, 1),
                getSpellScroll(SpellRegistry.DRAGON_BREATH_SPELL.get()),
                142857, 0, 0.01f));
        enderOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 1),
                new ItemStack(Items.ARROW, 1),
                getSpellScroll(SpellRegistry.MAGIC_ARROW_SPELL.get()),
                142857, 0, 0.01f));
        enderOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 1),
                new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), 1),
                getSpellScroll(SpellRegistry.MAGIC_MISSILE_SPELL.get()),
                142857, 0, 0.01f));
        enderOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 3),
                new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), 1),
                getSpellScroll(SpellRegistry.SHADOW_SLASH.get()),
                142857, 0, 0.01f));
        enderOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 1),
                new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), 1),
                getSpellScroll(SpellRegistry.STARFALL_SPELL.get()),
                142857, 0, 0.01f));

        // 神圣
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.BLESSING_OF_LIFE_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 5),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.DIVINE_SMITE_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.FORTIFY_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 5),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.GREATER_HEAL_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.GUIDING_BOLT_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 4),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.HASTE_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 2),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.HEAL_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.SUNBEAM_SPELL.get()),
                142857, 0, 0.01f));
        holyOffers.add(new MerchantOffer(
                new ItemStack(Items.GOLDEN_APPLE, 1),
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 1),
                getSpellScroll(SpellRegistry.WISP_SPELL.get()),
                142857, 0, 0.01f));

        // 猩红
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 1),
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 1),
                getSpellScroll(SpellRegistry.BLOOD_NEEDLES_SPELL.get()),
                142857, 0, 0.01f));
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 1),
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 1),
                getSpellScroll(SpellRegistry.ACUPUNCTURE_SPELL.get()),
                142857, 0, 0.01f));
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 3),
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 1),
                getSpellScroll(SpellRegistry.BLOOD_SLASH_SPELL.get()),
                142857, 0, 0.01f));
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 1),
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 1),
                getSpellScroll(SpellRegistry.DEVOUR_SPELL.get()),
                142857, 0, 0.01f));
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 1),
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 1),
                getSpellScroll(SpellRegistry.RAY_OF_SIPHONING_SPELL.get()),
                142857, 0, 0.01f));
        bloodOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOOD_VIAL.get(), 1),
                new ItemStack(Items.WITHER_SKELETON_SKULL, 1),
                getSpellScroll(SpellRegistry.WITHER_SKULL_SPELL.get()),
                142857, 0, 0.01f));

        // 自然
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 1),
                new ItemStack(Items.MOSS_BLOCK, 1),
                getSpellScroll(SpellRegistry.EARTHQUAKE_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 1),
                new ItemStack(Items.SHROOMLIGHT, 1),
                getSpellScroll(SpellRegistry.FIREFLY_SWARM_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 1),
                new ItemStack(Items.ARROW, 1),
                getSpellScroll(SpellRegistry.POISON_ARROW_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 1),
                new ItemStack(Items.MUD, 1),
                getSpellScroll(SpellRegistry.POISON_BREATH_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 1),
                new ItemStack(Items.MUD, 1),
                getSpellScroll(SpellRegistry.POISON_SPLASH_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 2),
                new ItemStack(Items.SPIDER_EYE, 1),
                getSpellScroll(SpellRegistry.SPIDER_ASPECT_SPELL.get()),
                142857, 0, 0.01f));
        natureOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 3),
                new ItemStack(Items.MOSS_BLOCK, 1),
                getSpellScroll(SpellRegistry.STOMP_SPELL.get()),
                142857, 0, 0.01f));

        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FROZEN_BONE_SHARD.get(), 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.ICE_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(Items.BLAZE_ROD, 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.FIRE_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.BLOODY_VELLUM.get(), 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.BLOOD_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(Items.ENDER_EYE, 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.ENDER_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.FIREFLY_JAR_ITEM.get(), 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.NATURE_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.LIGHTNING_BOTTLE.get(), 1),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.LIGHTNING_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(Items.EMERALD, 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.EVOCATION_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(Items.NETHERITE_INGOT, 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.PROTECTION_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.DIVINE_PEARL.get(), 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.HOLY_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(ItemRegistry.ARCANE_ESSENCE.get(), 4),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.MANA_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
        ballOffers.add(new MerchantOffer(
                new ItemStack(TCRItems.SOUL_FRAGMENT.get(), 1),
                new ItemStack(TCRItems.ARTIFACT_TICKET.get(), 1),
                ItemRegistry.COOLDOWN_UPGRADE_ORB.get().getDefaultInstance(),
                142857, 0, 0.01f));
    }

    public ItemStack getSpellScroll(AbstractSpell spell) {
        var itemstack = new ItemStack(ItemRegistry.SCROLL.get());
        ISpellContainer.createScrollContainer(spell, Math.max(spell.getMaxLevel() / 2, 1), itemstack);
        return itemstack;
    }

    @Override
    public void overrideOffers(@NotNull MerchantOffers merchantOffers) {

    }

    @Override
    public void notifyTrade(@NotNull MerchantOffer merchantOffer) {

    }

    @Override
    public void notifyTradeUpdated(@NotNull ItemStack itemStack) {

    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int i) {

    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public @NotNull SoundEvent getNotifyTradeSound() {
        return SoundEvents.EXPERIENCE_ORB_PICKUP;
    }

    @Override
    public boolean isClientSide() {
        return level().isClientSide;
    }

    @Override
    public boolean removeWhenFarAway(double p_21542_) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

}
