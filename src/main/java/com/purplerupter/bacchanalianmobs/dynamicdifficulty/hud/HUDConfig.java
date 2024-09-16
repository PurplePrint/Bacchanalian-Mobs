package com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.File;

public class HUDConfig {

    public static float customPosX;
    public static float customPosY;
    public static float customScale;
    public static boolean visibility = true;

    private static Configuration config;

    public static void init(File configDir) {
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "dynamic_difficulty_hud.cfg");
        config = new Configuration(configFile);

        try {
            config.load();

            customPosX = config.getFloat("customPosX", Configuration.CATEGORY_GENERAL, 1.0f, Integer.MIN_VALUE, Integer.MAX_VALUE, "X position offset of the HUD");
            customPosY = config.getFloat("customPosY", Configuration.CATEGORY_GENERAL, 97.2000f, Integer.MIN_VALUE, Integer.MAX_VALUE, "Y position offset of the HUD");
            customScale = config.getFloat("customScale", Configuration.CATEGORY_GENERAL, 1.0f, 0.1f, Float.MAX_VALUE, "Scale of the HUD");
            visibility = config.getBoolean("visibility", Configuration.CATEGORY_GENERAL, true, "Show/hide difficulty HUD.");
        } catch (Exception e) {
            // Log the exception
        } finally {
            if (config.hasChanged()) {
                config.save();
            }
        }
    }

    public static void saveConfig() {
        config.get(Configuration.CATEGORY_GENERAL, "customPosX", customPosX).set(customPosX);
        config.get(Configuration.CATEGORY_GENERAL, "customPosY", customPosY).set(customPosY);
        config.get(Configuration.CATEGORY_GENERAL, "customScale", customScale).set(customScale);
        config.get(Configuration.CATEGORY_GENERAL, "visibility", visibility).set(visibility);

        if (config.hasChanged()) {
            config.save();
        }
    }
}
