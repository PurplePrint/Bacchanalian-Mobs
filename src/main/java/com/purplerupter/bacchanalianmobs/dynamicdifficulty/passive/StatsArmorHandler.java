package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ISpecialArmor;

import java.util.HashMap;
import java.util.Map;

public class StatsArmorHandler {
    private static final Map<String, Integer> playerDefenseLevels = new HashMap<>();
    private static final Map<String, Integer> playerToughnessLevels = new HashMap<>();
    private static int customAdditionalPoints;


    public static void updateArmorStats(EntityPlayer player) {
        String playerName = player.getName();
        int newDefenseLevel = 0;
        int newHardnessLevel = 0;

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ItemStack armorItem = player.getItemStackFromSlot(slot);
                if (!armorItem.isEmpty() && armorItem.getItem() instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) armorItem.getItem();
                    // Techguns compatibility
                    if (armor instanceof ISpecialArmor) {
                        newDefenseLevel += ((ISpecialArmor) armor).getArmorDisplay(player, armorItem, slot.getIndex());
                        newHardnessLevel += armor.toughness;
                    } else {
                        newDefenseLevel += armor.damageReduceAmount;
                        newHardnessLevel += armor.toughness;
                    }
                }
            }
        }

        ArmorStats.setDefenseLevel(playerName, newDefenseLevel);
        ArmorStats.setHardnessLevel(playerName, newHardnessLevel);
    }
}
