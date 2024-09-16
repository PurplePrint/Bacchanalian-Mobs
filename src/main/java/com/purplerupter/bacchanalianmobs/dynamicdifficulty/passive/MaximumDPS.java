package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive;

import com.google.common.collect.Multimap;
import com.oblivioussp.spartanweaponry.item.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MaximumDPS {

    private static final ArrayList<Double> dpsListOfAllItems = new ArrayList<>();

    public static double getMaximumDPSValue(EntityPlayer player) {
        getItemWithMaximumDPS(player);

        if (dpsListOfAllItems.isEmpty()) {
            return 0.0;
        }

        double maximumDPS = Collections.max(dpsListOfAllItems);
        dpsListOfAllItems.clear();
        return maximumDPS;
    }

    private static void getItemWithMaximumDPS(EntityPlayer player) {
        for (ItemStack itemStack : player.inventory.mainInventory) {
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof Item) {
                dpsListOfAllItems.add(getDPS(itemStack));
            }
        }
    }

    private static double getDPS(ItemStack itemStack) {
        Multimap<String, AttributeModifier> map = itemStack.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
        double attackSpeed = 4.0;
        double damage = 1.0;
        if (map.containsKey("generic.attackSpeed")) {
            attackSpeed += sumAllAndReturn(map.get("generic.attackSpeed"));
        }
        if (map.containsKey("generic.attackDamage")) {
            damage += sumAllAndReturn(map.get("generic.attackDamage"));
        }
        double actualSpeedInSeconds = Math.ceil((20 / attackSpeed)) / 20.0;

        if (isWeaponOrTool(itemStack.getItem())) {
            return damage / actualSpeedInSeconds;
        } else {
            return 1.0 / actualSpeedInSeconds;
        }
    }

    private static boolean isWeaponOrTool(Item item) {
        // Too many Spartan Weaponry stuff
        // TODO
        return (item instanceof ItemSword || item instanceof ItemTool || item instanceof ItemBattleaxe || item instanceof ItemDagger
                || item instanceof ItemGreatsword || item instanceof ItemKatana || item instanceof ItemLongsword || item instanceof ItemRapier
                || item instanceof ItemSaber || item instanceof ItemCaestus || item instanceof ItemClub || item instanceof ItemHammer
                || item instanceof ItemMace || item instanceof ItemQuarterstaff || item instanceof ItemWarhammer || item instanceof ItemGlaive
                || item instanceof ItemHalberd || item instanceof ItemLance || item instanceof ItemPike || item instanceof ItemSpear
                || item instanceof ItemThrowingKnife || item instanceof ItemThrowingAxe || item instanceof ItemScythe || item instanceof ItemParryingDagger);
    }

    private static double sumAllAndReturn(Collection<AttributeModifier> col) {
        return col.stream().map(AttributeModifier::getAmount).reduce(0.0D, Double::sum);
    }
}
