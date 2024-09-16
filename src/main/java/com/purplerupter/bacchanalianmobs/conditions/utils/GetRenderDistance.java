package com.purplerupter.bacchanalianmobs.conditions.utils;

import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;

public class GetRenderDistance {
    private static final boolean debug = CommonConfig.allowDebugMessages;
    public static int getRenderDistance(MinecraftServer server, int dimensionId) {
        if (server.isSinglePlayer()) {
            int result = Minecraft.getMinecraft().gameSettings.renderDistanceChunks;
            if (debug) { System.out.println("This world is a singleplayer, and the render distance is: " + result); }
            return result;
        } else {
//            int result = server.getWorld(dimensionId).getMinecraftServer().getPlayerList().getViewDistance();
            int result;
            try {
                result = server.getWorld(dimensionId).getMinecraftServer().getPlayerList().getViewDistance();
            } catch (NullPointerException e) {
                if (debug) { System.out.println("NullPointerException in 'getPlayerList()' when trying to get the render distance!"); }
                result = 7;
            }
            if (debug) { System.out.println("This world is multiplayer, and the render distance is: " + result); }
            return result;
        }
    }

}
