package com.purplerupter.bacchanalianmobs.dynamicdifficulty;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;

public class DifficultyConfig {
    private static File configPath;
    private static final String CONFIG_FILE_NAME = "dynamic_difficulty_general.cfg";
    private static Configuration config;

    public static double pointsLimit;

    public static int difficultyTextColor;
    public static int differenceTextColorIncrease;
    public static int differenceTextColorLowering;
    public static int differenceTextColorZero;
    public static int actionTextColor;

    public static String difficultyTextColorRaw;
    public static String differenceTextColorIncreaseRaw;
    public static String differenceTextColorLoweringRaw;
    public static String differenceTextColorZeroRaw;
    public static String actionTextColorRaw;

    public static short actionPointsTimeout;

    public DifficultyConfig(File configDir) {
        configPath = new File(configDir, CONFIG_FILE_NAME);
        loadConfig();
    }

    public void loadConfig() {
        if (!configPath.exists()) {
            createDefaultConfig();
        }

        config = new Configuration(configPath);
        try {
            config.load();

            pointsLimit = config.getFloat("Points limit", Configuration.CATEGORY_GENERAL, 10_000.0f, 10.0f, 999_999.0f,
                    "Maximum amount of points that can be hit");

            difficultyTextColorRaw = config.getString("Difficulty text color", Configuration.CATEGORY_CLIENT,
                    "0xAAAAAA",
                    "Color of the difficulty points count (far left)");
            differenceTextColorIncreaseRaw = config.getString("Difference text color - increase", Configuration.CATEGORY_CLIENT,
                    "0x10FF05",
                    "Color of the recent points accrual from passive sources: if the points increase over time (far right)");
            differenceTextColorLoweringRaw = config.getString("Difference text color - lowering", Configuration.CATEGORY_CLIENT,
                    "0xFF1005",
                    "Color of the recent points accrual from passive sources: if the points lowering over time (far right)");
            differenceTextColorZeroRaw = config.getString("Difference text color - zero", Configuration.CATEGORY_CLIENT,
                    "0xFA7A10",
                    "Color of the recent points accrual from passive sources: if the points have not changed in a last update (far right)");
            actionTextColorRaw = config.getString("Action text color", Configuration.CATEGORY_CLIENT,
                    "0x8955A6",
                    "Color of the action points: they are added to player for active actions (middle)");

            difficultyTextColor = Integer.parseInt(difficultyTextColorRaw.replace("0x", ""), 16);
            differenceTextColorIncrease = Integer.parseInt(differenceTextColorIncreaseRaw.replace("0x", ""), 16);
            differenceTextColorLowering = Integer.parseInt(differenceTextColorLoweringRaw.replace("0x", ""), 16);
            differenceTextColorZero = Integer.parseInt(differenceTextColorZeroRaw.replace("0x", ""), 16);
            actionTextColor = Integer.parseInt(actionTextColorRaw.replace("0x", ""), 16);

            actionPointsTimeout = (short) config.getInt("Action points timeout", Configuration.CATEGORY_CLIENT, 10, 1, Short.MAX_VALUE,
                    "Duration of action points display, updated with each new active action");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public void createDefaultConfig() {
        try {
            configPath.getParentFile().mkdirs();
            configPath.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
