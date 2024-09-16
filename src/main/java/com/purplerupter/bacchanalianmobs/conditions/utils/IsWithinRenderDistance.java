package com.purplerupter.bacchanalianmobs.conditions.utils;

import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;

public class IsWithinRenderDistance {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    public static boolean isWithinRenderDistance(Entity entity, EntityPlayerMP player, int renderDistance) {
//        double entityX = entity.posX;
//        double entityZ = entity.posZ;
        int entityX = (int) entity.posX;
        int entityZ = (int) entity.posZ;
        int chunkXEntity = entityX >> 4;
        if (debug) { System.out.println("Entity coordinates X: " + chunkXEntity); }
        int chunkZEntity = entityZ >> 4;
        if (debug) { System.out.println("Entity coordinates Z: " + chunkZEntity); }

        int chunkXPlayer = player.chunkCoordX;
        if (debug) { System.out.println("Player coordinate X: " + chunkXPlayer); }
        int chunkZPlayer = player.chunkCoordZ;
        if (debug) { System.out.println("Player coordinate Z: " + chunkZPlayer); }

        boolean result = Math.abs(chunkXEntity - chunkXPlayer) <= renderDistance && Math.abs(chunkZEntity - chunkZPlayer) <= renderDistance;
        if (debug) { System.out.println("The result is: " + result); }
        return result;
    }
}
