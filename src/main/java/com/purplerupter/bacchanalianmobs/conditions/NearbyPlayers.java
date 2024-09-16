package com.purplerupter.bacchanalianmobs.conditions;

import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.List;

import static com.purplerupter.bacchanalianmobs.conditions.utils.GetRenderDistance.getRenderDistance;
import static com.purplerupter.bacchanalianmobs.conditions.utils.IsWithinRenderDistance.isWithinRenderDistance;

public class NearbyPlayers {
    private static final boolean debug = CommonConfig.allowDebugMessages;
    public static EntityPlayerMP getNearbyPlayer(Entity entity) {
        WorldServer world = (WorldServer) entity.world;
        int currentDimension = entity.dimension;
        int renderDistanceRadius = getRenderDistance(world.getMinecraftServer(), currentDimension) + 1; // '+ 1' это подстраховка


        for (EntityPlayerMP player : world.getMinecraftServer().getPlayerList().getPlayers()) {
            if (player.dimension == currentDimension) {
                if (isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                    return player;
                }
            }
        }
        return null;
    }

    public static List<EntityPlayerMP> getNearbyPlayers(Entity entity) {
        List<EntityPlayerMP> playerList = new ArrayList<>();

        MinecraftServer server = entity.getEntityWorld().getMinecraftServer();
        if (debug) { System.out.println("MinecraftServer from getEntityWorld is: " + server); }

        if (server != null && !entity.world.isRemote) {
            if (debug) { System.out.println("Server is valid and not remote (i.e., this is server-side code)"); }

            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                if (debug) { System.out.println("Player: " + player.getName() + " // in dimension: " + player.dimension); }

                if (player.dimension == entity.dimension) {
                    if (debug) { System.out.println("Player dimension matches entity dimension!"); }

                    int renderDistanceRadius = getRenderDistance(server, player.dimension) + 1;
                    if (debug) { System.out.println("The render distance is: " + renderDistanceRadius); }

                    if (isWithinRenderDistance(entity, player, renderDistanceRadius)) {
                        if (debug) { System.out.println("Adding player " + player.getName() + " to the playerList"); }
                        playerList.add(player);
                    } else {
                        if (debug) { System.out.println("The player " + player.getName() + " is too far away!"); }
                    }
                }
            }
        } else {
            if (debug) { System.out.println("Server is null or running on client (remote world)"); }
        }

        return playerList;
    }

}
