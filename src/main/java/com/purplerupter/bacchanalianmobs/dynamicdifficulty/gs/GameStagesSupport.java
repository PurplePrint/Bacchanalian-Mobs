package com.purplerupter.bacchanalianmobs.dynamicdifficulty.gs;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.GlobalActionDifficultyChanger;
import net.darkhax.gamestages.event.GameStageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class GameStagesSupport {

    private final Map<String, Double> stagePoints = new HashMap<>();
    private final Map<String, Boolean> stageReduction = new HashMap<>();

    public GameStagesSupport(GameStageConfigHandler configHandler) {
        loadStageConfigurations(configHandler);
    }

    private void loadStageConfigurations(GameStageConfigHandler configHandler) {
        for (String[] config : configHandler.getConfigurations()) {
            String stage = config[0];
            double points = Double.parseDouble(config[1]);
            boolean reduceOnLost = Boolean.parseBoolean(config[2]);
            stagePoints.put(stage, points);
            stageReduction.put(stage, reduceOnLost);
        }
    }

    @SubscribeEvent
    public void onStageAdded(GameStageEvent.Added event) {
        String stage = event.getStageName();
        EntityPlayer player = event.getEntityPlayer();

        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        if (persistentData.getBoolean("HasStage_" + stage)) {
            return;
        }

        if (stagePoints.containsKey(stage)) {
            player.sendMessage(new TextComponentString("Points for stage " + stage + " is: " + (stagePoints.get(stage))));
            addPoints(player, stagePoints.get(stage), stage);
            persistentData.setBoolean("HasStage_" + stage, true);
        }
    }

    @SubscribeEvent
    public void onStageRemoved(GameStageEvent.Removed event) {
        String stage = event.getStageName();
        EntityPlayer player = event.getEntityPlayer();

        if (stageReduction.containsKey(stage) && stageReduction.get(stage)) {
            reducePoints(player, -(stagePoints.get(stage)), stage);

            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            persistentData.removeTag("HasStage_" + stage);
        }
    }

    private void addPoints(EntityPlayer player, double amount, String stageName) {
        GlobalActionDifficultyChanger.changePlayerPointsByGlobal((EntityPlayerMP) player, amount, stageName, false);
    }

    private void reducePoints(EntityPlayer player, double amount, String stageName) {
        GlobalActionDifficultyChanger.changePlayerPointsByGlobal((EntityPlayerMP) player, amount, stageName, true);
    }
}
