package com.purplerupter.bacchanalianmobs.entityconfig;

import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.configDir;

public class EntityConfig {
    private static final  boolean debug = CommonConfig.allowDebugMessages;
    public static DamageConfig damageConfig;

    public static void preInit(FMLPreInitializationEvent event) {
        if (debug) { System.out.println("[EntityConfig] Pre-Initialization started."); }

        AttackAndDefenseConfig config = new AttackAndDefenseConfig(configDir);
        MinecraftForge.EVENT_BUS.register(new AttackAndDefenseEventHandler(config));

        damageConfig = new DamageConfig(configDir);
        MinecraftForge.EVENT_BUS.register(new DamageEventHandler());

        ScalingPropsConfig config1 = new ScalingPropsConfig(configDir);
        MinecraftForge.EVENT_BUS.register(new ScalingPropsEventHandler(config1));

        if (debug) { System.out.println("[EntityConfig] Pre-Initialization completed."); }

    }
}
