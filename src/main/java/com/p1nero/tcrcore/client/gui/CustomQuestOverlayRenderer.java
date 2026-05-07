package com.p1nero.tcrcore.client.gui;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Axis;
import com.p1nero.tcrcore.TCRClientConfig;
import com.p1nero.tcrcore.TCRCoreMod;
import com.p1nero.tcrcore.capability.TCRQuestManager;
import com.p1nero.tcrcore.client.TCRKeyMappings;
import com.p1nero.tcrcore.client.gui.screen.TCRQuestScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class CustomQuestOverlayRenderer implements IGuiOverlay {
    private static long fadeStartTime = 0;
    private static boolean hasQuest = false;
    private static boolean lastHasQuest = false;
    private static float alpha = 0.0f;
    private static final int FADE_DURATION = 30; // 30 ticks = 1.5 seconds
    private static Component lastQuestShortDesc = Component.empty();
    private static Component lastQuestTitle = Component.empty();
    private static Component hintText = null;
    private static int x;
    private static int y;
    private static int textX;
    private static int textY;
    private static long timeSinceStateChange;
    public static final ResourceLocation QUEST_ICON = ResourceLocation.fromNamespaceAndPath(TCRCoreMod.MOD_ID, "textures/gui/quest_icon.png");
    public static final ResourceLocation QUEST_ARROW_ICON = ResourceLocation.fromNamespaceAndPath(TCRCoreMod.MOD_ID, "textures/gui/quest_arrow.png");
    public static double QUEST_INDICATOR_MIN_DISTANCE = 5.0D;
    private static final int QUEST_ICON_SIZE = 8;
    private static final int QUEST_EDGE_MARGIN = 60;
    private static TCRQuestManager.Quest currentQuest;
    private static ResourceLocation currentQuestIcon;
    private static final List<QuestIndicatorInfo> QUEST_INDICATOR_INFOS = new ArrayList<>();

    private static class QuestIndicatorInfo {
        TCRQuestManager.Quest quest;
        ResourceLocation icon;
        float iconX;
        float iconY;
        float lastIconX;
        float lastIconY;
        boolean drawArrow;
        float arrowCenterX;
        float arrowCenterY;
        float lastArrowCenterX;
        float lastArrowCenterY;
        float arrowAngle;
        String distanceText;
        boolean isOnScreen;
    }

    private static class ScreenPos {
        boolean inside;
        double x;
        double y;
        double nx;
        double ny;
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        if(localPlayer == null || minecraft.level == null) {
            lastHasQuest = false;
            hasQuest = false;
            return;
        }
        Window window = minecraft.getWindow();
        long currentTime = localPlayer.level().getGameTime();
        // Calculate alpha based on game time with partialTick interpolation
        timeSinceStateChange = currentTime - fadeStartTime;
        hasQuest = TCRQuestManager.hasQuest(localPlayer);
        if(hintText == null) {
            hintText = TCRCoreMod.getInfo("press_to_show_quest_ui", TCRKeyMappings.SHOW_QUESTS.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GOLD));
        }
        // Handle state changes
        if (hasQuest != lastHasQuest) {
            fadeStartTime = currentTime;
        }
        if (hasQuest) {
            currentQuest = TCRQuestManager.getCurrentQuest(localPlayer);
            lastQuestShortDesc = currentQuest.getShortDesc();
            lastQuestTitle = currentQuest.getTitle();
            currentQuestIcon = currentQuest.getIcon();
        }

        lastHasQuest = hasQuest;

        // Calculate position (golden ratio - left side, about 38.2% from top)
        int screenHeight = window.getGuiScaledHeight();
        int goldenRatioY = (int) (screenHeight * 0.382f);
        x = 10 + TCRClientConfig.TASK_UI_X.get(); // 10 pixels from left edge
        y = goldenRatioY + TCRClientConfig.TASK_UI_Y.get();
        // Draw text with shadow
        textX = x + 20; // 4 pixels spacing after icon
        textY = y + 4; // Vertically center with 16px icon

        updateQuestIndicators(localPlayer, window);
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        float interpolatedTime = timeSinceStateChange + partialTick;

        if (hasQuest) {
            // Fade in
            if (interpolatedTime < FADE_DURATION) {
                alpha = interpolatedTime / FADE_DURATION;
            } else {
                alpha = 1.0f;
            }
        } else {
            // Fade out
            if (interpolatedTime < FADE_DURATION) {
                alpha = 1.0f - (interpolatedTime / FADE_DURATION);
            } else {
                alpha = 0.0f;
            }
        }

        // Only render if there's something to show and alpha > 0
        if (alpha <= 0.0f || lastQuestShortDesc == null || lastQuestShortDesc.getString().isEmpty()) {
            return;
        }
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(0.93F, 0.93F, 0.93F);
        // Set up alpha for rendering
        RenderSystem.enableBlend();

        // Draw icon (16x16)
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, alpha);

        ResourceLocation icon = currentQuestIcon == null ? QUEST_ICON : currentQuestIcon;
        guiGraphics.blit(icon, x, y, 0, 0, 16, 16, 16, 16);

        // Draw text with shadow and alpha
        int titleColor = (int) (alpha * 255) << 24 | 0xFFD700;
        int textColor = (int) (alpha * 255) << 24 | 0xFFFFFF;

        int h = minecraft.font.lineHeight + 2;
        // Draw main text
        guiGraphics.drawString(minecraft.font, lastQuestTitle, textX, textY - h / 2, titleColor, true);
        guiGraphics.drawString(minecraft.font, lastQuestShortDesc, textX, textY +  h / 2, textColor, true);

        // Draw hint text below in smaller size
        int hintTextColor = (int) (alpha * 255) << 24 | 0xAAAAAA;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate((float) x + 2, textY + 14, 0);
        guiGraphics.pose().scale(0.7F, 0.7F, 0.7F);
        if(hintText != null) {
            guiGraphics.drawString(minecraft.font, hintText, 0, 0, hintTextColor, true);
        }
        guiGraphics.pose().popPose();

        // Reset color
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();

        renderQuestTargetIndicator(minecraft.player, guiGraphics, partialTick);

    }

    /**
     * 画任务的目标指示器
     */
    public static void renderQuestTargetIndicator(LocalPlayer localPlayer, GuiGraphics guiGraphics, float partialTick) {
        if (localPlayer == null || QUEST_INDICATOR_INFOS.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        Camera camera = minecraft.gameRenderer.getMainCamera();

        for (QuestIndicatorInfo info : QUEST_INDICATOR_INFOS) {
            float iconX, iconY;
            String distText;
            boolean onScreen = info.isOnScreen;

            if (onScreen) {
                ScreenPos screenPos = calculateScreenPos(info.quest.getTrackingPos(), camera, window);
                if (screenPos != null) {
                    iconX = (float) (screenPos.x - QUEST_ICON_SIZE / 2.0);
                    iconY = (float) (screenPos.y - QUEST_ICON_SIZE / 2.0);
                } else {
                    iconX = Mth.lerp(partialTick, info.lastIconX, info.iconX);
                    iconY = Mth.lerp(partialTick, info.lastIconY, info.iconY);
                }
                double dist = localPlayer.position().distanceTo(Vec3.atCenterOf(info.quest.getTrackingPos()));
                distText = ((int) dist) + "m";
            } else {
                iconX = Mth.lerp(partialTick, info.lastIconX, info.iconX);
                iconY = Mth.lerp(partialTick, info.lastIconY, info.iconY);
                distText = info.distanceText;
            }

            RenderSystem.enableBlend();
            //非追踪的则画透明点
            guiGraphics.setColor(1.0f, 1.0f, 1.0f, info.quest.equals(currentQuest) ? 1.0F : 0.5F);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(iconX, iconY, 0.0F);
            guiGraphics.blit(info.icon, 0, 0, 0, 0, QUEST_ICON_SIZE, QUEST_ICON_SIZE, QUEST_ICON_SIZE, QUEST_ICON_SIZE);
            guiGraphics.pose().popPose();

            int textWidth = minecraft.font.width(distText);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(iconX + QUEST_ICON_SIZE / 2.0F, iconY + QUEST_ICON_SIZE + 4.0F, 0.0F);
            guiGraphics.pose().scale(0.6F, 0.6F, 0.6F);
            guiGraphics.drawString(minecraft.font, distText, -textWidth / 2, 0, 0xFFFFFFFF, true);
            guiGraphics.pose().popPose();

            if (!onScreen && info.drawArrow) {
                float arrowCenterX = Mth.lerp(partialTick, info.lastArrowCenterX, info.arrowCenterX);
                float arrowCenterY = Mth.lerp(partialTick, info.lastArrowCenterY, info.arrowCenterY);

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(arrowCenterX, arrowCenterY, 0.0F);
                guiGraphics.pose().mulPose(Axis.ZP.rotationDegrees(info.arrowAngle));
                guiGraphics.blit(QUEST_ARROW_ICON, -QUEST_ICON_SIZE / 2, -QUEST_ICON_SIZE / 2 - QUEST_ICON_SIZE, 0, 0, QUEST_ICON_SIZE, QUEST_ICON_SIZE, QUEST_ICON_SIZE, QUEST_ICON_SIZE);
                guiGraphics.pose().popPose();
            }

            guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }

    private static void updateQuestIndicators(LocalPlayer localPlayer, Window window) {
        if (localPlayer == null) {
            QUEST_INDICATOR_INFOS.clear();
            return;
        }

        List<QuestIndicatorInfo> previous = new ArrayList<>(QUEST_INDICATOR_INFOS);
        QUEST_INDICATOR_INFOS.clear();

        List<TCRQuestManager.Quest> quests = TCRQuestScreen.getQuests();
        TCRQuestManager.Quest selectedQuest = TCRQuestScreen.getSelectedQuest();
        if (quests == null || selectedQuest == null) {
            return;
        }

        BlockPos selectedQuestPos = selectedQuest.getTrackingPos();
        if (selectedQuestPos != null && localPlayer.level().dimension() == selectedQuest.getDimension()) {
            ResourceLocation icon = selectedQuest.getIcon();
            if (icon == null) {
                icon = QUEST_ICON;
            }
            QuestIndicatorInfo info = computeQuestIndicator(localPlayer, selectedQuestPos, icon, window, true);
            if (info != null) {
                applyPreviousIndicatorInfo(info, selectedQuest, previous);
                QUEST_INDICATOR_INFOS.add(info);
            }
        }

        quests.forEach(quest -> {
            if (quest.equals(selectedQuest)) {
                return;
            }
            BlockPos trackingPos = quest.getTrackingPos();
            if (trackingPos != null && localPlayer.level().dimension() == quest.getDimension()) {
                ResourceLocation icon = quest.getIcon();
                if (icon == null) {
                    icon = QUEST_ICON;
                }
                QuestIndicatorInfo info = computeQuestIndicator(localPlayer, trackingPos, icon, window, false);
                if (info != null) {
                    applyPreviousIndicatorInfo(info, quest, previous);
                    QUEST_INDICATOR_INFOS.add(info);
                }
            }
        });
    }

    private static void applyPreviousIndicatorInfo(QuestIndicatorInfo info, TCRQuestManager.Quest quest, List<QuestIndicatorInfo> previous) {
        info.quest = quest;
        QuestIndicatorInfo old = null;
        for (QuestIndicatorInfo prev : previous) {
            if (prev.quest != null && prev.quest.equals(quest)) {
                old = prev;
                break;
            }
        }
        if (old != null) {
            info.lastIconX = old.iconX;
            info.lastIconY = old.iconY;
            info.lastArrowCenterX = old.arrowCenterX;
            info.lastArrowCenterY = old.arrowCenterY;
        } else {
            info.lastIconX = info.iconX;
            info.lastIconY = info.iconY;
            info.lastArrowCenterX = info.arrowCenterX;
            info.lastArrowCenterY = info.arrowCenterY;
        }
    }

    private static ScreenPos calculateScreenPos(BlockPos pos, Camera camera, Window window) {
        Vec3 camPos = camera.getPosition();
        Vec3 targetPos = Vec3.atCenterOf(pos);
        Vec3 camToTarget = targetPos.subtract(camPos);

        if (camToTarget.lengthSqr() < 1.0e-4) {
            return null;
        }

        Vec3 forward = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        Vec3 upWorld = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = forward.cross(upWorld).normalize();
        Vec3 up = right.cross(forward).normalize();

        double xCam = camToTarget.dot(right);
        double yCam = camToTarget.dot(up);
        double zCam = camToTarget.dot(forward);

        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        float fovDegrees = Minecraft.getInstance().options.fov().get();
        double fovRad = Math.toRadians(fovDegrees);
        double tanHalfFovY = Math.tan(fovRad / 2.0);
        double aspect = (double) screenWidth / (double) screenHeight;
        double tanHalfFovX = tanHalfFovY * aspect;

        double nx = xCam / (zCam * tanHalfFovX);
        double ny = yCam / (zCam * tanHalfFovY);

        boolean inside = zCam > 0.1 && nx >= -1.0 && nx <= 1.0 && ny >= -1.0 && ny <= 1.0;

        ScreenPos result = new ScreenPos();
        result.inside = inside;
        result.nx = nx;
        result.ny = ny;

        if (inside) {
            result.x = (nx * 0.5 + 0.5) * screenWidth;
            result.y = (-ny * 0.5 + 0.5) * screenHeight;
        }

        return result;
    }

    private static QuestIndicatorInfo computeQuestIndicator(LocalPlayer localPlayer, BlockPos pos, ResourceLocation icon, Window window, boolean drawArrowWhenOffscreen) {
        if (localPlayer == null) {
            return null;
        }
        ScreenPos screenPos = calculateScreenPos(pos, Minecraft.getInstance().gameRenderer.getMainCamera(), window);
        if (screenPos == null) {
            return null;
        }

        if (!screenPos.inside && !drawArrowWhenOffscreen) {
            return null;
        }

        double distance = localPlayer.position().distanceTo(Vec3.atCenterOf(pos));
        QuestIndicatorInfo info = new QuestIndicatorInfo();
        info.icon = icon;
        info.distanceText = ((int) distance) + "m";
        info.isOnScreen = screenPos.inside;

        if (screenPos.inside) {
            info.iconX = (float) (screenPos.x - QUEST_ICON_SIZE / 2.0);
            info.iconY = (float) (screenPos.y - QUEST_ICON_SIZE / 2.0);
            info.drawArrow = false;
            info.arrowCenterX = 0;
            info.arrowCenterY = 0;
            info.arrowAngle = 0.0F;
            return info;
        }

        double nx = screenPos.nx;
        double ny = screenPos.ny;
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        double dx = nx;
        double dy = -ny;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1.0e-3) {
            dx = 0.0;
            dy = -1.0;
            len = 1.0;
        } else {
            dx /= len;
            dy /= len;
        }

        double a = (screenWidth  / 2.0) - QUEST_EDGE_MARGIN;
        double b = (screenHeight / 2.0) - QUEST_EDGE_MARGIN;
        double denom = (dx * dx) / (a * a) + (dy * dy) / (b * b);
        double t = denom > 1.0e-6 ? 1.0 / Math.sqrt(denom) : 0.0;

        int edgeX = (int) Math.round(centerX + dx * t);
        int edgeY = (int) Math.round(centerY + dy * t);

        int iconX = edgeX - QUEST_ICON_SIZE / 2;
        int iconY = edgeY - QUEST_ICON_SIZE / 2;

        info.iconX = iconX;
        info.iconY = iconY;
        info.drawArrow = true;
        info.arrowCenterX = edgeX;
        info.arrowCenterY = edgeY;

        double dirX = edgeX - centerX;
        double dirY = edgeY - centerY;
        info.arrowAngle = (float) (Math.atan2(dirX, -dirY) * (180.0 / Math.PI));

        return info;
    }

    // Helper method to reset state when needed (e.g., when GUI is closed)
    public static void reset() {
        fadeStartTime = 0;
        alpha = 0.0f;
        hasQuest = false;
        lastHasQuest = false;
        lastQuestShortDesc = Component.empty();
        lastQuestTitle = Component.empty();
    }
}