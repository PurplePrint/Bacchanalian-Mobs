package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HPTracker {

    private static final Map<EntityPlayer, List<Float>> playerHPMap = new HashMap<>();
    private static final Map<EntityPlayer, Long> lastUpdateMap = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            scheduler.schedule(() -> updatePlayerHP(player), 20, TimeUnit.MILLISECONDS);
        }
    }

    @SubscribeEvent
    public void onPlayerHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            scheduler.schedule(() -> updatePlayerHP(player), 20, TimeUnit.MILLISECONDS);
        }
    }

    private static void updatePlayerHP(EntityPlayer player) {
        long currentTime = System.currentTimeMillis();
        if (lastUpdateMap.containsKey(player)) {
            long lastUpdateTime = lastUpdateMap.get(player);
            if ((currentTime - lastUpdateTime) < 1000) {
                return;
            }
        }
        lastUpdateMap.put(player, currentTime);
        playerHPMap.computeIfAbsent(player, k -> new ArrayList<>()).add(player.getHealth());
    }

    public static List<Float> getListOfHP(EntityPlayer player) {
        return new ArrayList<>(playerHPMap.getOrDefault(player, new ArrayList<>()));
    }

    public static void clearListOfHP(EntityPlayer player) {
        playerHPMap.getOrDefault(player, new ArrayList<>()).clear();
    }
}
