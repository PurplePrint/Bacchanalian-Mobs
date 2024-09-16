package com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.FMLCommonHandler;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.PassiveDifficultyChanger.maximumPointsValue;

public class ChangePoints {
    public static void changeDifficultyPoints(String username, double amount) {
        // 'player' from chat command
        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(username);
        changeDifficultyPoints(player, amount);
    }

    public static void changeDifficultyPoints(EntityPlayerMP player, double amount) {
        changeDifficultyPoints(player, amount, false);
    }

    public static void changeDifficultyPoints(EntityPlayerMP player, double amount, boolean shouldShowActionPoints) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData;

        if (!playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persistentData = new NBTTagCompound();
            playerData.setTag(EntityPlayer.PERSISTED_NBT_TAG, persistentData);
        } else {
            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
        }

        double currentDifficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
        persistentData.setDouble("PlayerDifficultyPoints", Math.min(currentDifficultyPoints + amount, maximumPointsValue));

        if (shouldShowActionPoints) {
            DynamicDifficulty.sendDifficultyPointsToClient(player, amount);
        } else {
            DynamicDifficulty.sendDifficultyPointsToClient(player, false);
        }
    }
}
