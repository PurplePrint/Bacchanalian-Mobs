package com.purplerupter.bacchanalianmobs;

import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.io.IOException;

public class CommonConfig {
    private static File configPath;
    private static final String CONFIG_FILE_NAME = "Common_Config.cfg";
    private static Configuration config;

    public static boolean allowDebugMessages;
    public static boolean enableDifficultyModule;
    public static boolean enableEntityConfigModule;

    public CommonConfig(File configDir) {
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
            allowDebugMessages = config.getBoolean("Allow debug messages", Configuration.CATEGORY_GENERAL, false, "Messages in debug.log file");
            enableDifficultyModule = config.getBoolean("Enable the Dynamic Difficulty module", Configuration.CATEGORY_GENERAL, true,
                    "The Dynamic Difficulty module allows you to change the behavior of many other modules very flexibly. I do not recommend disabling it unless absolutely necessary.");
            enableEntityConfigModule = config.getBoolean("Enable the Entity Config module", Configuration.CATEGORY_GENERAL, true,
                    "The Entity Config module allows you to change the damage that entities take from different sources and from different attackers; " +
                            "it also allows you to apply potion effects and deal damage as a defense or offense.");

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
