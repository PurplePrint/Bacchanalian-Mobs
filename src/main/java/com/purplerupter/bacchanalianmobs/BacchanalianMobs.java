package com.purplerupter.bacchanalianmobs;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import com.purplerupter.bacchanalianmobs.entityconfig.EntityConfig;
import com.purplerupter.bacchanalianmobs.utils.CustomTextManager;
import com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.File;

@Mod(modid = BacchanalianMobs.MODID, name = BacchanalianMobs.NAME, version = BacchanalianMobs.VERSION)
public class BacchanalianMobs {
    public static final String MODID = "bacchanalianmobs";
    public static final String NAME = "Bacchanalian Mobs";
    public static final String VERSION = "0.2.1 INDEV";

    private DynamicDifficulty dynamicDifficulty = new DynamicDifficulty();

    public static File configDir;
    public static final String configDirName = "Bacchanalian Mobs";

    public static final String DEFAULT_CONFIG_PREFIX = "default_";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        configDir = new File(event.getModConfigurationDirectory(), configDirName);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }

        MinecraftForge.EVENT_BUS.register(new CommonConfig(configDir));
        CustomTextManager.init(configDir);
        MinecraftForge.EVENT_BUS.register(new DimensionTimeTracker());

        if (CommonConfig.enableDifficultyModule) {
            dynamicDifficulty.preInit(event);
            System.out.println("The Dynamic Difficulty module loaded");
        } else {
            System.out.println("The Dynamic Difficulty module is disabled");
        }

        if (CommonConfig.enableEntityConfigModule) {
            EntityConfig.preInit(event);
            System.out.println("The Entity Config module loaded");
        } else {
            System.out.println("The Entity Config module is disabled");
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        dynamicDifficulty.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        dynamicDifficulty.serverStarting(event);
    }

}
