package com.purplerupter.bacchanalianmobs.conditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.purplerupter.bacchanalianmobs.CommonConfig;
import com.purplerupter.bacchanalianmobs.dynamicdifficulty.DifficultyConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.purplerupter.bacchanalianmobs.conditions.GameStageCondition.isGameStageNearby;
import static com.purplerupter.bacchanalianmobs.conditions.NearbyPlayers.getNearbyPlayers;
import static com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker.TIME_SPENT_TAG;
import static com.purplerupter.bacchanalianmobs.utils.DimensionTimeTracker.updateTimeSpent;
import static com.purplerupter.bacchanalianmobs.utils.GetEvolutionPhase.getCurrentPhase;

public class Conditions {
    private static final boolean debug = CommonConfig.allowDebugMessages;

    public static boolean checkConditions(JsonObject conditions, Entity entity) {
        if (debug) { System.out.println("checkConditions..."); }
        for (Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
            if (debug) { System.out.println("Checking conditions for a one of groups..."); }
            JsonObject conditionSet = entry.getValue().getAsJsonObject();

            if (!checkRandom(conditionSet)) continue;
            if (!checkDimensionID(conditionSet, entity)) continue;
            if (!checkBiomeID(conditionSet, entity)) continue;
            if (!checkYRange(conditionSet, entity)) continue;

            if (!checkPhase(conditionSet, entity)) continue;

            if (!checkStages(conditionSet, entity)) continue;

            if (!checkDifficulty(conditionSet, entity)) continue;
            if (!checkTime(conditionSet, entity)) continue;
            // время в измерении

            if (debug) { System.out.println("All conditions return true or not processed!"); }
            return true;
        }
        return false;
    }

    private static boolean checkRandom(JsonObject conditionSet) {
        if (debug) { System.out.println("checkRandom..."); }
        if (conditionSet.has("chance")) {
            if (debug) { System.out.println("That condition group has 'chance'!"); }
            Random random = new Random();
            int chance = conditionSet.get("chance").getAsInt();
//            if (debug) { System.out.println("return!"); }
//            return random.nextInt(100) < chance;
            if (debug) { System.out.println("The chance from config is: " + chance); }
            int randomNumber = random.nextInt(100);
            if (debug) { System.out.println("A random number from 0 to 100 is: " + randomNumber); }
            return (randomNumber < chance);
        }
        return true;
    }

    private static boolean checkDimensionID(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkDimensionID..."); }
        if (conditionSet.has("dimensionID")) {
            if (debug) { System.out.println("That condition group has dimensionID!"); }
            int dimensionID = conditionSet.get("dimensionID").getAsInt();
            int currentDinmension = entity.world.provider.getDimension();
            if (debug) { System.out.println("The entity dimension is " + currentDinmension + ", the dimension ID from config is: " + dimensionID); }
            return currentDinmension == dimensionID;
        }
        return true;
    }

    private static boolean checkBiomeID(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkBiomeID..."); }
        if (conditionSet.has("biomeID")) {
            if (debug) { System.out.println("That condition group has biomeID!"); }
            String biomeID = conditionSet.get("biomeID").getAsString();
            BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);
            String currentBiome = entity.world.getBiome(pos).getRegistryName().toString();
            if (debug) { System.out.println("The entity biome is: " + currentBiome + ", the biome ID from config is: " + biomeID); }
//            if (debug) { System.out.println("return!"); }
//            return entity.world.getBiome(pos).getRegistryName().toString().equals(biomeID);
            return (currentBiome.equals(biomeID));
        }
        return true;
    }

    private static boolean checkYRange(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkYRange..."); }
        if (conditionSet.has("yRange")) {
            if (debug) { System.out.println("That condition group has yRange!"); }
            JsonArray yRange = conditionSet.get("yRange").getAsJsonArray();
//            int yMin = yRange.get(0).getAsInt();
//            int yMax = yRange.get(1).getAsInt();
            short yMin = 0;
            short yMax = 255;
            try {
                yMin = yRange.get(0).getAsShort();
                if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range condition successfully parsed as a short number"); }
            } catch (NumberFormatException e1) {
                if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range condition is not a number. Try to parse it as 'x' (minimum value)..."); }
                try {
                    if (yRange.get(0).getAsString().equals("x")) {
                        if (debug) { System.out.println("Apply the minimum value (0) to the first Y coordinate in that Y Range"); }
                        yMin = 0;
                    } else {
                        if (debug) { System.out.println("Wrong config! The first (minimum) Y coordinate in that Y Range condition is not a number and not the 'x' (minimum value)!"); }
                    }
                } catch (Exception e2) {
                    if (debug) { System.out.println("The first (minimum) Y coordinate in that Y Range is not a String. Error!"); }
                }
            }
            try {
                yMax = yRange.get(1).getAsShort();
                if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range condition successfully parsed as a short number"); }
            } catch (NumberFormatException e1) {
                if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range condition is not a number. Try to parse it as 'x' (maximum value)..."); }
                try {
                    if (yRange.get(1).getAsString().equals("x")) {
                        if (debug) { System.out.println("Apply the maximum value (255) to the second Y coordinate in that Y Range"); }
                        yMax = 255;
                    } else {
                        if (debug) { System.out.println("Wrong config! The second (maximum) Y coordinate in that Y Range condition is not a number and not the 'x' (maximum value)!"); }
                    }
                } catch (Exception e2) {
                    if (debug) { System.out.println("The second (maximum) Y coordinate in that Y Range is not a String. Error!"); }
                }
            }
//            if (debug) { System.out.println("return!"); }
            if (debug) { System.out.println("The yMin is: " + yMin + ", the yMax is: " + yMax); }
            if (debug) { System.out.println("Y coordinate of the entity is: " + entity.posY); }
            if (entity.posY > 255) {
                if (debug) { System.out.println("Achtung! The entity is out of the build limit: Y coordinate is " + entity.posY + " and it's above 255!"); }
                if (debug) { System.out.println("Any Y Range condition is always FALSE for such huge Y coordinate!"); }
            }
            return entity.posY >= yMin && entity.posY <= yMax;
        }
        return true;
    }

    private static boolean checkPhase(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkPhase..."); }
        if (conditionSet.has("phase")) {
            if (debug) { System.out.println("That condition group has phase!"); }
            byte requiredPhase = conditionSet.get("phase").getAsByte();
            byte phase = getCurrentPhase(entity);
            if (debug) { System.out.println("Current phase in dimension " + entity.world.provider.getDimension() + " is: " + phase + " and required phase for that condition is: " + requiredPhase); }
//            if (debug) { System.out.println("return!"); }
            return (requiredPhase == phase);
        }
        if (debug) { System.out.println("That condition group has no phase condition. Phase condition return true!"); }
        return true;
    }

    private static boolean checkStages(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkStages..."); }
        if (conditionSet.has("stages")) {
            if (debug) { System.out.println("That condition group has stages!"); }
            JsonArray gameStagesArray = conditionSet.getAsJsonArray("stages");
            List<String> hasStages = new ArrayList<>();
            List<String> hasNotStages = new ArrayList<>();
            if (debug) { System.out.println("Player must have stages: " + hasStages); }
            if (debug) { System.out.println("Player must have not stages: " + hasNotStages); }

            for (int i = 0; i < gameStagesArray.size(); i++) {
                String stage = gameStagesArray.get(i).getAsString();
                if (debug) { System.out.println("stage " + stage + "..."); }

                if (stage.startsWith("+")) {
                    hasStages.add(stage.substring(1));
//                    if (debug) { System.out.println("must have"); }
                } else if (stage.startsWith("-")) {
                    hasNotStages.add(stage.substring(1));
//                    if (debug) { System.out.println("must have not"); }
                }
            }

//            if (!isGameStageNearby(entity, hasStages) || isGameStageNearby(entity, hasNotStages)) {
            for (String stage : hasNotStages) {
                if (isGameStageNearby(entity, stage)) {
                    if (debug) { System.out.println("A player in the small radius (server-side render distance) near the entity have a stage " + stage + " that (s)he should not have"); }
                    if (debug) { System.out.println("Stage condition failed"); }
                    return false;
                }
            }
            boolean hasStagesBoolean = isGameStageNearby(entity, hasStages);
            if (hasStagesBoolean) {
                if (debug) { System.out.println("A player in the small radius (server-side render distance) near the entity have all stages that (s)he should have"); }
                if (debug) { System.out.println("Stage condition passed!"); }
            }
            return hasStagesBoolean;
//            if (!isGameStageNearby(entity, hasStages)) {
//                return false;
//            }
        }
        if (debug) { System.out.println("That condition group has no game stages. Stages condition return true!"); }
        return true;
    }

    private static boolean checkTime(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkTime..."); }
        return bigMethod("time", conditionSet, entity);
    }

    private static boolean checkDifficulty(JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("checkDifficulty..."); }
        return bigMethod("difficulty", conditionSet, entity);
    }

    private static boolean bigMethod(String type, JsonObject conditionSet, Entity entity) {
        if (debug) { System.out.println("Called a big method for check difficulty or time condition..."); }
        if (conditionSet.has(type)) {
            if (debug) { System.out.println("That condition group has " + type + "!"); }
            boolean validTypeConfig = true;  // Флаг для проверки корректности данных

            double typeDataMin = 0;
            double typeDataMax;
            if (type.equals("difficulty")) {
                if (debug) { System.out.println("The 'type' equals " + type + "!"); }
                typeDataMax = DifficultyConfig.pointsLimit;
            } else if (type.equals("time")) {
                if (debug) { System.out.println("The 'type' equals " + type + "!"); }
                typeDataMax = Long.MAX_VALUE;
            } else {
                if (debug) { System.out.println("Error! The bigMethod call is not the difficulty and not the time! It's: " + type); }
                typeDataMax = 0;
                return false;
            }

            JsonArray typeDataRange = conditionSet.getAsJsonArray(type);
            if (typeDataRange.size() != 2) {
                if (debug) { System.out.println("Config file is wrong! The '" + type + "' condition is wrong, array size is not 2, it's not a range!"); }
                return false;  // Если массив не содержит два элемента, завершить выполнение с ошибкой
            }

            // The minimum value in a range
            if (typeDataRange.get(0).isJsonPrimitive()) {
                JsonPrimitive primitiveMin = typeDataRange.get(0).getAsJsonPrimitive();
                if (primitiveMin.isNumber()) {
//                    typeDataMin = primitiveMin.getAsDouble(); // !!!
                    typeDataMin = (type.equals("difficulty") ? primitiveMin.getAsDouble() : primitiveMin.getAsLong());
                    if (debug) { System.out.println("The minimum " + type + " value is: " + typeDataMin); }
                } else if (primitiveMin.isString()) {
                    String minValue = primitiveMin.getAsString();
                    if (minValue.equals("x")) {
                        typeDataMin = 0;
                        if (debug) { System.out.println("The first element in that " + type + " array is 'x', setting minimum value to 0."); }
                    } else {
                        if (debug) { System.out.println("Error! That " + type + " array in that condition is wrong! It's not a number and not the 'x' (the minimum value)! It's: " + primitiveMin); }
                        validTypeConfig = false;
                    }
                }
            } else {
                if (debug) { System.out.println("Error! " + typeDataRange.get(0) + ", the first element in that " + type + " array is not a primitive type."); }
//                return false;
                validTypeConfig = false;
            }

            // The maximum value in a range
            if (typeDataRange.get(1).isJsonPrimitive()) {
                JsonPrimitive primitiveMax = typeDataRange.get(1).getAsJsonPrimitive();
                if (primitiveMax.isNumber()) {
//                    typeDataMax = primitiveMax.getAsDouble();
                    typeDataMax = (type.equals("difficulty") ? primitiveMax.getAsDouble() : primitiveMax.getAsLong());
                    if (debug) { System.out.println("The maximum " + type + " value is " + typeDataMax); }
                } else if (primitiveMax.isString()) {
                    String maxValue = primitiveMax.getAsString();
                    if (maxValue.equals("x")) {
//                        typeDataMax = DifficultyConfig.pointsLimit;
                        if (debug) { System.out.println("The second element in that " + type + " array is 'x', setting maximum value to " + typeDataMax + "."); }
                    } else {
                        if (debug) { System.out.println("Error! That " + type + " array in that condition is wrong! It's not a number and not the 'x' (the maximum value)!"); }
                        validTypeConfig = false;
                    }
                }
            } else {
                if (debug) { System.out.println("Error! " + typeDataRange.get(1) + ", the second element in that " + type + " array is not a primitive type."); }
                validTypeConfig = false;
            }

            if (!validTypeConfig) {
                if (debug) { System.out.println("Invalid " + type + " values. Exiting check."); }
                return false;
            }

            // Check nearby players
            List<EntityPlayerMP> nearbyPlayers = getNearbyPlayers(entity);
            Object playerTypeDataAmount = 0;
            for (EntityPlayerMP player : nearbyPlayers) {
                NBTTagCompound playerData = player.getEntityData();
                NBTTagCompound persistentData;

                if (playerData.hasKey(EntityPlayer.PERSISTED_NBT_TAG)) {
                    persistentData = playerData.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
//                    playerTypeDataAmount = persistentData.getDouble("PlayerDifficultyPoints");
                    if (type.equals("difficulty")) {
                        if (debug) { System.out.println("Try to get data from the NBT for the PlayerDifficultyPoints tag..."); }
                        playerTypeDataAmount = persistentData.getDouble("PlayerDifficultyPoints");
                        if (debug) { System.out.println("The amount of player's difficulty points is: " + playerTypeDataAmount); }
                    } else if (type.equals("time")) {
                        if (debug) { System.out.println("Try to get data from the NBT for the " + TIME_SPENT_TAG + " tag for the current dimension..."); }

                        updateTimeSpent(player);
                        NBTTagCompound timeSpentTag = persistentData.getCompoundTag(TIME_SPENT_TAG);

                        for (String key : timeSpentTag.getKeySet()) {
                            if (debug) { System.out.println("The key is: " + key + " // The value is: " + timeSpentTag.getLong(key)); }
                            if (debug) { System.out.println("The player " + player + " // " + player.getName() + " is in dimension: " + player.dimension); }
                            short dimId = Short.parseShort(key);
                            if (dimId == player.dimension) {
                                if (debug) { System.out.println("The player's dimension equals to a one of dimensions from NBT!"); }
                                playerTypeDataAmount = timeSpentTag.getLong(key);
                                if (debug) { System.out.println("The player " + player.getName() + " spent " + playerTypeDataAmount + " ticks in dimension " + dimId); }
                            }
                            if (debug) { System.out.println("The 'for' block of code is end"); }
                        }
                    }
                    if (debug) { System.out.println("Player " + player.getName() + " has " + type + " amount: " + playerTypeDataAmount); }

                    if ((type.equals("difficulty") ? (double)playerTypeDataAmount : (long)playerTypeDataAmount) >= (double)typeDataMin
                            && (type.equals("difficulty") ? (double)playerTypeDataAmount : (long)playerTypeDataAmount) <= (double)typeDataMax) {
                        if (debug) { System.out.println(type + " condition passed, return true!"); }
                        return true;
                    } else {
                        if (debug) { System.out.println("Condition not passed: the 'playerTypeDataAmount' is: " + playerTypeDataAmount + ", it's not in the range between minimum " + typeDataMin + " and maximum " + typeDataMax); }
                    }
                } else {
                    if (debug) { System.out.println("Error!!! Player " + player.getName() + " does not have " + (type.equals("difficulty") ? "DifficultyPoints" : "TimeSpentInDimensions") + " tag in NBT!!!"); }
                }
            }
            return false;
        } else {
            if (debug) { System.out.println("That condition group has no " + type + " condition."); }
            return true;
        }
    }
}
