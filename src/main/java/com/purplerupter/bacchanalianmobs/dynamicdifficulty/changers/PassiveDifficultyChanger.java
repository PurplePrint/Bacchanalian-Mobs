package com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.*;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.effects.Effects;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items.PointsPerItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.DEFAULT_CONFIG_PREFIX;
import static com.purplerupter.bacchanalianmobs.utils.DefaultConfigs.createDefaultConfig;

public class PassiveDifficultyChanger {
    private static Config config;
    private static File configPath;
    private static final String CONFIG_FILE_NAME = "Dynamic_Difficulty_PointsCost.json";
    private final Effects effects;
    private static final Logger LOGGER = Logger.getLogger(PassiveDifficultyChanger.class.getName());

    private int tickCounter = 0;
    public static final int difficultyUpdateInterval = 600;

    private static double pointsPerHealth;
    private static double pointsPerDPS;
    private static double pointsPerArmorDefense;
    private static double pointsPerArmorToughness;
    private static double pointsPerItems;
    private static double pointsForEffectsLevel;
    private static double pointsForEffectsDuration;
    private static Map<String, Double> itemPointsMap;

    private static long timeStampInTicks = 0L;

    private static final Map<String, Double> PassivePointsComposition = new HashMap<>();

    public static final double maximumPointsValue = 1000.0;

    public PassiveDifficultyChanger(Effects effects, File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig(configPath);
        MinecraftForge.EVENT_BUS.register(this);
        this.effects = effects;
    }

    public static void changeTimeInTicks(long timeStamp) {
        timeStampInTicks = timeStamp;
    }

    public static void addPointsForEffects(double forLevel, double forDuration) {
        pointsForEffectsLevel = forLevel;
        pointsForEffectsDuration = forDuration;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // 30 seconds = 600 ticks
            if (tickCounter >= difficultyUpdateInterval) {
                startPassiveDifficultyCalculationTask();
                tickCounter = 0;
            }
        }
    }

    private void startPassiveDifficultyCalculationTask() {
        // pointless?
        if (FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getCurrentPlayerCount() == 0) {
            return;
        }
        for (EntityPlayerMP player : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
            effects.calculateDataFromNBT(player, player.world.getTotalWorldTime());
            timeStampInTicks = 0L;
            effects.clearNBT(player);

            itemPointsMap = PointsPerItems.calculatePointsForInventory(player);
            player.sendMessage(new TextComponentString("==== ==== ==== ==== ==== ===="));
            player.sendMessage(new TextComponentString("itemPoints map is: "));
            player.sendMessage(new TextComponentString(itemPointsMap.toString()));
            player.sendMessage(new TextComponentString("==== ==== ==== ==== ==== ===="));
            calculatePointsPerItems(itemPointsMap);

            calculateAverageHealth(player);
            calculateStatsArmor(player);
            calculateMaximumDPS(player);

            writeDifficultyToPlayersNBT(player);
            DynamicDifficulty.sendDifficultyPointsToClient(player);
        }
    }

    private void calculatePointsPerItems(Map<String, Double> itemsMap) {
        pointsPerItems = 0.0;
        for (Double amount : itemsMap.values()) {
            pointsPerItems += amount;
        }
    }

    private void calculateAverageHealth(EntityPlayer player) {
        List<Float> hpList = HPTracker.getListOfHP(player);
        if (hpList.isEmpty()) {
            float averageHP = player.getHealth();
            pointsPerHealth = calculateDifficultyPoints(averageHP, config.PointsPerHealth);
            player.sendMessage(new TextComponentString("HP has not changed in the last 30 seconds. Current HP: " + averageHP + " and points cost is: " + pointsPerHealth));
            return;
        }
        float sum = 0;
        for (float value : hpList) {
            sum += value;
        }
        float averageHP = sum / hpList.size();
        pointsPerHealth = calculateDifficultyPoints(averageHP, config.PointsPerHealth);
        player.sendMessage(new TextComponentString("Average HP in the last 30 seconds: " + averageHP + " and points cost is: " + pointsPerHealth));
        HPTracker.clearListOfHP(player);
    }

    private void calculateStatsArmor(EntityPlayer player) {
        StatsArmorHandler.updateArmorStats(player);
        int defenseLevel = ArmorStats.getDefenseLevel(player.getName());
        int toughnessLevel = ArmorStats.getToughnessLevel(player.getName());
        pointsPerArmorDefense = calculateDifficultyPoints(defenseLevel, config.PointsPerArmorDefense);
        pointsPerArmorToughness = calculateDifficultyPoints(toughnessLevel, config.PointsPerArmorToughness);
        player.sendMessage(new TextComponentString("Armor is " + defenseLevel + " // " + toughnessLevel + " and points cost is: " + pointsPerArmorDefense + " |/AND/| " + pointsPerArmorToughness));
    }

    private void calculateMaximumDPS(EntityPlayer player) {
        double maximumDPS = MaximumDPS.getMaximumDPSValue(player);
        String formattedMaximumDPS = DynamicDifficulty.df2.format(maximumDPS);
        pointsPerDPS = calculateDifficultyPoints(maximumDPS, config.PointsPerDPS);
        player.sendMessage(new TextComponentString("Strongest weapon in inventory has " + formattedMaximumDPS + " DPS and points cost is: " + pointsPerDPS));
    }

    private void writeDifficultyToPlayersNBT(EntityPlayerMP player) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData;

        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
        } else {
            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        player.sendMessage(new TextComponentString("Points for effects/LEVEL: " + pointsForEffectsLevel));
        player.sendMessage(new TextComponentString("Points for effects/DURATION: " + pointsForEffectsDuration));
        double totalPoints = pointsForEffectsLevel + pointsForEffectsDuration +
                pointsPerItems +
                pointsPerHealth + pointsPerDPS + pointsPerArmorDefense + pointsPerArmorToughness;

        NBTTagCompound passiveBonuses = persistentData.getCompoundTag("PassiveBonuses");
        for (String key : passiveBonuses.getKeySet()) {
            totalPoints += passiveBonuses.getDouble(key);
        }

        double currentDifficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
        persistentData.setDouble("PlayerDifficultyPoints", Math.min(currentDifficultyPoints + totalPoints, maximumPointsValue));

        pointsForEffectsLevel = 0; pointsForEffectsDuration = 0;
        pointsPerItems = 0;
        pointsPerHealth = 0; pointsPerDPS = 0; pointsPerArmorDefense = 0; pointsPerArmorToughness = 0;
    }

    private double calculateDifficultyPoints(double currentValue, Config.PointsConfig config) {
        if (currentValue < config.MinimumAmount) {
            return 0;
        }
        if (currentValue > config.MaximumAmount) {
            currentValue = config.MaximumAmount;
        }
        return currentValue / config.TargetAmount * config.Cost;
    }

    private void loadConfig(File configPath) {
        Gson gson = new Gson();
        if (!configPath.exists()) {
            createDefaultConfig(DEFAULT_CONFIG_PREFIX + CONFIG_FILE_NAME, configPath);
        }
        try (FileReader reader = new FileReader(configPath)) {
            Type configType = new TypeToken<Config>() {}.getType();
            config = gson.fromJson(reader, configType);
            LOGGER.info("Config loaded successfully.");
        } catch (IOException e) {
            LOGGER.severe("Failed to load config: " + e.getMessage());
        }
    }

    public void addPassiveBonusPointsToPlayer(EntityPlayer player, String identifier, double value) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData;

        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
        } else {
            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        NBTTagCompound passiveBonuses;
        if (persistentData.hasKey("PassiveBonuses")) {
            passiveBonuses = persistentData.getCompoundTag("PassiveBonuses");
        } else {
            passiveBonuses = new NBTTagCompound();
            persistentData.setTag("PassiveBonuses", passiveBonuses);
        }

        passiveBonuses.setDouble(identifier, value);
        player.sendMessage(new TextComponentString("Added passive bonus " + value + " with identifier " + identifier));
    }

    public void removePassiveBonusPointsFromPlayer(EntityPlayer player, String identifier) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData;

        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            return;
        } else {
            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        if (persistentData.hasKey("PassiveBonuses")) {
            NBTTagCompound passiveBonuses = persistentData.getCompoundTag("PassiveBonuses");
            passiveBonuses.removeTag(identifier);
            player.sendMessage(new TextComponentString("Removed passive bonus with identifier " + identifier));
        }
    }

    private static class Config {
        PointsConfig PointsPerHealth;
        PointsConfig PointsPerDPS;
        PointsConfig PointsPerArmorDefense;
        PointsConfig PointsPerArmorToughness;

        private static class PointsConfig {
            double MinimumAmount;
            double MaximumAmount;
            double Cost;
            double TargetAmount;
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        NBTTagCompound playerData = player.getEntityData();
        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            NBTTagCompound persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
            persistentData.setDouble("PlayerDifficultyPoints", 0.0f);
        }
    }
}
