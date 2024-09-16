package com.purplerupter.bacchanalianmobs.dynamicdifficulty.actions;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.GlobalActionDifficultyChanger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class PointsPerSleep {

    private static final String SLEEP_COUNT_TAG = "sleepCount";
    private static final String CONFIG_FILE_NAME = "points_per_sleep.cfg";
    private static File configPath;

    private double startAmount;
    private String operation;
    private double operationAmount;

    private static final boolean shouldShowNextSleep = true;

    public PointsPerSleep(File configDir) {
        loadConfig(configDir);
    }

    private void loadConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }

        try {
            List<String> lines = Files.readAllLines(configPath.toPath());
            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                startAmount = Double.parseDouble(parts[0]);
                operation = parts[1];
                operationAmount = Double.parseDouble(parts[2]);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onPlayerWakeUp(PlayerWakeUpEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        World world = player.getEntityWorld();

        // Проверяем, если это мультиплеер и ночь была пропущена
        if (!world.isRemote && event.shouldSetSpawn() && !world.isDaytime()) {
            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound persistentData;

            if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                persistentData = new NBTTagCompound();
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
            } else {
                persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            }

            int sleepCount = persistentData.getInteger(SLEEP_COUNT_TAG) + 1;
            persistentData.setInteger(SLEEP_COUNT_TAG, sleepCount);

            double points = calculatePoints(sleepCount);
            GlobalActionDifficultyChanger.changePlayerPointsByGlobal((EntityPlayerMP) player, points, sleepCount, calculatePoints(sleepCount + 1), shouldShowNextSleep);
        }
    }

    private double calculatePoints(int sleepCount) {
        double points = startAmount;

        for (int i = 1; i < sleepCount; i++) {
            if ("ADD".equals(operation)) {
                points += operationAmount;
            } else if ("MULT".equals(operation)) {
                points *= operationAmount;
            }
        }

        return points;
    }
}
