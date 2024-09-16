package com.purplerupter.bacchanalianmobs.utils;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class GetEvolutionPhase {
    private static final boolean debug = CommonConfig.allowDebugMessages;
    public static byte getCurrentPhase(Entity entity) {
        World world = entity.getEntityWorld();
        MinecraftServer server = entity.getEntityWorld().getMinecraftServer();
        if (server != null && !world.isRemote) {
            byte phase = (SRPSaveData.get(world).getEvolutionPhase(world.provider.getDimension()));
            System.out.println("Current SRP phase in the " + world + " world is: " + phase);
            return (phase); // the minimum phase is -2
        } else {
            if (debug) { System.out.println("Error! The entity world is client! Can't get SRP phase for this entity!"); }
            return 0;
        }
    }
}
