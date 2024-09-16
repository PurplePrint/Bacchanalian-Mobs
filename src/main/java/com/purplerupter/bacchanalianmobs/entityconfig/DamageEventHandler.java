package com.purplerupter.bacchanalianmobs.entityconfig;

import com.google.gson.JsonObject;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import com.purplerupter.bacchanalianmobs.conditions.Conditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class DamageEventHandler {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        Entity attacker = event.getSource().getTrueSource();
//        String mobID = entity.getEntityString();
        String mobID;
        if (entity instanceof EntityPlayerMP) {
            mobID = "minecraft:player";
        } else {
            mobID = EntityList.getKey(entity).toString();
        }
        String damageSource = event.getSource().damageType;
        if (debug) { System.out.println("Mob: " + mobID + " get damage from: " + damageSource + " source"); }

        // Получаем правило для DamageSource
        JsonObject damageSourceRule = EntityConfig.damageConfig.getDamageSourceRule(mobID, damageSource, (byte) 0);
        if (damageSourceRule != null) {
            if (debug) { System.out.println("damageSourceRule is not null! It is: " + damageSourceRule); }
            if (Conditions.checkConditions(damageSourceRule.getAsJsonObject("Conditions"), entity)) {
                if (damageSource.equals("mob") || damageSource.equals("player")) {
                    if (debug) { System.out.println("damageSource is a 'mob' or a 'player': Change damage by source is passed!"); }
                } else {
                    float multiplier = damageSourceRule.getAsJsonObject("DamageChanges").get(damageSource).getAsFloat();
                    event.setAmount(event.getAmount() * multiplier);
                    if (debug) { System.out.println("Event damage has changed: " + event.getAmount()); }
                }
            }
        }

        if (attacker != null) {
            if (debug) { System.out.println("The attacker is not null"); }
//            String attackerID = attacker.getEntityString();
//            String attackerID = EntityList.getKey(entity).toString();
            String attackerID;
            if (attacker instanceof EntityPlayerMP) {
                if (debug) { System.out.println("attackerID is 'minecraft:player'"); }
                attackerID = "minecraft:player";
            } else {
                if (debug) { System.out.println("attackerID is entity: "); }
                attackerID = EntityList.getKey(attacker).toString();
                if (debug) { System.out.println(attackerID); }
            }
            JsonObject entityDamageRule = EntityConfig.damageConfig.getEntityDamageRule(attackerID, mobID, (byte) 0);
            if (entityDamageRule != null) {
                if (debug) { System.out.println("entityDamageRule is not null"); }
                if (Conditions.checkConditions(entityDamageRule.getAsJsonObject("Conditions"), entity)) {
                    if (debug) { System.out.println("Conditions passed for " + entityDamageRule + " // " + entity); }
                    float multiplier = entityDamageRule.get("multiplier").getAsFloat();
                    if (debug) { System.out.println("The multiplier is: " + multiplier); }
                    event.setAmount(event.getAmount() * multiplier);
                    if (debug) { System.out.println("Damage has changed based on EntityDamageRule: " + event.getAmount()); }
                }
            }
        }
    }
}
