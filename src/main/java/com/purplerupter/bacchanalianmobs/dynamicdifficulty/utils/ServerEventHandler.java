package com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils;

import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP serverPlayer = (EntityPlayerMP) event.player;
            DynamicDifficulty.sendDifficultyPointsToClient(serverPlayer);
        }
    }
}
