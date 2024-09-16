package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import com.purplerupter.bacchanalianmobs.conditions.Conditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.purplerupter.bacchanalianmobs.utils.GetEvolutionPhase.getCurrentPhase;

public class AttackAndDefenseEventHandler {
    private static final boolean debug = CommonConfig.allowDebugMessages;
    private AttackAndDefenseConfig config;

    public AttackAndDefenseEventHandler(AttackAndDefenseConfig config) {
        this.config = config;
        if (debug) { System.out.println("AttackAndDefenseConfig loaded into EntityEventHandler"); }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        Entity source = event.getSource().getTrueSource();
        Entity target = event.getEntity();

        if (source == null) {
            if (debug) { System.out.println("onLivingAttack event failed - the source is null!"); }
            return;
        }
        if (target == null) {
            if (debug) { System.out.println("onLivingAttack event failed - the target is null!"); }
            return;
        }

        if (debug) { System.out.println("onLivingAttack event."); }
        if (debug) { System.out.println("Source is: " + source + " // target is: " + target); }

        if (source instanceof EntityPlayer) {
            handleDefense((EntityPlayer) source, target);
            if (debug) { System.out.println("handleDefense " + source + " // " + target); }
        } else if (target instanceof EntityPlayer) {
            handleAttack((EntityPlayer) target, source);
            if (debug) { System.out.println("handleAttack " + source + " // " + target); }
        }
    }

    private void handleDefense(EntityPlayer player, Entity target) {
        if (debug) { System.out.println("handleDefense started!"); }
        byte index = (byte)(getCurrentPhase(target) + 2);
        String mobId = EntityList.getKey(target).toString();
        if (debug) { System.out.println("Index is: " + index); }

        JsonObject defenseRule = config.getMobDefenseRule(mobId, index);
        if (debug) { System.out.println("Mob name is: " + mobId); }
        if (defenseRule != null) {
            if (debug) { System.out.println("defenseRule is not null"); }
            if (Conditions.checkConditions(defenseRule.getAsJsonObject("Conditions"), target)) {
                if (debug) { System.out.println("Conditions passed!"); }
                applyEffectsAndDamage(defenseRule.getAsJsonObject("Damage"), defenseRule.getAsJsonObject("Effects"), player, target);
            }
        } else {
            if (debug) { System.out.println("Defense rule is null!!!"); }
        }
    }

    private void handleAttack(EntityPlayer player, Entity source) {
        if (debug) { System.out.println("handleAttack started!"); }
        byte index = (byte)(getCurrentPhase(source) + 2);
        String mobId = EntityList.getKey(source).toString();
        if (debug) { System.out.println("Index is: " + index); }

        JsonObject offenseRule = config.getMobOffenseRule(mobId, index);
        if (debug) { System.out.println("Mob name is: " + mobId); }
        if (offenseRule != null) {
            if (debug) { System.out.println("attackRule is not null"); }
            if (Conditions.checkConditions(offenseRule.getAsJsonObject("Conditions"), source)) {
                if (debug) { System.out.println("Conditions passed!"); }
                applyEffectsAndDamage(offenseRule.getAsJsonObject("Damage"), offenseRule.getAsJsonObject("Effects"), player, source);
            } else {
                if (debug) { System.out.println("Conditions not passed!!!"); }
            }
        } else {
            if (debug) { System.out.println("Offense rule is null!!!"); }
        }
    }

    private void applyEffectsAndDamage(JsonObject damage, JsonObject effects, EntityPlayer player, Entity entity) {
        if (debug) { System.out.println("applyEffectsAndDamage started!"); }
        double amount = damage.get("amount").getAsDouble();
        String type = damage.get("type").getAsString();
//        String source = damage.get("source").getAsString();
        DamageSource source = new DamageSource(damage.get("source").getAsString());
        if (debug) { System.out.println("Damage amount is: " + amount + " // Damage type is: " + type + " // Damage source is: " + source); }

        if (type.equals("hp")) {
            player.attackEntityFrom(source, (float) amount);
        }
        if (type.equals("percent")) {
//            float damageAmount = player.getHealth() / 100 * (float) amount;
            float damageAmount = player.getMaxHealth() / 100 * (float) amount;
            player.attackEntityFrom(source, damageAmount);
        } else {
            if (debug) { System.out.println("Error! The damage type is not 'hp' and not 'percent'! It's: " + type); }
        }

        String effectID = effects.get("effectID").getAsString();
        int duration = effects.get("duration").getAsInt();
        int level = effects.get("level").getAsInt();
        if (debug) { System.out.println(effectID + " // " + level + " // " + duration); }

        if (effectID != null) {
            player.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(effectID), duration, level));
        } else {
            if (debug) { System.out.println("Error! The effectID is null!"); }
        }
    }

//    private double getDifficulty(EntityPlayer player) {
//        if (debug) { System.out.println("getDifficulty started"); }
//        NBTTagCompound playerData = player.getEntityData();
//        NBTTagCompound persistentData;
//        if (playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
//            persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
//            double playerDifficultyPoints = persistentData.getDouble("PlayerDifficultyPoints");
//            if (debug) { System.out.println("Player " + player.getName() + " have " + playerDifficultyPoints + " difficulty points"); }
//            return playerDifficultyPoints;
//        } else {
//            if (debug) { System.out.println("Error! Player " + player.getName() + " have not difficulty points in the NBT tag"); }
//            return 0;
//        }
//    }
}
