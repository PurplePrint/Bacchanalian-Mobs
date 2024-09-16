package com.purplerupter.bacchanalianmobs.conditions;

import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import java.util.List;

import static com.purplerupter.bacchanalianmobs.conditions.utils.GetRenderDistance.getRenderDistance;
import static com.purplerupter.bacchanalianmobs.conditions.utils.IsWithinRenderDistance.isWithinRenderDistance;

public class GameStageCondition {
    public static boolean isGameStageNearby(Entity entity, List<String> stages) {
        WorldServer world = (WorldServer) entity.world;
        //int renderDistance = getRenderDistance(world.getMinecraftServer());
        int currentDimension = entity.dimension;
        int renderDistanceRadius = getRenderDistance(world.getMinecraftServer(), currentDimension) + 1; // '+ 1' это подстраховка

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.dimension == currentDimension) {
//                int currentDimension = player.dimension;
//                if (hasGameStages(player, stages) && isWithinRenderDistance(entity, player, getRenderDistance(world.getMinecraftServer(), currentDimension))) {
                if (hasGameStages(player, stages) && isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isGameStageNearby(Entity entity, String stage) {
        WorldServer world = (WorldServer) entity.world;
        int currentDimension = entity.dimension;
        int renderDistanceRadius = getRenderDistance(world.getMinecraftServer(), currentDimension) + 1;

        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.dimension == currentDimension) {
                if (GameStageHelper.hasStage(player, stage) && isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasGameStages(EntityPlayerMP player, List<String> stages) {
        for (String stage : stages) {
            if (!GameStageHelper.hasStage(player, stage)) {
                return false;
            }
        }
        return true;
    }

}
