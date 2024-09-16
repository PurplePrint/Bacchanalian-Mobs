package com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud;

import com.purplerupter.bacchanalianmobs.BacchanalianMobs;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DifficultyConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderHUD extends Gui {
    private static final ResourceLocation TEXTURE = new ResourceLocation(BacchanalianMobs.MODID, "textures/frame.png");
    private static final ResourceLocation FILLER_TEXTURE = new ResourceLocation(BacchanalianMobs.MODID, "textures/filler.png");

    public static final RenderHUD INSTANCE = new RenderHUD();
    private double difficultyPoints = 0.0f;
    private double previousPoints = 0.0f;

    private static boolean shouldRenderActionPoints;
    private static float actionPointsRecentSum = 0.0f;
    private static long lastTimeGettingAction;
    private static short actionPointsTimeout;

    private static final String normalPointsFormat = "%.2f";
    private static final String actionPointsFormat = "%.3f";

    private static int difficultyTextColor;
    private static int differenceTextColorIncrease;
    private static int differenceTextColorLowering;
    private static int differenceTextColorZero;
    private static int actionTextColor;

    public void updateConfig() {
        difficultyTextColor = DifficultyConfig.difficultyTextColor;
        differenceTextColorIncrease = DifficultyConfig.differenceTextColorIncrease;
        differenceTextColorLowering = DifficultyConfig.differenceTextColorLowering;
        differenceTextColorZero = DifficultyConfig.differenceTextColorZero;
        actionTextColor = DifficultyConfig.actionTextColor;

        actionPointsTimeout = DifficultyConfig.actionPointsTimeout;
    }

    public void setDifficultyPoints(double points, boolean passive, float actionAmount) {
        this.previousPoints = passive ? this.difficultyPoints : (points - (this.difficultyPoints - this.previousPoints));
        this.difficultyPoints = points;
        if (actionAmount != 0) {
            renderActionPoints(actionAmount);
        }
    }

    public static void renderActionPoints(float amount) {
        shouldRenderActionPoints = true;
        actionPointsRecentSum += amount;
        lastTimeGettingAction = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
            return;
        }

        if (!HUDConfig.visibility) {
            return;
        }

        updateConfig();

        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.enableBlend();
        mc.renderEngine.bindTexture(TEXTURE);

        GlStateManager.pushMatrix();


        float floatPosX = HUDConfig.customPosX / 100 * event.getResolution().getScaledWidth();
        float floatPosY = HUDConfig.customPosY / 100 * event.getResolution().getScaledHeight();
        // Все равно, даже с процентами, при смене масштаба GUI игры оно немного сдвигается.
        short posX = (short) floatPosX;
        short posY = (short) floatPosY;

//        int boxWidth = 80;
//        int boxHeight = 16;
        short boxWidth = 90;
        short boxHeight = 18;

        short fillerPadding = 1;

        short textOffset = (short) (8 + fillerPadding);
        short boxHeightText = (short) (boxHeight + textOffset);

//        drawTexturedModalRect(posX, posY - textOffset, 190, 0, boxWidth, boxHeightText);
        drawModalRectWithCustomSizedTexture(posX, posY - textOffset, 190, 0, boxWidth, boxHeightText, boxWidth, boxHeightText);
        // Its weird, but the drawTexturedModalRect above makes the texture image corrupted. I don't know why.

        short fillerMaxWidth = (short) (boxWidth - (fillerPadding * 2));
        short fillerWidth = (short) (fillerMaxWidth * (difficultyPoints / 1000.0));
        short fillerHeight = (short) (boxHeight - (fillerPadding * 2));

        mc.getTextureManager().bindTexture(FILLER_TEXTURE);
        drawModalRectWithCustomSizedTexture(posX + fillerPadding, posY + fillerPadding, 0, 0, fillerWidth, fillerHeight, fillerMaxWidth, fillerHeight);

        GlStateManager.pushMatrix();
        float textScale = 1f;
        GlStateManager.scale(textScale, textScale, 1.0f);


        String textDifficulty = String.format(normalPointsFormat, difficultyPoints);
        int difficultyColor =difficultyTextColor;
        mc.fontRenderer.drawString(textDifficulty, posX + fillerPadding, posY - 8, difficultyColor);


        int differenceColor = 0xAAAA10;
        if (previousPoints != 0.0f) {
            if (difficultyPoints - previousPoints > 0.0) {
                differenceColor = differenceTextColorIncrease;
            } else {
                if (difficultyPoints - previousPoints < 0.0) {
                    differenceColor = differenceTextColorLowering;
                } else {
                    if (difficultyPoints - previousPoints == 0.0) {
                        differenceColor = differenceTextColorZero;
                    }
                }
            }
        }

        String textDifference = String.format(normalPointsFormat, (previousPoints != difficultyPoints && previousPoints != 0.0f) ? difficultyPoints - previousPoints : 0.0f );
        short textDifferenceWidth = (short) (mc.fontRenderer.getStringWidth(textDifference));
        mc.fontRenderer.drawString(textDifference, posX + boxWidth - textDifferenceWidth - (fillerPadding * 2), posY - 8, differenceColor);
        // textDifference need *2 for correct position, otherwise it will shift too much to the right


        int actionColor = actionTextColor;
        String textAction = String.format(actionPointsFormat, actionPointsRecentSum);

        if (shouldRenderActionPoints) {
//            mc.fontRenderer.drawString(textAction, posX + boxWidth + 4, posY + (boxHeight / 2), actionColor);
//            mc.fontRenderer.drawString(textAction, posX + (boxWidth / 2) - (textDifferenceWidth / 2), posY - 8, actionColor);
            mc.fontRenderer.drawString(textAction, posX + (boxWidth / 2) - (short)(textDifferenceWidth / 2 * 1.3), posY - 8, actionColor);
            // I don't know why, but textDifferenceWidth is not exact, and text is not at the center of the HUD
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTimeGettingAction > actionPointsTimeout * 1000) {
                shouldRenderActionPoints = false;
                actionPointsRecentSum = 0f;
            }
        }

        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
    }
}
