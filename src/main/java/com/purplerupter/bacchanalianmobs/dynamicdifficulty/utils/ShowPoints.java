package com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public class ShowPoints {
    public static double returnPointsAmount(EntityPlayer player) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagCompound persistentData;

        if (playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
            return persistentData.getDouble("PlayerDifficultyPoints");
        } else {
            return 0f;
        }
    }
}
