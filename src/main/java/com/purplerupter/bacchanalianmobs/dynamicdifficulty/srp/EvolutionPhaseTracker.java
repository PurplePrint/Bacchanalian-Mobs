package com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.GlobalActionDifficultyChanger.changePlayerPointsByGlobal;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;
import static com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker.TIME_SPENT_TAG;
import static com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker.updateTimeSpent;

public class EvolutionPhaseTracker {

    private final Map<Short, Byte> dimensionPhases = new HashMap<>();
    private static final String CONFIG_FILE_NAME = "points_per_phases.cfg";
    private static File configPath;
    // TODO: minimumTime from config!
    private static final int minimumTime = 300; // seconds

    public EvolutionPhaseTracker(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        World world = event.world;
        short dimensionId = (short) world.provider.getDimension();

        byte curPhase = SRPSaveData.get(world).getEvolutionPhase(dimensionId);

        if (dimensionPhases.containsKey(dimensionId)) {
            byte previousPhase = dimensionPhases.get(dimensionId);
            if (curPhase != previousPhase) {
                onPhaseChange(world, dimensionId, previousPhase, curPhase);
            }
        }

        dimensionPhases.put(dimensionId, curPhase);
    }

    private void onPhaseChange(World world, short dimensionId, byte oldPhase, byte newPhase) {

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            updateTimeSpent(player);

            int timeInDimension = 0;

            NBTTagCompound playerData = player.getEntityData();
            NBTTagCompound persistentData;

            if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                persistentData = new NBTTagCompound();
                playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
            } else {
                persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            }

            if (persistentData.hasKey(TIME_SPENT_TAG)) {
                NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);
                for (String key : timeSpentTag.getKeySet()) {
                    short dimId = Short.parseShort(key);
                    timeInDimension = timeSpentTag.getInteger(key);
                    if (dimId == dimensionId) break;
                }

                if (timeInDimension >= minimumTime) {
                    double points = getPointsFromConfig(dimensionId, newPhase, oldPhase);
                    changePlayerPointsByGlobal(player, points, newPhase, oldPhase);
                } else {
                    player.sendMessage(new TextComponentString("You are novice in " + dimensionId + " dimension, and you do not get points for phase " + newPhase));
                }
            } else {
                player.sendMessage(new TextComponentString("Where are your tag???"));
            }
        }
    }

    private double getPointsFromConfig(short dimensionId, byte newPhase, byte oldPhase) {
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split(";");
                if (parts.length == 4) {
                    short configDimensionId = Short.parseShort(parts[0]);
                    byte configPhaseNumber = Byte.parseByte(parts[1]);
                    double pointsAmount = Double.parseDouble(parts[2]);
                    double pointsAmountDecrease = Double.parseDouble(parts[3]);

                    if (configDimensionId == dimensionId && configPhaseNumber == newPhase) {
                        return newPhase > oldPhase ? pointsAmount : pointsAmountDecrease;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
