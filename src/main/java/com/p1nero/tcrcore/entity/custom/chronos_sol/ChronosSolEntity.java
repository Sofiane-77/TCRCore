package com.p1nero.tcrcore.entity.custom.chronos_sol;

import com.github.L_Ender.cataclysm.init.ModItems;
import com.obscuria.aquamirae.registry.AquamiraeItems;
import com.p1nero.dialog_lib.api.component.DialogueComponentBuilder;
import com.p1nero.dialog_lib.api.component.DialogNode;
import com.p1nero.dialog_lib.api.entity.custom.IEntityNpc;
import com.p1nero.dialog_lib.api.entity.goal.LookAtConservingPlayerGoal;
import com.p1nero.dialog_lib.client.screen.DialogueScreen;
import com.p1nero.dialog_lib.client.screen.builder.StreamDialogueScreenBuilder;
import com.p1nero.dpr.DodgeParryRewardMod;
import com.p1nero.fast_tpa.network.PacketRelay;
import com.p1nero.tcr_bosses.entity.TCRBossEntities;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.*;
import com.p1nero.tcrcore.entity.TCREntities;
import com.p1nero.tcrcore.gameassets.TCRSkills;
import com.p1nero.tcrcore.item.TCRItems;
import com.p1nero.tcrcore.network.TCRPacketHandler;
import com.p1nero.tcrcore.network.packet.clientbound.PlayTitlePacket;
import com.p1nero.tcrcore.save_data.TCRDimSaveData;
import com.p1nero.tcrcore.utils.EntityUtil;
import com.p1nero.tcrcore.utils.FTBTeamUtils;
import com.p1nero.tcrcore.utils.ItemUtil;
import com.p1nero.tcrcore.utils.WorldUtil;
import com.yesman.epicskills.registry.entry.EpicSkillsSkillTrees;
import com.yesman.epicskills.skilltree.SkillTree;
import com.yesman.epicskills.world.capability.SkillTreeProgression;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.p1nero.ss.gameassets.skills.FlyingSkills;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.merlin204.mimic.util.PositionTeleporter;
import org.merlin204.wraithon.WraithonMod;
import org.merlin204.wraithon.entity.WraithonEntities;
import org.merlin204.wraithon.worldgen.WraithonDimensions;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 曦轮
 */
public class ChronosSolEntity extends PathfinderMob implements IEntityNpc, GeoEntity {

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.god_girl.idle2");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private Player conversingPlayer;

    public ChronosSolEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void startSeenByPlayer(@NotNull ServerPlayer serverPlayer) {
        super.startSeenByPlayer(serverPlayer);
        TCRCapabilityProvider.syncPlayerDataToClient(serverPlayer);//保险
    }

    @Override
    public void tick() {
        super.tick();
        if(level() instanceof ServerLevel) {
            if(conversingPlayer != null && (conversingPlayer.isRemoved() || conversingPlayer.isDeadOrDying() || conversingPlayer.distanceTo(this) > 5)) {
                conversingPlayer = null;
            }
            //位置矫正保险
            if(tickCount % 100 == 0) {
                BlockPos myPos = this.getOnPos();
                if(myPos.getX() != WorldUtil.CHRONOS_SOL_BLOCK_POS.getX() || myPos.getZ() != WorldUtil.CHRONOS_SOL_BLOCK_POS.getZ()) {
                    this.setPos(new BlockPos(WorldUtil.CHRONOS_SOL_BLOCK_POS).getCenter());
                }
            }
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float value) {
        if(FMLEnvironment.production) {
            return false;
        }
        if(source.getEntity() instanceof Player player && player.isCreative()) {
            player.displayClientMessage(Component.translatable("/summon " + ForgeRegistries.ENTITY_TYPES.getKey(this.getType())).withStyle(ChatFormatting.RED), false);
            this.discard();
        }
        return false;
    }

    public static AttributeSupplier setAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0f)
                .add(Attributes.ATTACK_DAMAGE, 1.0f)
                .add(Attributes.ATTACK_SPEED, 2.0f)
                .add(Attributes.MOVEMENT_SPEED, 0.3f)
                .add(Attributes.KNOCKBACK_RESISTANCE, 114514f)
                .build();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new LookAtConservingPlayerGoal<>(this));
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double p_21542_) {
        return false;
    }

    @Override
    protected @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        //通关就不可交互，假装看不见～
        if(!canInteract(player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            CompoundTag tag = new CompoundTag();
            this.sendDialogTo(serverPlayer, tag);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public DialogueScreen getDialogueScreen(CompoundTag compoundTag) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        TCRQuestManager.Quest currentQuest = TCRQuestManager.getCurrentQuest(localPlayer);
        StreamDialogueScreenBuilder treeBuilder = new StreamDialogueScreenBuilder(this, TCRCoreMod.MOD_ID);
        DialogueComponentBuilder dBuilder = treeBuilder.getComponentBuildr();

        //-1:继续
        //-2:结束对话
        DialogNode root = new DialogNode(dBuilder.ans(0), dBuilder.opt(-3));//冒险可还顺利？

        DialogNode aboutThisWorld = new DialogNode(dBuilder.ans(1), dBuilder.opt(0))
                .addChild(new DialogNode(dBuilder.ans(2), dBuilder.opt(-1))
                        .addChild(root)
                        .addLeaf(dBuilder.opt(-2)));

        DialogNode aboutAine = new DialogNode(dBuilder.ans(3), dBuilder.opt(1, TCREntities.AINE.get().getDescription()))
                .addLeaf(dBuilder.opt(-2));

        DialogNode aboutFerryGirl = new DialogNode(dBuilder.ans(4), dBuilder.opt(2, TCREntities.FERRY_GIRL.get().getDescription()))
                .addLeaf(dBuilder.opt(-2));

        DialogNode aboutOrnn = new DialogNode(dBuilder.ans(5), dBuilder.opt(3, TCREntities.ORNN.get().getDescription()))
                .addLeaf(dBuilder.opt(-2));

        if(TCRQuests.TALK_TO_CHRONOS_0.equals(currentQuest)) {
            //初次对话
            root = new DialogNode(dBuilder.ans(6), dBuilder.opt(-3));
            //你是何人
            DialogNode aboutMe = new DialogNode(dBuilder.ans(7, TCREntities.CHRONOS_SOL.get().getDescription()), dBuilder.opt(4))
                    .addChild(new DialogNode(dBuilder.ans(8), dBuilder.opt(5))
                            .addChild(root)
                            .addLeaf(dBuilder.opt(-2)));

            aboutThisWorld = new DialogNode(dBuilder.ans(1), dBuilder.opt(0))
                    .addChild(new DialogNode(dBuilder.ans(2), dBuilder.opt(-1))
                            .addChild(root)
                            .addLeaf(dBuilder.opt(-2)));
            //关于接下来的行动
            DialogNode aboutNext = new DialogNode(dBuilder.ans(9), dBuilder.opt(6))
                    .addChild(new DialogNode(dBuilder.ans(10), dBuilder.opt(-1))
                            .addChild(new DialogNode(dBuilder.ans(11, TCREntities.ORNN.get().getDescription().copy().withStyle(ChatFormatting.GOLD), TCREntities.FERRY_GIRL.get().getDescription().copy().withStyle(ChatFormatting.GOLD)), dBuilder.opt(-1))
                                    .addLeaf(dBuilder.opt(-4, TCRItems.LAND_RESONANCE_STONE.get().getDescription()), 1)));

            root.addChild(aboutMe)
                    .addChild(aboutThisWorld)
                    .addChild(aboutNext);
        } else if(TCRQuests.TALK_TO_CHRONOS_1.equals(currentQuest)) {
            //提交沙漠眼
            root = new DialogNode(dBuilder.ans(12, ModItems.DESERT_EYE.get().getDescription()))
                    .addChild(new DialogNode(dBuilder.ans(13), dBuilder.opt(7, ModItems.DESERT_EYE.get().getDescription()))
                            .addChild(new DialogNode(dBuilder.ans(14), dBuilder.opt(-1))
                                    .addChild(new DialogNode(dBuilder.ans(15), dBuilder.opt(-1))
                                            .addLeaf(dBuilder.opt(-2), 2))));
        } else if(TCRQuests.TALK_TO_CHRONOS_2.equals(currentQuest)) {
            //充能完毕，去开始领海洋共鸣石
            root = new DialogNode(dBuilder.ans(16))
                    .addLeaf(dBuilder.opt(-4, TCRItems.OCEAN_RESONANCE_STONE.get().getDescription()), 3);
        } else if(TCRQuests.TALK_TO_CHRONOS_3.equals(currentQuest)) {
            //问海船墓地回响
            treeBuilder.start(dBuilder.ans(12, ModItems.ABYSS_EYE.get().getDescription()))
                    .addOption(dBuilder.opt(7, ModItems.ABYSS_EYE.get().getDescription()), dBuilder.ans(17))
                    .addOption(dBuilder.opt(8, AquamiraeItems.SHIP_GRAVEYARD_ECHO.get().getDescription()), dBuilder.ans(18, AquamiraeItems.SHIP_GRAVEYARD_ECHO.get().getDescription()))
                    .addFinalOption(dBuilder.opt(-2), 4);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_4.equals(currentQuest)) {
            treeBuilder.start(19)
                    .addOption(dBuilder.opt(8, AquamiraeItems.SHIP_GRAVEYARD_ECHO.get().getDescription()), dBuilder.ans(20, TCRBossEntities.MALEDICTUS_HUMANOID.get().getDescription()))
                    .addOption(-1, 21)
                    .addFinalOption(dBuilder.opt(-4, TCRItems.CURSED_RESONANCE_STONE.get().getDescription()), 5);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_5.equals(currentQuest)) {
            //找回诅咒眼
            treeBuilder.start(dBuilder.ans(12, ModItems.CURSED_EYE.get().getDescription()))
                    .addOption(dBuilder.opt(7, ModItems.CURSED_EYE.get().getDescription()), dBuilder.ans(22))
                    .addFinalOption(-2, 6);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_6.equals(currentQuest)) {
            //充能完毕，去开始领炉心共鸣石
            root = new DialogNode(dBuilder.ans(16))
                    .addLeaf(dBuilder.opt(-4, TCRItems.CORE_RESONANCE_STONE.get().getDescription()), 7);
        } else if(TCRQuests.TALK_TO_CHRONOS_7.equals(currentQuest)) {
            //已找回烈焰眼
            treeBuilder.start(dBuilder.ans(12, ModItems.FLAME_EYE.get().getDescription()))
                    .addOption(dBuilder.opt(7, ModItems.FLAME_EYE.get().getDescription()), dBuilder.ans(23))
                    .addFinalOption(-2, 8);
            return treeBuilder.build();
        }  else if(TCRQuests.TALK_TO_CHRONOS_8.equals(currentQuest)) {
            //充能完毕，去开始领地狱共鸣石
            treeBuilder.start(dBuilder.ans(24))
                    .addOption(9, 25)
                    .addOption(dBuilder.opt(-1), dBuilder.ans(26,ModItems.FLAME_EYE.get().getDescription(), TCRItems.CORE_FLINT.get().getDescription().copy().withStyle(ChatFormatting.GOLD), TCRItems.CORE_FLINT.get().getDescription()))
                    .addOption(-1, 27)
                    .addFinalOption(dBuilder.opt(-4, TCRItems.CORE_FLINT.get().getDescription()), 9);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_9.equals(currentQuest)) {
            //出发找回凋零眼
            treeBuilder.start(28)
                    .addOption(dBuilder.opt(10), dBuilder.ans(29, TCRBossEntities.HARBINGER_HUMANOID.get().getDescription()))
                    .addOption(dBuilder.opt(-1), dBuilder.ans(30))
                    .addOption(dBuilder.opt(11), dBuilder.ans(31, Items.SOUL_SAND.getDescription(), Items.SOUL_SAND.getDescription(), Items.WITHER_SKELETON_SKULL.getDescription()))
                    .addFinalOption(-2, 10);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_10.equals(currentQuest)) {
            //已找回凋零眼
            treeBuilder.start(dBuilder.ans(12, ModItems.MECH_EYE.get().getDescription()))
                    .addOption(dBuilder.opt(8, TCRItems.WITHER_SOUL_STONE.get().getDescription()), dBuilder.ans(32, TCRItems.WITHER_SOUL_STONE.get().getDescription(), Component.translatable("travelerstitles.pbf1.sanctum_of_the_battle1")))
                    .addOption(dBuilder.opt(-1), dBuilder.ans(33, Component.translatable("travelerstitles.pbf1.sanctum_of_the_battle1")))
                    .addOption(dBuilder.opt(-1), dBuilder.ans(34, TCREntities.AINE.get().getDescription(), TCRItems.WITHER_SOUL_STONE.get().getDescription()))
                    .addFinalOption(-2, 11);
            return treeBuilder.build();
        } else if(TCRQuests.TALK_TO_CHRONOS_11.equals(currentQuest)) {
            //充能完去领天域共鸣石
            DialogNode whatWrong = new DialogNode(dBuilder.ans(36), dBuilder.opt(12, TCREntities.CHRONOS_SOL.get().getDescription()));
            DialogNode youLookTerrible = new DialogNode(dBuilder.ans(36), dBuilder.opt(13, TCREntities.CHRONOS_SOL.get().getDescription()));

            DialogNode next = new DialogNode(dBuilder.ans(37), dBuilder.opt(-1))
                    .addChild(new DialogNode(dBuilder.ans(38), dBuilder.opt(-1))
                            .addChild(new DialogNode(dBuilder.ans(39), dBuilder.opt(-1))
                                    .addChild(new DialogNode(dBuilder.ans(40, ModItems.STORM_EYE.get().getDescription().copy().withStyle(ChatFormatting.AQUA)), dBuilder.opt(-1))
                                            .addLeaf(dBuilder.opt(-4, TCRItems.SKY_RESONANCE_STONE.get().getDescription().copy().withStyle(ChatFormatting.AQUA)), 12))));

            whatWrong.addChild(next);
            youLookTerrible.addChild(next);
            root = new DialogNode(dBuilder.ans(35));

            root.addChild(whatWrong)
                    .addChild(youLookTerrible);

        } else if(TCRQuests.TALK_TO_CHRONOS_12.equals(currentQuest)) {
            root = new DialogNode(dBuilder.ans(42, ModItems.STORM_EYE.get().getDescription()));
            DialogNode chronos = new DialogNode(dBuilder.ans(43, WorldUtil.AETHER_NAME, TCREntities.CHRONOS_SOL.get().getDescription()), dBuilder.opt(14, TCREntities.CHRONOS_SOL.get().getDescription()));
            DialogNode known = new DialogNode(dBuilder.ans(43, WorldUtil.AETHER_NAME, TCREntities.CHRONOS_SOL.get().getDescription()), dBuilder.opt(15));

            DialogNode next = new DialogNode(dBuilder.ans(44), dBuilder.opt(-1))
                    .addChild(new DialogNode(dBuilder.ans(45), dBuilder.opt(-1))
                            .addChild(new DialogNode(dBuilder.ans(46), dBuilder.opt(-1))
                                    .addChild(new DialogNode(dBuilder.ans(47, WorldUtil.getStructureName(WorldUtil.STRONG_HOLD)), dBuilder.opt(-1))
                                            .addLeaf(dBuilder.opt(-2), 13))));

            chronos.addChild(next);
            known.addChild(next);
            root.addChild(chronos)
                    .addChild(known);

        } else if(TCRQuests.TALK_TO_CHRONOS_END.equals(currentQuest) || TCRQuests.KILL_MAD_CHRONOS.equals(currentQuest)) {
            //进维度打架
            root = new DialogNode(dBuilder.ans(48));
            DialogNode c1 = new DialogNode(dBuilder.ans(48), dBuilder.opt(14, TCREntities.CHRONOS_SOL.get().getDescription()));
            DialogNode c2 = new DialogNode(dBuilder.ans(48), dBuilder.opt(7, ModItems.VOID_EYE.get().getDescription()));
            DialogNode next = new DialogNode(dBuilder.ans(49), dBuilder.opt(16, TCREntities.CHRONOS_SOL.get().getDescription()))
                    .addExecutable(dialogueScreen -> ChronosSolGeoRenderer.useRedTexture = true)
                    .addExecutable(-1)
                    .addLeaf(dBuilder.opt(17, TCREntities.CHRONOS_SOL.get().getDescription()), 14);
            c1.addChild(next);
            c2.addChild(next);
            root.addChild(c1).addChild(c2);
        } else {
            //默认的情况

            if(PlayerDataManager.aineTalked.get(localPlayer)) {
                root.addChild(aboutAine);
            }
            if(PlayerDataManager.ferryGirlTalked.get(localPlayer)) {
                root.addChild(aboutFerryGirl);
            }
            if(PlayerDataManager.ornnTalked.get(localPlayer)) {
                root.addChild(aboutOrnn);
            }

            root.addChild(aboutThisWorld);
        }

        return treeBuilder.buildWith(root);

    }

    @Override
    public void handleNpcInteraction(ServerPlayer player, int code) {
        if(code == -1) {
            EntityUtil.playLocalSound(player, SoundEvents.WITCH_CELEBRATE);
        }
        //初次对话，准备启程
        if(code == 1) {
            TCRQuests.TALK_TO_CHRONOS_0.finish(player);
            TCRQuests.TALK_TO_FERRY_GIRL_0.start(player);
            TCRQuests.TALK_TO_ORNN_0.start(player);
            ItemUtil.addItemEntity(player, TCRItems.LAND_RESONANCE_STONE.get(), 1, ChatFormatting.YELLOW.getColor());
            PlayerDataManager.chronosTalked.put(player, true);
        }

        //拿回大地眼睛后的对话
        if(code == 2) {
            TCRQuests.TALK_TO_CHRONOS_1.finish(player);
            //还没摆放则提示摆放沙漠眼
            if(!PlayerDataManager.desertEyeActivated.get(player)) {
                TCRQuests.PUT_DESERT_EYE_ON_ALTAR.start(player);
            }
            //还没祈福则提示祈福
            if(!PlayerDataManager.desertEyeBlessed.get(player)) {
                TCRQuests.BLESS_ON_THE_GODNESS_STATUE.start(player);
            }

            TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(player);
            tcrPlayer.startWaitingResonanceStoneCharge(player);
        }

        //领海洋共鸣石
        if(code == 3) {
            TCRQuests.TALK_TO_CHRONOS_2.finish(player);
            ItemUtil.addItemEntity(player, TCRItems.OCEAN_RESONANCE_STONE.get(), 1, ChatFormatting.BLUE.getColor());
            TCRQuests.GO_TO_OVERWORLD_OCEAN.start(player);
        }

        //找安问问海船墓地回响
        if(code == 4) {
            TCRQuests.TALK_TO_CHRONOS_3.finish(player);
            TCRQuests.TALK_TO_AINE_ECHO.start(player);
        }

        //问到后回报
        if(code == 5) {
            TCRQuests.TALK_TO_CHRONOS_4.finish(player);
            ItemUtil.addItemEntity(player, TCRItems.CURSED_RESONANCE_STONE.get(), 1, ChatFormatting.DARK_GREEN.getColor());
            TCRQuests.GO_TO_OVERWORLD_CURSED.start(player);
        }

        //找回诅咒眼后
        if(code == 6) {
            TCRQuests.TALK_TO_CHRONOS_5.finish(player);
            TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(player);
            tcrPlayer.startWaitingResonanceStoneCharge(player);
        }

        //领炉心共鸣石
        if(code == 7) {
            TCRQuests.TALK_TO_CHRONOS_6.finish(player);
            ItemUtil.addItemEntity(player, TCRItems.CORE_RESONANCE_STONE.get(), 1, ChatFormatting.RED.getColor());
            TCRQuests.GO_TO_OVERWORLD_CORE.start(player);
        }

        //找回烈焰眼后
        if(code == 8) {
            TCRQuests.TALK_TO_CHRONOS_7.finish(player);
            TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(player);
            tcrPlayer.startWaitingResonanceStoneCharge(player);
        }

        //领地狱共鸣石
        if(code == 9) {
            TCRQuests.TALK_TO_CHRONOS_8.finish(player);
            ItemUtil.addItemEntity(player, TCRItems.CORE_FLINT.get(), 1, ChatFormatting.DARK_RED.getColor());
            ItemUtil.addItemEntity(player, TCRItems.NETHER_RESONANCE_STONE.get(), 1, ChatFormatting.DARK_RED.getColor());
            PlayerDataManager.canEnterNether.put(player, true);
            PlayerDataManager.fireAvoidUnlocked.put(player, true);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(player, member -> {
                member.getCapability(SkillTreeProgression.SKILL_TREE_PROGRESSION).ifPresent(skillTreeProgression -> {
                    ResourceKey<SkillTree> resourceKey = ResourceKey.create(SkillTree.SKILL_TREE_REGISTRY_KEY, ResourceLocation.fromNamespaceAndPath(DodgeParryRewardMod.MOD_ID, "passive"));
                    skillTreeProgression.unlockTree(resourceKey, member);
                    skillTreeProgression.unlockNode(resourceKey, TCRSkills.FIRE_AVOID, member);
                });
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_SKILL), member);
                member.displayClientMessage(TCRCoreMod.getInfo("unlock_new_skill", Component.translatable(TCRSkills.FIRE_AVOID.getTranslationKey()).withStyle(ChatFormatting.RED)), false);
                member.level().playSound(null, member.getX(), member.getY(), member.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            });
            TCRQuests.GO_TO_NETHER.start(player);
        }

        //开启凋零任务
        if(code == 10) {
            TCRQuests.TALK_TO_CHRONOS_9.finish(player);
            TCRQuests.GET_WITHER_EYE.start(player);
        }

        //凋零任务结束，开轮回绝境
        if(code == 11) {
            TCRQuests.TALK_TO_CHRONOS_10.finish(player);
            TCRQuests.TALK_TO_AINE_SAMSARA.start(player);
            TCRPlayer tcrPlayer = TCRCapabilityProvider.getTCRPlayer(player);
            tcrPlayer.startWaitingResonanceStoneCharge(player);
        }

        //出发天域
        if(code == 12) {
            TCRQuests.TALK_TO_CHRONOS_11.finish(player);
            ItemUtil.addItemEntity(player, TCRItems.SKY_RESONANCE_STONE.get(), 1, ChatFormatting.AQUA.getColor());

            PlayerDataManager.canEnterAether.put(player, true);
            PlayerDataManager.swordSoaringUnlocked.put(player, true);
            FTBTeamUtils.onlineTeamMembersDoWithSelf(player, member -> {
                member.getCapability(SkillTreeProgression.SKILL_TREE_PROGRESSION).ifPresent(skillTreeProgression -> {
                    ResourceKey<SkillTree> resourceKey = EpicSkillsSkillTrees.BATTLEBORN;
                    skillTreeProgression.unlockTree(resourceKey, member);
                    skillTreeProgression.unlockNode(resourceKey, FlyingSkills.SWORD_SOARING_ELYTRA_MASTER, member);
                    skillTreeProgression.unlockNode(resourceKey, FlyingSkills.SWORD_SOARING_MASTER, member);
                });
                PacketRelay.sendToPlayer(TCRPacketHandler.INSTANCE, new PlayTitlePacket(PlayTitlePacket.UNLOCK_NEW_SKILL), member);
                member.displayClientMessage(TCRCoreMod.getInfo("unlock_new_skill", Component.translatable(FlyingSkills.SWORD_SOARING_MASTER.getTranslationKey()).withStyle(ChatFormatting.AQUA)), false);
                member.level().playSound(null, member.getX(), member.getY(), member.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0F, 1.0F);
            });
            TCRQuests.GO_TO_AETHER.start(player);
        }

        //去末地
        if(code == 13) {
            TCRQuests.TALK_TO_CHRONOS_12.finish(player);
            TCRQuests.GO_TO_OVERWORLD_END.start(player);
            ItemUtil.addItemEntity(player, TCRItems.END_RESONANCE_STONE.get(), 1, ChatFormatting.LIGHT_PURPLE.getColor());
        }

        //打最终boss
        if(code == 14) {
            this.setConversingPlayer(null);//保险
            if(TCRQuestManager.hasQuest(player, TCRQuests.TALK_TO_CHRONOS_END)) {
                TCRQuests.TALK_TO_CHRONOS_END.finish(player, true);
            }
            if(!TCRQuestManager.hasQuest(player, TCRQuests.KILL_MAD_CHRONOS)) {
                TCRQuests.KILL_MAD_CHRONOS.start(player);
            }
            ServerLevel mimicDim = player.server.getLevel(WraithonDimensions.SANCTUM_OF_THE_WRAITHON_LEVEL_KEY);
            if(mimicDim != null) {
                player.changeDimension(mimicDim, new PositionTeleporter(WraithonMod.PLAYER_SPAWN_POS));
                if(mimicDim.getEntities(TCREntities.TCR_MIMIC.get(), (Entity::isAlive)).isEmpty()
//                        && mimicDim.getEntities(WraithonEntities.WRAITHON.get(), (Entity::isAlive)).isEmpty()
                ) {
                    TCRDimSaveData saveData = TCRDimSaveData.get(mimicDim);
                    if(!saveData.isBossSummoned()) {
                        LivingEntity boss = TCREntities.TCR_MIMIC.get().spawn(mimicDim, WraithonMod.PLAYER_SPAWN_POS.above(20), MobSpawnType.MOB_SUMMONED);
                        if(boss != null) {
                            saveData.setBossSummoned(true);
                        }
                    }
                }
            }

        }

        this.setConversingPlayer(null);
    }

    @Override
    public void setConversingPlayer(@Nullable Player player) {
        this.conversingPlayer = player;
    }

    @Override
    public @Nullable Player getConversingPlayer() {
        return conversingPlayer;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, this::deployAnimController));
    }

    protected <E extends ChronosSolEntity> PlayState deployAnimController(final AnimationState<E> state) {
        return state.setAndContinue(IDLE);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /**
     * 通关了就不可对话了
     */
    public boolean canInteract(Player player) {
        return !PlayerDataManager.gameCleared.get(player);
    }
    
}
