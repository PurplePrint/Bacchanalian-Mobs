package com.purplerupter.bacchanalianmobs.dynamicdifficulty.passive.items;

import com.dhanantry.scapeandrunparasites.world.SRPSaveData;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.purplerupter.bacchanalianmobs.dynamicdifficulty.DynamicDifficulty.itemCostConfig;

public class PointsPerItems {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    public static Map<String, Double> calculatePointsForInventory(EntityPlayerMP player) {
        World world = player.getEntityWorld();
        MinecraftServer server = player.getEntityWorld().getMinecraftServer();
        if (debug) { System.out.println("World: " + world + " // Server: " + server + " // Player: " + player); }
        if (server != null && !world.isRemote) {
            Set<String> stages = new HashSet<>(GameStageHelper.getPlayerData(player).getStages());
            byte phase = SRPSaveData.get(world).getEvolutionPhase(world.provider.getDimension());
            return calculatePointsForInventory(player, world, stages, phase);
        } else {
            if (debug) { System.out.println("[calculatePointsForInventory] Error! The player's world is client!"); }
            return null;
        }
    }

    public static Map<String, Double> calculatePointsForInventory(EntityPlayerMP player, World world, Set<String> playerStages, byte phase) {
        Map<String, Double> pointsMap = new HashMap<>();

        for (Slot slot : player.inventoryContainer.inventorySlots) {
            ItemStack stack = slot.getStack();
            if (!stack.isEmpty()) {
                String itemID = stack.getItem().getRegistryName().toString();
                double itemCost = itemCostConfig.getItemCost(player, itemID, world, player.getPosition().getY(), playerStages, phase);
                pointsMap.put(itemID, itemCost);
            }
        }

        return pointsMap;
    }

    /**
     * Добавляет в NBT игрока словарь "PointsPerEachItem", где ключ - ID предмета, а значение - число double.
     * При повторном вызове метода словарь полностью перезаписывается.
     */
    public void updatePlayerNBT(EntityPlayerMP player, Map<String, Double> pointsMap) {
        NBTTagCompound playerNBT = player.getEntityData();
        NBTTagList pointsList = new NBTTagList();

        for (Map.Entry<String, Double> entry : pointsMap.entrySet()) {
            NBTTagCompound itemData = new NBTTagCompound();
            itemData.setString("ItemID", entry.getKey());
            itemData.setTag("Points", new NBTTagDouble(entry.getValue()));
            pointsList.appendTag(itemData);
        }

        playerNBT.setTag("PointsPerEachItem", pointsList);
    }
}
