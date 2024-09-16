package com.purplerupter.bacchanalianmobs.dynamicdifficulty.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.util.HashMap;
import java.util.Map;

public class CommandShowDimensionTime extends CommandBase {

    @Override
    public String getName() {
        return "showdimensiontime";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/showdimensiontime - Shows the time spent in each dimension.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) sender;
            NBTTagCompound persistentData = getOrCreatePersistentData(player);

            Map<Short, Integer> timeSpent = getTimeSpentMap(persistentData);

            player.sendMessage(new TextComponentString("Time spent in each dimension:"));

            for (Map.Entry<Short, Integer> entry : timeSpent.entrySet()) {
                short dimensionId = entry.getKey();
                int timeInSeconds = entry.getValue();
                String timeFormatted = formatTime(timeInSeconds);

                player.sendMessage(new TextComponentString("Dimension " + dimensionId + ": " + timeFormatted));
            }
        } else {
            sender.sendMessage(new TextComponentString("This command can only be used by a player."));
        }
    }

    private NBTTagCompound getOrCreatePersistentData(EntityPlayerMP player) {
        NBTTagCompound playerData = player.getEntityData();
        if (!playerData.hasKey(EntityPlayerMP.PERSISTED_NBT_TAG)) {
            NBTTagCompound persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayerMP.PERSISTED_NBT_TAG, persistentData);
            return persistentData;
        } else {
            return playerData.getCompoundTag(EntityPlayerMP.PERSISTED_NBT_TAG);
        }
    }

    private Map<Short, Integer> getTimeSpentMap(NBTTagCompound persistentData) {
        Map<Short, Integer> timeSpent = new HashMap<>();
        if (persistentData.hasKey("TimeSpentInDimensions")) {
            NBTTagCompound timeSpentTag = persistentData.getCompoundTag("TimeSpentInDimensions");
            for (String key : timeSpentTag.getKeySet()) {
                short dimensionId = Short.parseShort(key);
                int time = timeSpentTag.getInteger(key);
                timeSpent.put(dimensionId, time);
            }
        }
        return timeSpent;
    }

    private String formatTime(int timeInSeconds) {
        int hours = timeInSeconds / 72000;
        int minutes = (timeInSeconds % 72000) / 1200;
        int seconds = (timeInSeconds % 1200) / 20;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void register(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandShowDimensionTime());
    }
}
