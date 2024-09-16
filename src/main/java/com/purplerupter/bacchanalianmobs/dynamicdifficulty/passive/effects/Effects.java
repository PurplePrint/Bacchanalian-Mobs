package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.effects;

import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;
import java.util.logging.Logger;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.changers.PassiveDifficultyChanger.*;
import static com.purplerupter.bacchanalianmobs.utils.GetEvolutionPhase.getCurrentPhase;

public class Effects {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    private static final Logger LOGGER = Logger.getLogger(Effects.class.getName());
    private final EffectsConfig effectsConfig;
    private static final String NBT_TAG = "EffectData";

    public Effects(EffectsConfig effectsConfig) {
        this.effectsConfig = effectsConfig;
    }

    @SubscribeEvent
    public void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayerMP) {
            if (debug) { System.out.println("onPotionApplicable for player..."); }
            EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
            long timeStamp = player.world.getTotalWorldTime();
            if (debug) { System.out.println("Player: " + player.getName() + " // timeStamp: " + timeStamp); }

            changeTimeInTicks(timeStamp);

            String effectId = Potion.REGISTRY.getNameForObject(event.getPotionEffect().getPotion()).toString();
            short level = (short) (event.getPotionEffect().getAmplifier() + 1);
            int duration = event.getPotionEffect().getDuration();
            if (debug) { System.out.println("effectId: " + effectId + " // level: " + level + " // duration: " + duration); }

            short dimensionId = (short) player.world.provider.getDimension();
            String biomeId = player.world.getBiome(player.getPosition()).getRegistryName().toString();
            if (debug) { System.out.println("dimensionId: " + dimensionId + " // biomeId: " + biomeId); }

            byte currentPhase = getCurrentPhase(player);
            if (debug) { System.out.println("current phase: " + currentPhase); }

            updateEffectData(player, effectId, level, duration, timeStamp, dimensionId, biomeId, currentPhase);
        }
    }

    private void updateEffectData(EntityPlayerMP player, String effectId, short level, int duration, long timeStamp, short dimensionId, String biomeId, byte currentPhase) {
        if (debug) { System.out.println("updateEffectData..."); }
        NBTTagCompound playerData = player.getEntityData();
        NBTTagList effectList = playerData.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        boolean effectFound = false;

        for (int i = 0; i < effectList.tagCount(); i++) {
            NBTTagCompound effectCompound = effectList.getCompoundTagAt(i);
            if (debug) { System.out.println("effectCompound is:"); System.out.println(effectCompound); }

            if (effectCompound.getString("effectId").equals(effectId) && effectCompound.getShort("level") == level) {
                long previousTimeStamp = effectCompound.getLong("timeStamp");
                int previousDuration = effectCompound.getInteger("duration");

                if (previousTimeStamp + previousDuration >= timeStamp) {
                    int remainingDuration = (int) (previousTimeStamp + previousDuration - timeStamp);
                    effectCompound.setInteger("duration", duration + remainingDuration);
                } else {
                    effectCompound.setInteger("duration", duration);
                }

                effectCompound.setLong("timeStamp", timeStamp);
                effectFound = true;
                break;
            }
        }

        if (!effectFound) {
            NBTTagCompound newEffectCompound = new NBTTagCompound();
            newEffectCompound.setShort("dimensionId", dimensionId);
            newEffectCompound.setString("biomeId", biomeId);
            newEffectCompound.setString("effectId", effectId);
            newEffectCompound.setShort("level", level);
            newEffectCompound.setInteger("duration", duration);
            newEffectCompound.setLong("timeStamp", timeStamp);

            effectList.appendTag(newEffectCompound);
        }

        playerData.setTag(NBT_TAG, effectList);
    }

    public EffectsConfig.EffectData getEffectData(short dimension, String biomeId, String effectId, byte currentPhase, EntityPlayerMP player) {
        EffectsConfig.EffectData effectData = effectsConfig.getEffectData(dimension, biomeId, effectId);
        if (effectData != null) {
            double targetPointsLevel = getTargetPoints(effectData.getTargetPointsLevel(), currentPhase, player);
            double targetPointsDuration = getTargetPoints(effectData.getTargetPointsDuration(), currentPhase, player);

            effectData.setTargetPointsLevelParsed(targetPointsLevel);
            effectData.setTargetPointsDurationParsed(targetPointsDuration);

            return effectData;
        } else {
            LOGGER.severe("Effect data not found for: " + effectId);
            return null;
        }
    }

    private double getTargetPoints(Object targetPoints, byte phase, EntityPlayerMP player) {
        if (targetPoints instanceof Double) {
            LOGGER.info("getTargetPoints return double " + targetPoints);
            return (double) targetPoints;
        } else if (targetPoints instanceof String) {
            try {
                String[] values = ((String) targetPoints).split(";");
                LOGGER.info("getTargetPoints return element in parsed array at index" + (phase + 2) + " , and it is " + Double.parseDouble(values[phase + 2]));
                return Double.parseDouble(values[phase + 2]); // -2 is the minimum available phase
            } catch (Exception e) {
                LOGGER.severe("Cannot parse targetPoints for phase: " + phase);
            }
        }
        return 0.0;
    }

    public void clearNBT(EntityPlayerMP player) {
        Collection<PotionEffect> activeEffects = player.getActivePotionEffects();

        NBTTagCompound playerData = player.getEntityData();
        playerData.removeTag(NBT_TAG);

        for (PotionEffect effect : activeEffects) {
            String effectId = Potion.REGISTRY.getNameForObject(effect.getPotion()).toString();
            short level = (short) (effect.getAmplifier() + 1);
            int duration = effect.getDuration();
            long timeStamp = player.world.getTotalWorldTime();

            short dimensionId = (short) player.world.provider.getDimension();
            String biomeId = player.world.getBiome(player.getPosition()).getRegistryName().toString();

            NBTTagList effectList = playerData.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

            boolean effectFound = false;
            for (short i = 0; i < effectList.tagCount(); i++) {
                NBTTagCompound effectCompound = effectList.getCompoundTagAt(i);

                if (effectCompound.getString("effectId").equals(effectId) &&
                        effectCompound.getShort("level") == level) {

                    long previousTimeStamp = effectCompound.getLong("timeStamp");
                    int previousDuration = effectCompound.getInteger("duration");

                    if (previousTimeStamp + previousDuration >= timeStamp) {
                        int remainingDuration = (int) (previousTimeStamp + previousDuration - timeStamp);
                        effectCompound.setInteger("duration", duration + remainingDuration);
                    } else {
                        effectCompound.setInteger("duration", duration);
                    }

                    effectCompound.setLong("timeStamp", timeStamp);
                    effectFound = true;
                    break;
                }
            }

            if (!effectFound) {
                NBTTagCompound newEffectCompound = new NBTTagCompound();
                newEffectCompound.setShort("dimensionId", dimensionId);
                newEffectCompound.setString("biomeId", biomeId);
                newEffectCompound.setString("effectId", effectId);
                newEffectCompound.setShort("level",level);
                newEffectCompound.setInteger("duration", duration);
                newEffectCompound.setLong("timeStamp", timeStamp);

                effectList.appendTag(newEffectCompound);
            }

            playerData.setTag(NBT_TAG, effectList);
        }
    }

    public void calculateDataFromNBT(EntityPlayerMP player, long timeStampInTicks) {
        NBTTagCompound playerData = player.getEntityData();
        NBTTagList effectList = playerData.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        for (short i = 0; i < effectList.tagCount(); i++) {
            NBTTagCompound effectCompound = effectList.getCompoundTagAt(i);
            long timeStamp = effectCompound.getLong("timeStamp");
            short dimensionId = effectCompound.getShort("dimensionId");
            String biomeId = effectCompound.getString("biomeId");
            String effectId = effectCompound.getString("effectId");
            short level = effectCompound.getShort("level");
            int duration = 0;
            if (timeStampInTicks != 0) {
                duration = (int) (timeStampInTicks - timeStamp);
                changeTimeInTicks(0L);
            } else {
                duration = Math.min(difficultyUpdateInterval, effectCompound.getInteger("duration"));
            }

            byte currentPhase = getCurrentPhase(player);
            EffectsConfig.EffectData effectData = getEffectData(dimensionId, biomeId, effectId, currentPhase, player);
            if (effectData == null) { return; } // If this effect is not specified in the config

            double pointsForLevel = 0.0;
            if (level < effectData.getMinimumLevel()) {
                level = 0;
            }
            if (level > effectData.getMaximumLevel()) {
                level = effectData.getMaximumLevel();
            }
            pointsForLevel = ((double) level / (double) effectData.getTargetLevel() * effectData.getTargetPointsLevelParsed());

            double pointsForDuration = 0.0;
            if (duration < effectData.getMinimumDuration()) {
                duration = 0;
            }
            if (duration > effectData.getMaximumDuration()) {
                duration = effectData.getMaximumDuration();
            }
            pointsForDuration = ((double) duration / (double) effectData.getTargetDuration() * effectData.getTargetPointsDurationParsed());

            addPointsForEffects(pointsForLevel, pointsForDuration);
        }
    }
}
