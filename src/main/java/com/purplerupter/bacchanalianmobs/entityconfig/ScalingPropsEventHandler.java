package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;

import static com.purplerupter.bacchanalianmobs.conditions.Conditions.checkConditions;
import static com.purplerupter.bacchanalianmobs.conditions.NearbyPlayers.getNearbyPlayers;
import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.utils.ShowPoints.returnPointsAmount;
import static com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker.*;
import static com.purplerupter.bacchanalianmobs.utils.GetEvolutionPhase.getCurrentPhase;

public class ScalingPropsEventHandler {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    private ScalingPropsConfig config;

    public ScalingPropsEventHandler(ScalingPropsConfig trueConfig) {
        this.config = trueConfig;
    }

    @SubscribeEvent
    public void onMobSpawn(EntityJoinWorldEvent event) {
        if (debug) { System.out.println("====  ====  ====  ===="); System.out.println("====  ====  ====  ===="); System.out.println("onMobSpawn event..."); }
        Entity entity = event.getEntity();
        if (debug) { System.out.println(entity); }

        if (entity == null || !(entity instanceof EntityLivingBase) || entity instanceof EntityPlayerMP || entity instanceof EntityPlayer) { return; }
        String mobID = EntityList.getKey(entity).toString();
        if (config.getRuleForMob(mobID, (byte)0) == null) { if (debug) { System.out.println("Don't find any rule for " + mobID + " mob"); } return; }

        if (debug) { System.out.println("The mobID is: " + mobID); }
        byte phase = getCurrentPhase(entity);
        if (debug) { System.out.println("The phase is: " + phase); }
        JsonObject rule = config.getRuleForMob(mobID, phase); // Получаем правило
        if (debug) { System.out.println("The config rule is: " + rule); }

        if (rule != null && checkConditions(rule.getAsJsonObject("Conditions"), entity)) {
            if (debug) { System.out.println("The rule is not null and conditions passed"); }
            double scalingFactor = 1;

            if (rule.has("ChangeByDifficulty")) {
                if (debug) { System.out.println("ChangeByDifficulty..."); }
                JsonObject changeByDifficulty = rule.getAsJsonObject("ChangeByDifficulty");
                if (debug) { System.out.println("That rule is: "); }
                if (debug) { System.out.println(changeByDifficulty); }
                double minDiff = changeByDifficulty.get("DifficultyMin").getAsDouble();
                double maxDiff = changeByDifficulty.get("DifficultyMax").getAsDouble();
                double targetDiff = changeByDifficulty.get("TargetDifficulty").getAsDouble();
                float targetMultiplier = getTargetMultiplier(changeByDifficulty, getCurrentPhase(entity));
                if (debug) { System.out.println("Parsed: "); }
                if (debug) { System.out.println(minDiff + " // " + maxDiff + " // " + targetDiff + " // " + targetMultiplier); }

                List<EntityPlayerMP> players = getNearbyPlayers(entity);
                if (debug) { System.out.println("List of online players: " + players); }
                if (debug) { System.out.println("Starting a 'for' code block..."); }
                for (EntityPlayerMP player : players) {
                    if (debug) { System.out.println("A player: " + player); }
                    double difficulty = returnPointsAmount(player);
                    if (debug) { System.out.println("Difficulty points for this player: " + difficulty); }

                    if (difficulty >= minDiff) {
                        if (debug) { System.out.println("The difficulty is more than 'minDiff' (" + minDiff + ")"); }
                        if (difficulty > maxDiff) {
                            if (debug) { System.out.println("The difficulty is more than 'maxDiff' (" + maxDiff + ")"); }
                            difficulty = maxDiff;
                        }
                        if (debug) { System.out.println("Changing scalingFactor..."); }
                        scalingFactor = (difficulty / targetDiff) * targetMultiplier;
                    } else {
                        if (debug) { System.out.println("The difficulty is less than 'minDiff' (" + minDiff + ")"); }
                    }
                }
            } else {
                if (debug) { System.out.println("This rule has no ChangeByDifficulty"); }
            }

            if (rule.has("ChangeByTime")) {

                JsonObject changeByTime = rule.getAsJsonObject("ChangeByTime");
                long minTime = changeByTime.get("TimeMin").getAsLong();
                long maxTime = getTimeMax(changeByTime);
                long targetTime = changeByTime.get("TargetTime").getAsLong();
                float targetMultiplier = getTargetMultiplier(changeByTime, getCurrentPhase(entity));

                List<EntityPlayerMP> players = getNearbyPlayers(entity);
                if (debug) { System.out.println("List of online players: " + players); }
                if (debug) { System.out.println("Starting a 'for' code block..."); }
                for (EntityPlayerMP player : players) {
                    if (debug) { System.out.println("A player: " + player); }
                    long time = getTimeInDimension(player);
                    if (debug) { System.out.println("Time amount for this player: " + time); }

                    if (time >= minTime) {
                        if (debug) { System.out.println("The time is more than 'minTime' (" + minTime + ")"); }
                        if (time > maxTime) {
                            if (debug) { System.out.println("The time is more than 'maxTime' (" + maxTime + ")"); }
                            time = maxTime;
                        }
                        scalingFactor *= ((float)time / targetTime) * targetMultiplier;
                    } else {
                        if (debug) { System.out.println("The time is less than 'minTime' (" + minTime + ")"); }
                    }
                }
            } else {
                if (debug) { System.out.println("This rule have no ChangeByTime"); }
            }

            if (debug) { System.out.println("Call scaleHealth method..."); }
            scaleHealth((EntityLivingBase) entity, scalingFactor);

            } else {
                if (debug) { System.out.println("The rule is null or conditions not passed!"); }
            }
    }

    private void scaleHealth(EntityLivingBase entity, double multiplier) {
        if (debug) { System.out.println("scaleHealth..."); System.out.println(entity); }

        // Получаем текущее значение максимального здоровья
        IAttributeInstance healthAttribute = entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        double baseMaxHealth = healthAttribute.getBaseValue();  // Базовое значение здоровья
        if (debug) { System.out.println("Base Max Health: " + baseMaxHealth); }

        // Вычисляем новое максимальное здоровье
        float newMaxHealth = (float)(baseMaxHealth * multiplier);
        if (debug) { System.out.println("New Max Health: " + newMaxHealth); }

        // Устанавливаем новое значение максимального здоровья
        healthAttribute.setBaseValue(newMaxHealth);

        // Корректируем текущее здоровье, чтобы не было больше максимального
        if (entity.getHealth() > newMaxHealth) {
            entity.setHealth(newMaxHealth);
        } else {
            entity.setHealth(newMaxHealth);
        }
    }

    private long getTimeMax(JsonObject changeByTime) {
        // Получение TimeMax (либо число, либо строка "x")
        if (changeByTime.get("TimeMax").isJsonPrimitive()) {
            if (changeByTime.get("TimeMax").getAsJsonPrimitive().isNumber()) {
                return changeByTime.get("TimeMax").getAsLong();
            } else if ("x".equals(changeByTime.get("TimeMax").getAsString())) {
                return Long.MAX_VALUE;
            }
        }
        return 0;
    }

    private float getTargetMultiplier(JsonObject ruleSection, byte index) {
        if (ruleSection.get("TargetMultiplier").isJsonArray()) {
            return ruleSection.get("TargetMultiplier").getAsJsonArray().get(index).getAsFloat();
        } else {
            return ruleSection.get("TargetMultiplier").getAsFloat();
        }
    }
}
