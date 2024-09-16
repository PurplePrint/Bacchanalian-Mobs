package com.purplerupter.bacchanalianmobs.dynamicdifficulty;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.actions.PointsPerKill;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.actions.PointsPerSleep;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.ChangePoints;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.PassiveDifficultyChanger;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands.*;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands.CommandGetItemCost;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.gs.GameStageConfigHandler;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.gs.GameStagesSupport;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.HUDCommand;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.HUDConfig;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.RenderHUD;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.hud.VisibilityKeybind;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.HPTracker;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.effects.Effects;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.effects.EffectsConfig;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items.ItemCostConfig;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items.PointsPerItems;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.srp.EvolutionPhaseTracker;
//import com.purplerupter.bacchanalianmobs.utils.CustomTextManager;
//import com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.PacketDifficulty;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.ServerEventHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

//import java.io.File;
import java.text.DecimalFormat;

import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.MODID;
import static com.purplerupter.bacchanalianmobs.BacchanalianMobs.configDir;

public class DynamicDifficulty {
    public static DecimalFormat df2 = new DecimalFormat("#.##");

    public static PassiveDifficultyChanger passiveDifficultyChanger;
    private static EffectsConfig effectsConfig;
    private static Effects effects;
    public static PointsPerItems pointsPerItems;
    public static ItemCostConfig itemCostConfig;

    public static SimpleNetworkWrapper network;

//    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new DifficultyConfig(configDir));

        effectsConfig = new EffectsConfig(configDir);
        effects = new Effects(effectsConfig);
        MinecraftForge.EVENT_BUS.register(effects);

//        MinecraftForge.EVENT_BUS.register(new ItemCostConfig(configDir));
        itemCostConfig = new ItemCostConfig(configDir);
        MinecraftForge.EVENT_BUS.register(itemCostConfig);
        pointsPerItems = new PointsPerItems();

        MinecraftForge.EVENT_BUS.register(new HPTracker());
//        MinecraftForge.EVENT_BUS.register(new PassiveDifficultyChanger(effects));
        passiveDifficultyChanger = new PassiveDifficultyChanger(effects, configDir);
        MinecraftForge.EVENT_BUS.register(passiveDifficultyChanger);

        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        network.registerMessage(PacketDifficulty.Handler.class, PacketDifficulty.class, 0, Side.CLIENT);

        // Game Stages support
//        File configFile = new File(configDir, "points_per_stages.cfg");
        GameStageConfigHandler configHandlergs = new GameStageConfigHandler(configDir);
        MinecraftForge.EVENT_BUS.register(new GameStagesSupport(configHandlergs));

        if (event.getSide() == Side.CLIENT) {
            MinecraftForge.EVENT_BUS.register(RenderHUD.INSTANCE);
            HUDConfig.init(configDir);
            MinecraftForge.EVENT_BUS.register(new VisibilityKeybind());
        }

//        MinecraftForge.EVENT_BUS.register(new DimensionTimeTracker());
        MinecraftForge.EVENT_BUS.register(new EvolutionPhaseTracker(configDir));
        MinecraftForge.EVENT_BUS.register(new ChangePoints());
        MinecraftForge.EVENT_BUS.register(new PointsPerSleep(configDir));
        MinecraftForge.EVENT_BUS.register(new PointsPerKill(configDir));
    }

//    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
    }

//    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ShowPointsCommand());
        event.registerServerCommand(new CommandAddPassiveBonus());
        event.registerServerCommand(new CommandRemovePassiveBonus());
        event.registerServerCommand(new HUDCommand());
        event.registerServerCommand(new CommandAddPoints());
        CommandShowDimensionTime.register(event);

        event.registerServerCommand(new CommandGetItemCost(itemCostConfig));
    }

    public static void sendDifficultyPointsToClient(EntityPlayerMP player) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        double difficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
        network.sendTo(new PacketDifficulty(difficultyPoints, true, 0), player);
    }

    public static void sendDifficultyPointsToClient(EntityPlayerMP player, double amount) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        double difficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
        network.sendTo(new PacketDifficulty(difficultyPoints, false, (float) amount), player);

//        renderActionPoints((float) amount);
    }

    public static void sendDifficultyPointsToClient(EntityPlayerMP player, boolean bruhDRY) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

        double difficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
        network.sendTo(new PacketDifficulty(difficultyPoints, false, 0), player);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        passiveDifficultyChanger.onPlayerLogin(event);
    }
}
